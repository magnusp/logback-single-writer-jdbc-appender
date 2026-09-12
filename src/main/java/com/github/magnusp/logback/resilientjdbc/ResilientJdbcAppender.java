package com.github.magnusp.logback.resilientjdbc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.joran.spi.DefaultClass;
import ch.qos.logback.core.spi.LifeCycle;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * High-performance, resilient Logback appender that writes log events to a single-writer JDBC DataSource
 * (such as SQLite or embedded relational databases) using a single background worker thread,
 * transactional batching, and backoff under failure.
 */
public class ResilientJdbcAppender extends AppenderBase<ILoggingEvent> {

    private BlockingQueue<ILoggingEvent> buffer;
    private final AtomicBoolean isFlushing = new AtomicBoolean(false);
    private final AtomicBoolean isInRetryStorm = new AtomicBoolean(false);

    // Monitoring metrics
    private final AtomicLong droppedEventsCount = new AtomicLong(0);
    private final AtomicLong insertedEventsCount = new AtomicLong(0);

    // Configuration Options
    private String dataSourceName = "default";
    private DataSource dataSource;
    private EventSqlBinder eventSqlBinder;
    private String eventSqlBinderClassName;

    private int maxBufferSize = 100;          // Flush trigger threshold
    private int flushIntervalSeconds = 5;     // Periodic flush trigger
    private int queueCapacity = 10000;        // Bounded capacity to prevent OOM
    private long initialDelayMs = 1000;       // Initial retry backoff
    private long maxDelayMs = 60000;          // Max retry backoff (capped at 60s)
    private long currentDelayMs = 1000;
    private boolean discardLowPriorityOnStorm = true;

    private ScheduledExecutorService scheduler;
    private volatile Thread flusherThread;
    private boolean initializedSchema = false;

    // Rate-limiting status warnings to prevent log storms
    private static final long WARN_THROTTLE_INTERVAL_MS = 2000;
    private final AtomicLong lastWarnTimestamp = new AtomicLong(0);

