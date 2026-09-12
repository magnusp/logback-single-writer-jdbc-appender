package com.github.magnusp.logback.resilientjdbc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
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

    @Test
    void testPoisonPillBisectionAndChronologicalOrder() throws Exception {
        // Custom binder where a message containing "POISON" throws SQLException to simulate constraint violation/corrupt payload
        EventSqlBinder poisonBinder = new DefaultSqliteEventSqlBinder() {
            @Override
            public void bind(java.sql.PreparedStatement ps, ILoggingEvent event) throws java.sql.SQLException {
                if (event.getMessage().contains("POISON")) {
                    throw new java.sql.SQLException("Simulated payload constraint violation: data too long or invalid charset");
                }
                super.bind(ps, event);
            }
        };

        appender.stop(); // stop default
        appender = new ResilientJdbcAppender();
        appender.setContext(context);
        appender.setName("POISON_TEST_APPENDER");
        appender.setDataSource(dataSource);
        appender.setEventSqlBinder(poisonBinder);
        appender.setMaxBufferSize(5);
        appender.setFlushIntervalSeconds(1);
        appender.setInitialDelayMs(10); // fast retry for test
        appender.setDiscardLowPriorityOnStorm(false);
        appender.start();

        // Send 5 events: Event 0, Event 1, POISON Event 2, Event 3, Event 4
        for (int i = 0; i < 5; i++) {
            String msg = (i == 2) ? "POISON Event " + i : "Valid Event " + i;
            LoggingEvent event = new LoggingEvent(
                    "com.example.PoisonTest",
                    logger,
                    Level.INFO,
                    msg,
                    null,
                    null
            );
            appender.doAppend(event);
        }

        // Wait for bisection to isolate and discard POISON event while inserting the remaining 4
        int rowCount = 0;
        java.util.List<String> messages = new java.util.ArrayList<>();
        for (int retry = 0; retry < 50; retry++) {
            Thread.sleep(100);
            messages.clear();
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT message FROM application_logs ORDER BY id ASC")) {
                while (rs.next()) {
                    messages.add(rs.getString("message"));
                }
                rowCount = messages.size();
                if (rowCount >= 4) break;
            } catch (Exception ignored) {}
        }

        assertThat(rowCount).isEqualTo(4);
        assertThat(messages).containsExactly(
                "Valid Event 0",
                "Valid Event 1",
                "Valid Event 3",
                "Valid Event 4"
        );
        assertThat(appender.getInsertedEventsCount()).isEqualTo(4);
        assertThat(appender.getDroppedEventsCount()).isEqualTo(1);
    }

    @Test
    void testTransientDatabaseErrorRetriesWithoutDiscard() throws Exception {
        java.util.concurrent.atomic.AtomicInteger failCount = new java.util.concurrent.atomic.AtomicInteger(3);

        EventSqlBinder transientFailingBinder = new DefaultSqliteEventSqlBinder() {
            @Override
            public void bind(java.sql.PreparedStatement ps, ILoggingEvent event) throws java.sql.SQLException {
                if (failCount.getAndDecrement() > 0) {
                    throw new java.sql.SQLException("The database file is locked (SQLITE_BUSY)");
                }
                super.bind(ps, event);
            }
        };

        appender.stop();
        appender = new ResilientJdbcAppender();
        appender.setContext(context);
        appender.setName("TRANSIENT_TEST_APPENDER");
        appender.setDataSource(dataSource);
        appender.setEventSqlBinder(transientFailingBinder);
        appender.setMaxBufferSize(2);
        appender.setFlushIntervalSeconds(1);
        appender.setInitialDelayMs(10);
        appender.setDiscardLowPriorityOnStorm(false);
        appender.start();

        LoggingEvent event1 = new LoggingEvent("com.example.T1", logger, Level.INFO, "Transient Test 1", null, null);
        LoggingEvent event2 = new LoggingEvent("com.example.T2", logger, Level.INFO, "Transient Test 2", null, null);
        appender.doAppend(event1);
        appender.doAppend(event2);

        // Wait for retry recovery
        int rowCount = 0;
        for (int retry = 0; retry < 50; retry++) {
            Thread.sleep(100);
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM application_logs WHERE message LIKE 'Transient Test%'")) {
                if (rs.next()) {
                    rowCount = rs.getInt(1);
                    if (rowCount >= 2) break;
                }
            } catch (Exception ignored) {}
        }

        assertThat(rowCount).isEqualTo(2);
        assertThat(appender.getInsertedEventsCount()).isEqualTo(2);
        // None should be discarded because it was recognized as a transient error
        assertThat(appender.getDroppedEventsCount()).isZero();
    }

    @Test
    void testXmlConfigurationWithNestedDriverManagerDataSource() throws Exception {
        Path dbFile = tempDir.resolve("xml_configured.db");
        String xmlConfig =
                "<configuration>\n" +
                "    <appender name=\"XML_JDBC\" class=\"com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppender\">\n" +
                "        <dataSource class=\"com.github.magnusp.logback.resilientjdbc.DriverManagerDataSource\">\n" +
                "            <driverClass>org.sqlite.JDBC</driverClass>\n" +
                "            <url>jdbc:sqlite:" + dbFile.toAbsolutePath() + "</url>\n" +
                "        </dataSource>\n" +
                "        <maxBufferSize>1</maxBufferSize>\n" +
                "        <flushIntervalSeconds>1</flushIntervalSeconds>\n" +
                "    </appender>\n" +
                "    <root level=\"INFO\">\n" +
                "        <appender-ref ref=\"XML_JDBC\" />\n" +
                "    </root>\n" +
                "</configuration>";

        context.reset();
        ch.qos.logback.classic.joran.JoranConfigurator configurator = new ch.qos.logback.classic.joran.JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(new java.io.ByteArrayInputStream(xmlConfig.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        ch.qos.logback.classic.Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        ResilientJdbcAppender xmlAppender = (ResilientJdbcAppender) rootLogger.getAppender("XML_JDBC");
        assertThat(xmlAppender).isNotNull();
        assertThat(xmlAppender.isStarted()).isTrue();
        assertThat(xmlAppender.getDataSource()).isInstanceOf(DriverManagerDataSource.class);

        rootLogger.info("Log message via XML configured appender");

        int rowCount = 0;
        for (int retry = 0; retry < 50; retry++) {
            Thread.sleep(100);
            try (Connection conn = xmlAppender.getDataSource().getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM application_logs")) {
                if (rs.next()) {
                    rowCount = rs.getInt(1);
                    if (rowCount >= 1) break;
                }
            } catch (Exception ignored) {}
        }

        assertThat(rowCount).isEqualTo(1);
        xmlAppender.stop();
    }

    @Test
    void testXmlConfigurationWithDefaultClassForDataSource() throws Exception {
        Path dbFile = tempDir.resolve("xml_default_class.db");
        String xmlConfig =
                "<configuration>\n" +
                "    <appender name=\"DEFAULT_CLASS_JDBC\" class=\"com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppender\">\n" +
                "        <dataSource>\n" +
                "            <driverClass>org.sqlite.JDBC</driverClass>\n" +
                "            <url>jdbc:sqlite:" + dbFile.toAbsolutePath() + "</url>\n" +
                "        </dataSource>\n" +
                "        <maxBufferSize>1</maxBufferSize>\n" +
                "        <flushIntervalSeconds>1</flushIntervalSeconds>\n" +
                "    </appender>\n" +
                "    <root level=\"INFO\">\n" +
                "        <appender-ref ref=\"DEFAULT_CLASS_JDBC\" />\n" +
                "    </root>\n" +
                "</configuration>";

        context.reset();
        ch.qos.logback.classic.joran.JoranConfigurator configurator = new ch.qos.logback.classic.joran.JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(new java.io.ByteArrayInputStream(xmlConfig.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        ch.qos.logback.classic.Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        ResilientJdbcAppender xmlAppender = (ResilientJdbcAppender) rootLogger.getAppender("DEFAULT_CLASS_JDBC");
        assertThat(xmlAppender).isNotNull();
        assertThat(xmlAppender.isStarted()).isTrue();
        assertThat(xmlAppender.getDataSource()).isInstanceOf(DriverManagerDataSource.class);

        rootLogger.info("Log message via default-class XML appender");

        int rowCount = 0;
        for (int retry = 0; retry < 50; retry++) {
            Thread.sleep(100);
            try (Connection conn = xmlAppender.getDataSource().getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM application_logs")) {
                if (rs.next()) {
                    rowCount = rs.getInt(1);
                    if (rowCount >= 1) break;
                }
            } catch (Exception ignored) {}
        }

        assertThat(rowCount).isEqualTo(1);
        xmlAppender.stop();
    }

    @Test
    void testXmlConfigurationWithVariableInterpolation() throws Exception {
        Path dbFile = tempDir.resolve("interpolated.db");
        System.setProperty("TEST_DB_PATH", dbFile.toAbsolutePath().toString());
        System.setProperty("TEST_AUTH_TOKEN", "secret-token-123");

        try {
            String xmlConfig =
                    "<configuration>\n" +
                    "    <appender name=\"INTERPOLATED_JDBC\" class=\"com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppender\">\n" +
                    "        <dataSource>\n" +
                    "            <driverClass>org.sqlite.JDBC</driverClass>\n" +
                    "            <url>jdbc:sqlite:${TEST_DB_PATH}?authToken=${TEST_AUTH_TOKEN}</url>\n" +
                    "            <password>${TEST_AUTH_TOKEN}</password>\n" +
                    "        </dataSource>\n" +
                    "        <maxBufferSize>1</maxBufferSize>\n" +
                    "        <flushIntervalSeconds>1</flushIntervalSeconds>\n" +
                    "    </appender>\n" +
                    "    <root level=\"INFO\">\n" +
                    "        <appender-ref ref=\"INTERPOLATED_JDBC\" />\n" +
                    "    </root>\n" +
                    "</configuration>";

            context.reset();
            ch.qos.logback.classic.joran.JoranConfigurator configurator = new ch.qos.logback.classic.joran.JoranConfigurator();
            configurator.setContext(context);
            configurator.doConfigure(new java.io.ByteArrayInputStream(xmlConfig.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

            ch.qos.logback.classic.Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            ResilientJdbcAppender xmlAppender = (ResilientJdbcAppender) rootLogger.getAppender("INTERPOLATED_JDBC");
            assertThat(xmlAppender).isNotNull();

            DriverManagerDataSource ds = (DriverManagerDataSource) xmlAppender.getDataSource();
            assertThat(ds.getUrl()).isEqualTo("jdbc:sqlite:" + dbFile.toAbsolutePath() + "?authToken=secret-token-123");
            assertThat(ds.getPassword()).isEqualTo("secret-token-123");

            xmlAppender.stop();
        } finally {
            System.clearProperty("TEST_DB_PATH");
            System.clearProperty("TEST_AUTH_TOKEN");
        }
    }
}
