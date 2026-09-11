package com.github.magnusp.logback.resilientjdbc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.openjdk.jmh.annotations.*;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private ResilientJdbcAppender appender;
    private Logger logger;
    private LoggingEvent testEvent;

    @Setup(org.openjdk.jmh.annotations.Level.Trial)
    public void setupTrial() throws IOException {
        tempDir = Files.createTempDirectory("jmh-sqlite-resilient-");
        File dbFile = tempDir.resolve("bench.db").toFile();

        dataSource = new SQLiteDataSource();
        // Deterministic SQLite PRAGMAs: WAL mode, synchronous=NORMAL for standard high-throughput durability
        dataSource.setUrl("jdbc:sqlite:" + dbFile.getAbsolutePath() + "?journal_mode=WAL&synchronous=NORMAL");

        DataSourceRegistry.register("benchDs", dataSource);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger = context.getLogger("BenchmarkLogger");

        appender = new ResilientJdbcAppender();
        appender.setContext(context);
        appender.setName("BENCH_APPENDER");
        appender.setDataSourceName("benchDs");
        appender.setMaxBufferSize(500);
        appender.setFlushIntervalSeconds(1);
        appender.setQueueCapacity(50000);
        appender.start();

        testEvent = new LoggingEvent(
                "com.example.BenchService",
                logger,
                Level.INFO,
                "Benchmark logging message payload test",
                null,
                null
        );
    }

    @TearDown(org.openjdk.jmh.annotations.Level.Trial)
    public void tearDownTrial() throws IOException {
        if (appender != null) {
            appender.stop();
        }
        DataSourceRegistry.clear();
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

    @Benchmark
    public void benchmarkAppend() {
        appender.doAppend(testEvent);
    }
}
