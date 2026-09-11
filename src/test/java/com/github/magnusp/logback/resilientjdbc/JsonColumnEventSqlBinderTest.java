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

import static org.assertj.core.api.Assertions.assertThat;

class JsonColumnEventSqlBinderTest {

    @TempDir
    Path tempDir;

    private LoggerContext context;
    private Logger logger;
    private DataSource dataSource;
    private ResilientJdbcAppender appender;

    @BeforeEach
    void setUp() {
        context = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger = context.getLogger("com.example.OrderService");

        Path dbFile = tempDir.resolve("json_test.db");
        dataSource = TestDataSourceHelper.createSqliteDataSource(dbFile);

        DataSourceRegistry.clear();
        DataSourceRegistry.register("jsonDs", dataSource);

        appender = new ResilientJdbcAppender();
        appender.setContext(context);
        appender.setName("JSON_JDBC");
        appender.setDataSourceName("jsonDs");
        appender.setEventSqlBinder(new JsonColumnEventSqlBinder());
        appender.setMaxBufferSize(2);
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
    void testJsonInsertAndJson1Queries() throws Exception {
        appender.start();

        LoggingEvent event = new LoggingEvent(
                "com.example.OrderService",
                logger,
                Level.INFO,
                "Order created with id 42",
                null,
                null
        );
        event.setMDCPropertyMap(Map.of("tenantId", "tenant-123", "userId", "user-456"));
        appender.doAppend(event);

        // Wait for asynchronous flush
        String extractedLogger = null;
        String extractedTenant = null;

        for (int retry = 0; retry < 50; retry++) {
            Thread.sleep(100);
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 // Uses SQLite JSON1 json_extract function
                 ResultSet rs = stmt.executeQuery(
                         "SELECT json_extract(data, '$.logger'), json_extract(data, '$.mdc.tenantId') " +
                         "FROM application_logs")) {
                if (rs.next()) {
                    extractedLogger = rs.getString(1);
                    extractedTenant = rs.getString(2);
                    if (extractedLogger != null) break;
                }
            } catch (Exception ignored) {}
        }

        assertThat(extractedLogger).isEqualTo("com.example.OrderService");
        assertThat(extractedTenant).isEqualTo("tenant-123");
    }
}