    @Override
    public void start() {
        if (flushIntervalSeconds <= 0 || maxBufferSize <= 0 || queueCapacity <= 0) {
            addError("Invalid parameters provided for ResilientJdbcAppender: " +
                    "flushIntervalSeconds=" + flushIntervalSeconds +
                    ", maxBufferSize=" + maxBufferSize +
                    ", queueCapacity=" + queueCapacity);
            return;
        }

        buffer = new ArrayBlockingQueue<>(queueCapacity);
        currentDelayMs = initialDelayMs;

        // Start DataSource if it implements LifeCycle and has not been started
        if (dataSource instanceof LifeCycle lifeCycleDs && !lifeCycleDs.isStarted()) {
            lifeCycleDs.start();
        }

        // Instantiate EventSqlBinder if configured or default
        if (eventSqlBinder == null) {
            if (eventSqlBinderClassName != null && !eventSqlBinderClassName.isBlank()) {
                try {
                    Class<?> clazz = Class.forName(eventSqlBinderClassName);
                    eventSqlBinder = (EventSqlBinder) clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    addError("Failed to instantiate EventSqlBinder: " + eventSqlBinderClassName, e);
                    return;
                }
            } else {
                eventSqlBinder = new DefaultSqliteEventSqlBinder();
            }
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "logback-resilient-jdbc-flusher");
            t.setDaemon(true);
            return t;
        });

        // Periodic trigger
        scheduler.scheduleAtFixedRate(this::asyncFlush, flushIntervalSeconds, flushIntervalSeconds, TimeUnit.SECONDS);

        super.start();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted()) return;

        // 1. Drop low-priority logs early during a retry storm to preserve both queue capacity and heap memory
        // (avoids materializing heavy stack traces and MDC strings via prepareForDeferredProcessing)
        if (discardLowPriorityOnStorm && isInRetryStorm.get() && isLowPriority(eventObject)) {
            droppedEventsCount.incrementAndGet();
            return;
        }

        // 2. Prepare event for deferred processing across threads (caller data, MDC, etc.)
        eventObject.prepareForDeferredProcessing();

        // 3. Offer to bounded queue; if full, drop to prevent blocking application threads
        if (!buffer.offer(eventObject)) {
            droppedEventsCount.incrementAndGet();
            return;
        }

        // 4. Batch size trigger: only submit flush if not already actively flushing
        if (buffer.size() >= maxBufferSize && !isFlushing.get()) {
            scheduler.submit(this::asyncFlush);
        }
    }

    private void asyncFlush() {
        if (!isFlushing.compareAndSet(false, true)) {
            return;
        }
        flusherThread = Thread.currentThread();

        try {
            // Drain in batches of maxBufferSize until the buffer is empty
            // or an active retry storm halts processing
            while (!buffer.isEmpty() && isStarted()) {
                List<ILoggingEvent> batch = new ArrayList<>(maxBufferSize);
                buffer.drainTo(batch, maxBufferSize);

                if (batch.isEmpty()) {
                    break;
                }

                if (discardLowPriorityOnStorm && isInRetryStorm.get()) {
                    int beforeSize = batch.size();
                    batch.removeIf(this::isLowPriority);
                    droppedEventsCount.addAndGet(beforeSize - batch.size());
                }

                if (!batch.isEmpty()) {
                    boolean success = sendWithRetry(batch);
                    // If send failed or thread was interrupted, stop draining further batches for this cycle
                    if (!success) {
                        break;
                    }
                }
            }
        } finally {
            flusherThread = null;
            isFlushing.set(false);
        }
    }

    /**
     * Attempts to write the batch to the database. On failure, bisects the batch recursively
     * until individual events are isolated — transient failures back off and retry the sub-batch,
     * while a single-event failure is discarded as an unwritable poison pill.
     *
     * <p>Chronological order is preserved: the first half is always attempted before the second.
     *
     * @return {@code true} if the appender should continue flushing, {@code false} if interrupted
     *         or the appender was stopped.
     */
    private boolean sendWithRetry(List<ILoggingEvent> batch) {
        return sendBatch(batch, false, 0);
    }

    private static final int MAX_BISECTION_DEPTH = 6; // max recursion depth (100 -> 50 -> 25 -> 12 -> 6 -> 3 -> 1)

    /**
     * Recursive bisecting send. {@code isBisectedSingle} is {@code true} when the batch has
     * already been reduced to a single event; on failure it is silently discarded.
     * {@code depth} tracks recursive bisection depth to avoid exhaustive iteration on batch-wide failures.
     */
    private boolean sendBatch(List<ILoggingEvent> batch, boolean isBisectedSingle, int depth) {
        while (isStarted() && !batch.isEmpty()) {
            DataSource ds = resolveDataSource();
            if (ds == null) {
                isInRetryStorm.set(true);
                // No bisection for missing DataSource — back off and retry the whole batch
                String missingMsg = dataSourceName != null ?
                        "DataSource not available yet for: " + dataSourceName :
                        "DataSource not configured or available yet";
                if (!handleBackoff(batch, missingMsg)) {
                    return false;
                }
                continue;
            }

            try (Connection conn = ds.getConnection()) {
                if (!initializedSchema) {
                    try {
                        eventSqlBinder.init(conn);
                        initializedSchema = true;
                    } catch (SQLException e) {
                        addWarn("Schema init failed: " + e.getMessage(), e);
                    }
                }

                boolean origAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);
                try {
                    String sql = eventSqlBinder.getInsertSql();
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        for (ILoggingEvent event : batch) {
                            eventSqlBinder.bind(ps, event);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                    conn.commit();
                } catch (Exception e) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        addWarn("Rollback failed: " + rollbackEx.getMessage());
                    }
                    throw e;
                } finally {
                    try {
                        conn.setAutoCommit(origAutoCommit);
                    } catch (SQLException ignored) {}
                }

                // Batch write succeeded
                insertedEventsCount.addAndGet(batch.size());
                if (isInRetryStorm.getAndSet(false)) {
                    addInfo("Connection restored. Normal operations resumed.");
                }
                currentDelayMs = initialDelayMs;
                return true;

            } catch (Exception e) {
                isInRetryStorm.set(true);

                // If interrupted or appender stopped, don't bisect or retry; exit cleanly
                if (Thread.currentThread().isInterrupted() || !isStarted()) {
                    return false;
                }

                // Distinguish transient connection/pool/database errors (e.g. database locked/busy, connection refused,
                // disk full, IO error, read-only filesystem) vs data/payload errors (constraint violation, malformed data).
                // Transient database/infrastructure errors affect the whole database and should back off without discarding.
                boolean isTransientError = isTransientDatabaseError(e);

                if (isTransientError) {
                    logThrottledWarn("Transient database error (" + e.getMessage() + "). Backing off without discarding.");
                    if (!handleBackoff(batch, "Transient database error")) {
                        return false;
                    }
                    continue; // retry the same batch
                }

                if (isBisectedSingle) {
                    // This is already a single isolated event that failed twice — discard it as a poison pill
                    droppedEventsCount.incrementAndGet();
                    logThrottledWarn("Discarding unwritable log event after bisection: logger=" +
                            batch.get(0).getLoggerName() + " msg=" + batch.get(0).getMessage() +
                            " error=" + e.getMessage());
                    return isStarted(); // keep flush loop alive; event is discarded
                }

                if (batch.size() == 1) {
                    // Single event failed for the first time — attempt one transient backoff,
                    // then discard if it still fails on the recursive call
                    logThrottledWarn("Single-event write failed, backing off before discard attempt: " + e.getMessage());
                    if (!handleBackoffSilent()) {
                        return false; // interrupted
                    }
                    return sendBatch(batch, true, depth);
                }

                // If bisection depth exceeded, entire sub-batch is systematically invalid (e.g. invalid table schema or corrupt batch)
                // Discard the sub-batch instead of exhaustively testing every single element and hanging flusher for minutes
                if (depth >= MAX_BISECTION_DEPTH) {
                    droppedEventsCount.addAndGet(batch.size());
                    logThrottledWarn("Exceeded max bisection depth (" + MAX_BISECTION_DEPTH + "); discarding uninsertable sub-batch of " +
                            batch.size() + " events to prevent thread starvation: " + e.getMessage());
                    return isStarted();
                }

                // Multi-event payload failure: bisect and retry each half in order
                logThrottledWarn("Batch of " + batch.size() + " failed, bisecting: " + e.getMessage());
                int mid = batch.size() / 2;
                List<ILoggingEvent> first = new ArrayList<>(batch.subList(0, mid));
                List<ILoggingEvent> second = new ArrayList<>(batch.subList(mid, batch.size()));

                // Apply storm filter to each half before recursing
                if (discardLowPriorityOnStorm && isInRetryStorm.get()) {
                    int before = first.size();
                    first.removeIf(this::isLowPriority);
                    droppedEventsCount.addAndGet(before - first.size());
                    before = second.size();
                    second.removeIf(this::isLowPriority);
                    droppedEventsCount.addAndGet(before - second.size());
                }

                // First half — if interrupted, abort the whole flush cycle
                if (!first.isEmpty() && !sendBatch(first, false, depth + 1)) {
                    return false;
                }
                // Second half
                if (!second.isEmpty() && !sendBatch(second, false, depth + 1)) {
                    return false;
                }
                return isStarted();
            }
        }
        return false;
    }

    /**
     * Determines if an exception represents a transient database or connectivity failure
     * (e.g., locked database, busy handler, connection unavailable, disk full, I/O error)
     * where events should NOT be discarded.
     */
    private boolean isTransientDatabaseError(Throwable t) {
        Throwable curr = t;
        while (curr != null) {
            String msg = curr.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("busy") || lower.contains("locked") ||
                    lower.contains("connection") || lower.contains("timeout") ||
                    lower.contains("pool") || lower.contains("io error") ||
                    lower.contains("ioerr") || lower.contains("full") ||
                    lower.contains("disk") || lower.contains("space") ||
                    lower.contains("readonly") || lower.contains("read-only") ||
                    lower.contains("sqlite_busy") || lower.contains("sqlite_locked") ||
                    lower.contains("sqlite_full") || lower.contains("sqlite_ioerr") ||
                    lower.contains("sqlite_readonly") || lower.contains("sqlite_cantopen")) {
                    return true;
                }
            }
            curr = curr.getCause();
        }
        return false;
    }

    /**
     * Throttles logback status warnings to at most one per {@link #WARN_THROTTLE_INTERVAL_MS}
     * to avoid recursive storms or flooding when logging errors occur frequently.
     */
    private void logThrottledWarn(String message) {
        long now = System.currentTimeMillis();
        long last = lastWarnTimestamp.get();
        if (now - last >= WARN_THROTTLE_INTERVAL_MS && lastWarnTimestamp.compareAndSet(last, now)) {
            addWarn(message);
        }
    }

    /**
     * Sleeps for the current backoff delay without logging a warning.
     * Used when backing off a single isolated event before its final discard attempt.
     *
     * @return {@code false} if the thread was interrupted
     */
    private boolean handleBackoffSilent() {
        try {
            Thread.sleep(currentDelayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        }
        currentDelayMs = Math.min(currentDelayMs * 2, maxDelayMs);
        return true;
    }

    /**
     * Logs a warning and sleeps for the current backoff delay.
     * Applies low-priority storm filtering to the batch in-place.
     * Doubles the delay for the next call (capped at {@link #maxDelayMs}).
     *
     * @return {@code false} if the thread was interrupted
     */
    private boolean handleBackoff(List<ILoggingEvent> batch, String warningMessage) {
        logThrottledWarn(warningMessage + ". Entering backoff (" + currentDelayMs + "ms).");

        if (discardLowPriorityOnStorm) {
            int beforeSize = batch.size();
            batch.removeIf(this::isLowPriority);
            droppedEventsCount.addAndGet(beforeSize - batch.size());
        }

        return handleBackoffSilent();
    }

    private DataSource resolveDataSource() {
        if (this.dataSource != null) {
            return this.dataSource;
        }
        if (this.dataSourceName != null) {
            return DataSourceRegistry.get(this.dataSourceName);
        }
        return null;
    }

    private boolean isLowPriority(ILoggingEvent event) {
        return event.getLevel() == null || !event.getLevel().isGreaterOrEqual(Level.WARN);
    }

    @Override
    public void stop() {
        super.stop();
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Wait up to 2 seconds for any active in-flight flush loop to finish cleanly.
        long waitDeadline = System.currentTimeMillis() + 2000;
        while (isFlushing.get() && System.currentTimeMillis() < waitDeadline) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // If the flusher thread is still busy (e.g. stuck sleeping in retry backoff), interrupt it
        // so it exits promptly and releases the isFlushing lock.
        Thread activeFlusher = flusherThread;
        if (isFlushing.get() && activeFlusher != null && activeFlusher.isAlive()) {
            activeFlusher.interrupt();
            try {
                activeFlusher.join(500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        // Only flush remaining events if we can exclusively acquire the flushing lock.
        // If isFlushing is still true here it means the background thread is still writing;
        // attempting a concurrent write would violate the single-writer invariant.
        if (isFlushing.compareAndSet(false, true)) {
            try {
                downstreamFlushOnShutdown();
            } finally {
                isFlushing.set(false);
            }
        } else {
            addWarn("Background flusher still active at shutdown deadline; skipping final flush to preserve single-writer invariant.");
        }

        // Stop configured DataSource if it implements LifeCycle
        if (dataSource instanceof LifeCycle lifeCycleDs && lifeCycleDs.isStarted()) {
            lifeCycleDs.stop();
        }
    }

    private void downstreamFlushOnShutdown() {
        if (buffer == null || buffer.isEmpty()) {
            return;
        }
        try {
            List<ILoggingEvent> finalBatch = new ArrayList<>();
            buffer.drainTo(finalBatch);

            if (isInRetryStorm.get() && discardLowPriorityOnStorm) {
                finalBatch.removeIf(this::isLowPriority);
            }

            DataSource ds = resolveDataSource();
            if (ds != null && !finalBatch.isEmpty()) {
                try (Connection conn = ds.getConnection()) {
                    boolean origAutoCommit = conn.getAutoCommit();
                    conn.setAutoCommit(false);
                    try {
                        String sql = eventSqlBinder.getInsertSql();
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            for (ILoggingEvent event : finalBatch) {
                                eventSqlBinder.bind(ps, event);
                                ps.addBatch();
                            }
                            ps.executeBatch();
                        }
                        conn.commit();
                        insertedEventsCount.addAndGet(finalBatch.size());
                    } catch (Exception e) {
                        try { conn.rollback(); } catch (SQLException ignored) {}
                        throw e;
                    } finally {
                        try { conn.setAutoCommit(origAutoCommit); } catch (SQLException ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {
            // Safeguard against JVM crash during shutdown hook
        }
    }

    // --- Configuration Getters and Setters ---

    public String getDataSourceName() { return dataSourceName; }
    public void setDataSourceName(String dataSourceName) { this.dataSourceName = dataSourceName; }

    public DataSource getDataSource() { return dataSource; }

    @DefaultClass(DriverManagerDataSource.class)
    public void setDataSource(DataSource dataSource) { this.dataSource = dataSource; }

    public EventSqlBinder getEventSqlBinder() { return eventSqlBinder; }
    public void setEventSqlBinder(EventSqlBinder eventSqlBinder) { this.eventSqlBinder = eventSqlBinder; }

    public String getEventSqlBinderClassName() { return eventSqlBinderClassName; }
    public void setEventSqlBinderClassName(String eventSqlBinderClassName) { this.eventSqlBinderClassName = eventSqlBinderClassName; }

    public int getMaxBufferSize() { return maxBufferSize; }
    public void setMaxBufferSize(int maxBufferSize) { this.maxBufferSize = maxBufferSize; }

    public int getFlushIntervalSeconds() { return flushIntervalSeconds; }
    public void setFlushIntervalSeconds(int flushIntervalSeconds) { this.flushIntervalSeconds = flushIntervalSeconds; }

    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }

    public long getInitialDelayMs() { return initialDelayMs; }
    public void setInitialDelayMs(long initialDelayMs) { this.initialDelayMs = initialDelayMs; }

    public long getMaxDelayMs() { return maxDelayMs; }
    public void setMaxDelayMs(long maxDelayMs) { this.maxDelayMs = maxDelayMs; }

    public boolean isDiscardLowPriorityOnStorm() { return discardLowPriorityOnStorm; }
    public void setDiscardLowPriorityOnStorm(boolean discardLowPriorityOnStorm) { this.discardLowPriorityOnStorm = discardLowPriorityOnStorm; }

    public long getDroppedEventsCount() { return droppedEventsCount.get(); }
    public long getInsertedEventsCount() { return insertedEventsCount.get(); }
    public int getBufferedEventsCount() { return buffer != null ? buffer.size() : 0; }
}
