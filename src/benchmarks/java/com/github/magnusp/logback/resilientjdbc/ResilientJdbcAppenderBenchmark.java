package com.github.magnusp.logback.resilientjdbc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.openjdk.jmh.annotations.*;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"--enable-native-access=ALL-UNNAMED"})
public class ResilientJdbcAppenderBenchmark {

    private Path tempDir;
    private SQLiteDataSource dataSource;
    private EventSqlBinder relationalBinder;
    private EventSqlBinder jsonBinder;
    private List<ILoggingEvent> batchOf100;
    private List<ILoggingEvent> batchOf500;
    private Connection persistentConn;

    @Setup(org.openjdk.jmh.annotations.Level.Trial)
    public void setupTrial() throws IOException, SQLException {
        tempDir = Files.createTempDirectory("jmh-sqlite-resilient-");
        File dbFile = tempDir.resolve("bench.db").toFile();

        dataSource = new SQLiteDataSource();
        // Deterministic SQLite PRAGMAs: WAL mode, synchronous=NORMAL for high durability and performance
        dataSource.setUrl("jdbc:sqlite:" + dbFile.getAbsolutePath() + "?journal_mode=WAL&synchronous=NORMAL");

        persistentConn = dataSource.getConnection();
        persistentConn.setAutoCommit(false);

        relationalBinder = new DefaultSqliteEventSqlBinder("relational_logs");
        relationalBinder.init(persistentConn);

        jsonBinder = new JsonColumnEventSqlBinder("json_logs");
        jsonBinder.init(persistentConn);
        persistentConn.commit();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("BenchmarkLogger");

        batchOf100 = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            LoggingEvent event = new LoggingEvent(
                    "com.example.OrderService",
                    logger,
                    Level.INFO,
                    "Order processing completed successfully for customer: " + i,
                    null,
                    null
            );
            event.setMDCPropertyMap(Map.of("tenantId", "tenant-" + i, "traceId", "trace-abc-" + i));
            batchOf100.add(event);
        }

        batchOf500 = new ArrayList<>(500);
        for (int i = 0; i < 500; i++) {
            LoggingEvent event = new LoggingEvent(
                    "com.example.PaymentService",
                    logger,
                    Level.INFO,
                    "Payment transaction authorized for amount: " + (i * 10),
                    null,
                    null
            );
            event.setMDCPropertyMap(Map.of("merchantId", "m-" + i, "currency", "USD"));
            batchOf500.add(event);
        }
    }

    @TearDown(org.openjdk.jmh.annotations.Level.Trial)
    public void tearDownTrial() throws IOException, SQLException {
        if (persistentConn != null && !persistentConn.isClosed()) {
            persistentConn.close();
        }
        if (tempDir != null) {
            deleteDirectory(tempDir.toFile());
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDirectory(f);
                else f.delete();
            }
        }
        dir.delete();
    }

    /**
     * Measures actual end-to-end SQLite write throughput for relational batch of 100 events.
     * (Returns number of log events written per second)
     */
    @Benchmark
    @OperationsPerInvocation(100)
    public void sqliteRelationalBatchOf100() throws SQLException {
        executeBatch(relationalBinder, batchOf100);
    }

    /**
     * Measures actual end-to-end SQLite write throughput for relational batch of 500 events.
     */
    @Benchmark
    @OperationsPerInvocation(500)
    public void sqliteRelationalBatchOf500() throws SQLException {
        executeBatch(relationalBinder, batchOf500);
    }

    /**
     * Measures actual end-to-end SQLite write throughput for JSON column batch of 100 events.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    public void sqliteJsonBatchOf100() throws SQLException {
        executeBatch(jsonBinder, batchOf100);
    }

    /**
     * Measures actual end-to-end SQLite write throughput for JSON column batch of 500 events.
     */
    @Benchmark
    @OperationsPerInvocation(500)
    public void sqliteJsonBatchOf500() throws SQLException {
        executeBatch(jsonBinder, batchOf500);
    }

    private void executeBatch(EventSqlBinder binder, List<ILoggingEvent> events) throws SQLException {
        String sql = binder.getInsertSql();
        try (PreparedStatement ps = persistentConn.prepareStatement(sql)) {
            for (ILoggingEvent event : events) {
                binder.bind(ps, event);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        persistentConn.commit();
    }
}
