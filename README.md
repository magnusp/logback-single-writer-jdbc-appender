# logback-single-writer-jdbc-appender

A resilient, single-background-writer JDBC appender for [Logback](https://logback.qos.ch/) built for high throughput, zero caller thread blocking, backoff on outages, and drop protection, designed specifically for SQLite and single-writer JDBC databases.

## Features

- **Single-Writer Safety**: Uses a dedicated single background thread to process writes sequentially, perfectly matched for SQLite and embedded single-writer database engines.
- **Non-blocking Appends**: Log events are offered to a bounded `ArrayBlockingQueue`. Application threads never block on database writes.
- **Transactional Batching**: Batches log events within transactions (`PreparedStatement.executeBatch()`) periodically or whenever `maxBufferSize` threshold is hit.
- **Resilience & Backoff**: Exponential backoff on database locking, contention, or network errors without crashing or starving caller threads.
- **Priority-based Dropping**: Preserves critical `WARN` and `ERROR` logs by automatically shedding low-priority (`DEBUG`, `INFO`) logs during prolonged outages or write contention storms.
- **Pluggable EventSqlBinder**: Customize SQL schema and prepared statement parameter bindings via [`EventSqlBinder`](file:///home/magnus/src/github.com/magnusp/logback-single-writer-jdbc-appender/src/main/java/com/github/magnusp/logback/resilientjdbc/EventSqlBinder.java).
  - [`DefaultSqliteEventSqlBinder`](file:///home/magnus/src/github.com/magnusp/logback-single-writer-jdbc-appender/src/main/java/com/github/magnusp/logback/resilientjdbc/DefaultSqliteEventSqlBinder.java): Traditional relational schema (`timestamp`, `level`, `logger_name`, `thread_name`, `message`, `exception`, `mdc`).
  - [`JsonColumnEventSqlBinder`](file:///home/magnus/src/github.com/magnusp/logback-single-writer-jdbc-appender/src/main/java/com/github/magnusp/logback/resilientjdbc/JsonColumnEventSqlBinder.java): JSON-oriented schema (`timestamp`, `level`, `data JSON`) leveraging SQLite/LibSQL JSON1 operators (`json_extract()`, `->>`).
- **Decoupled Spring Boot / Framework Integration**: Safe against startup ordering issues using [`DataSourceRegistry`](file:///home/magnus/src/github.com/magnusp/logback-single-writer-jdbc-appender/src/main/java/com/github/magnusp/logback/resilientjdbc/DataSourceRegistry.java). Events are buffered safely until the `DataSource` bean finishes initializing.

---

## Getting Started

### 1. Register DataSource in Spring Boot (or Application Entry)

```java
import com.github.magnusp.logback.resilientjdbc.DataSourceRegistry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;

@Component
public class LoggingDataSourceConfig {

    private final DataSource dataSource;

    public LoggingDataSourceConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        DataSourceRegistry.register("loggingDataSource", dataSource);
    }
}
```

### 2. Configure `logback.xml`

#### Standard Relational Table
```xml
<configuration>
    <appender name="RESILIENT_JDBC" class="com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppender">
        <dataSourceName>loggingDataSource</dataSourceName>
        <maxBufferSize>100</maxBufferSize>
        <flushIntervalSeconds>5</flushIntervalSeconds>
        <queueCapacity>10000</queueCapacity>
        <discardLowPriorityOnStorm>true</discardLowPriorityOnStorm>
    </appender>

    <root level="INFO">
        <appender-ref ref="RESILIENT_JDBC" />
    </root>
</configuration>
```

#### JSON Column Schema (SQLite / LibSQL JSON1)
```xml
<configuration>
    <appender name="RESILIENT_JSON_JDBC" class="com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppender">
        <dataSourceName>loggingDataSource</dataSourceName>
        <eventSqlBinderClassName>com.github.magnusp.logback.resilientjdbc.JsonColumnEventSqlBinder</eventSqlBinderClassName>
        <maxBufferSize>100</maxBufferSize>
        <flushIntervalSeconds>5</flushIntervalSeconds>
        <queueCapacity>10000</queueCapacity>
    </appender>

    <root level="INFO">
        <appender-ref ref="RESILIENT_JSON_JDBC" />
    </root>
</configuration>
```

---

## Development & Testing

Built with **Java 25+** and **Maven 3.9+** using [mise](https://mise.jdx.dev/).

```bash
# Run tests
mvn clean test

# Run JMH benchmarks
mvn test -Pbenchmark
```

## Benchmarks & Baseline

JMH benchmarks run deterministically against an isolated SQLite database configured with WAL journal mode. Benchmarks are tracked across PRs and main branches automatically via GitHub Actions.

---

## Credits & Acknowledgements

Created with the assistance of **Gemini 3.8 Flash**.
