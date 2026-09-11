package com.github.magnusp.logback.resilientjdbc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

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
    private boolean initializedSchema = false;

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

        // 1. Prepare event for deferred processing across threads (caller data, MDC, etc.)
        eventObject.prepareForDeferredProcessing();

        // 2. Drop low-priority logs during a network / database outage to preserve queue capacity
        if (discardLowPriorityOnStorm && isInRetryStorm.get() && isLowPriority(eventObject)) {
            droppedEventsCount.incrementAndGet();
            return;
        }

        // 3. Offer to bounded queue; if full, drop to prevent blocking application threads
        if (!buffer.offer(eventObject)) {
            droppedEventsCount.incrementAndGet();
            return;
        }

        // 4. Batch size trigger
        if (buffer.size() >= maxBufferSize) {
            scheduler.submit(this::asyncFlush);
        }
    }

    private void asyncFlush() {
        if (!isFlushing.compareAndSet(false, true)) {
            return;
        }

        try {
            if (buffer.isEmpty()) return;

            List<ILoggingEvent> batch = new ArrayList<>(maxBufferSize);
            buffer.drainTo(batch, maxBufferSize);

            if (discardLowPriorityOnStorm && isInRetryStorm.get()) {
                int beforeSize = batch.size();
                batch.removeIf(this::isLowPriority);
                droppedEventsCount.addAndGet(beforeSize - batch.size());
            }

            if (!batch.isEmpty()) {
                sendWithRetry(batch);
            }
        } finally {
            isFlushing.set(false);
        }
    }

    private void sendWithRetry(List<ILoggingEvent> batch) {
        while (isStarted() && !batch.isEmpty()) {
            DataSource ds = resolveDataSource();
            if (ds == null) {
                // DataSource not yet registered (e.g. during Spring boot initialization)
                isInRetryStorm.set(true);
                handleBackoff(batch, "DataSource not available yet for: " + dataSourceName);
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
                return;

            } catch (Exception e) {
                isInRetryStorm.set(true);
                handleBackoff(batch, "JDBC write failed: " + e.getMessage());
            }
        }
    }

    private void handleBackoff(List<ILoggingEvent> batch, String warningMessage) {
        addWarn(warningMessage + ". Entering backoff (" + currentDelayMs + "ms).");

        if (discardLowPriorityOnStorm) {
            int beforeSize = batch.size();
            batch.removeIf(this::isLowPriority);
            droppedEventsCount.addAndGet(beforeSize - batch.size());
        }

        try {
            Thread.sleep(currentDelayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return;
        }

        currentDelayMs = Math.min(currentDelayMs * 2, maxDelayMs);
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
                if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        downstreamFlushOnShutdown();
    }

    private void downstreamFlushOnShutdown() {
        if (buffer != null && !buffer.isEmpty()) {
            try {
                List<ILoggingEvent> finalBatch = new ArrayList<>();
                buffer.drainTo(finalBatch);

                if (isInRetryStorm.get() && discardLowPriorityOnStorm) {
                    finalBatch.removeIf(this::isLowPriority);
                }

                DataSource ds = resolveDataSource();
                if (ds != null && !finalBatch.isEmpty()) {
                    try (Connection conn = ds.getConnection()) {
                        conn.setAutoCommit(false);
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
                    }
                }
            } catch (Exception ignored) {
                // Safeguard against JVM crash during shutdown hook
            }
        }
    }

    // --- Configuration Getters and Setters ---

    public String getDataSourceName() { return dataSourceName; }
    public void setDataSourceName(String dataSourceName) { this.dataSourceName = dataSourceName; }

    public DataSource getDataSource() { return dataSource; }
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
