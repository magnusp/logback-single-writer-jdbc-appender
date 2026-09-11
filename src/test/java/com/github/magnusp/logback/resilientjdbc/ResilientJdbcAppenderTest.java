package com.github.magnusp.logback.resilientjdbc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ResilientJdbcAppenderTest {

    @TempDir
    Path tempDir;

    private LoggerContext context;
    private Logger logger;
    private DataSource dataSource;
    private ResilientJdbcAppender appender;

    @BeforeEach
    void setUp() {
        context = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger = context.getLogger(ResilientJdbcAppenderTest.class);

        Path dbFile = tempDir.resolve("test.db");
        dataSource = TestDataSourceHelper.createSqliteDataSource(dbFile);

        DataSourceRegistry.clear();
        DataSourceRegistry.register("testDs", dataSource);

        appender = new ResilientJdbcAppender();
        appender.setContext(context);
        appender.setName("RESILIENT_JDBC");
        appender.setDataSourceName("testDs");
        appender.setMaxBufferSize(5);
        appender.setFlushIntervalSeconds(1);
    }

    @AfterEach
    void tearDown() {
        if (appender != null && appender.isStarted()) {
            appender.stop();
        }
        DataSourceRegistry.clear();
    }

    @Test
    void testSuccessfulBatchInsert() throws Exception {
        appender.start();
        assertThat(appender.isStarted()).isTrue();

        for (int i = 0; i < 10; i++) {
            LoggingEvent event = new LoggingEvent(
                    "com.example.Service",
                    logger,
                    Level.INFO,
                    "Message " + i,
                    null,
                    null
            );
            event.setMDCPropertyMap(Map.of("reqId", "id-" + i));
            appender.doAppend(event);
        }

        // Wait for asynchronous flush
        int rowCount = 0;
        for (int retry = 0; retry < 50; retry++) {
            Thread.sleep(100);
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM application_logs")) {
                if (rs.next()) {
                    rowCount = rs.getInt(1);
                    if (rowCount >= 10) break;
                }
            } catch (Exception ignored) {}
        }

        assertThat(rowCount).isEqualTo(10);
        assertThat(appender.getInsertedEventsCount()).isEqualTo(10);
        assertThat(appender.getDroppedEventsCount()).isZero();
    }

    @Test
    void testDataSourceDelayedRegistrationBuffersAndFlushes() throws Exception {
        DataSourceRegistry.clear(); // Unregister DataSource to simulate delayed Spring context refresh

        appender.setInitialDelayMs(100);
        appender.start();

        LoggingEvent warnEvent = new LoggingEvent("com.example.Warn", logger, Level.WARN, "Important warn", null, null);
        LoggingEvent infoEvent = new LoggingEvent("com.example.Info", logger, Level.INFO, "Standard info", null, null);

        appender.doAppend(warnEvent);
        appender.doAppend(infoEvent);

        // Allow appender to trigger a flush cycle while DataSource is absent
        Thread.sleep(250);

        // Register DataSource later
        DataSourceRegistry.register("testDs", dataSource);

        // Wait for recovery and flush
        int rowCount = 0;
        for (int retry = 0; retry < 50; retry++) {
            Thread.sleep(100);
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM application_logs")) {
                if (rs.next()) {
                    rowCount = rs.getInt(1);
                    if (rowCount >= 1) break;
                }
            } catch (Exception ignored) {}
        }

        // The high priority WARN should be preserved and inserted
        assertThat(rowCount).isGreaterThanOrEqualTo(1);
    }
}
