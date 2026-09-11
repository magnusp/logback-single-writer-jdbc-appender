# logback-single-writer-jdbc-appender

A resilient, single-background-writer JDBC appender for [Logback](https://logback.qos.ch/) built for high throughput, zero caller thread blocking, backoff on outages, and drop protection, designed specifically for Turso and SQLite-backed systems.

## Features

- **Non-blocking Appends**: Log events are offered to a bounded `ArrayBlockingQueue`. Application threads never block on database writes.
- **Batching & Auto-commit Handling**: Batches logs within transactions (`PreparedStatement.executeBatch()`) periodically or whenever `maxBufferSize` threshold is hit.
- **Resilience & Backoff**: Exponential backoff on database or network errors without crashing or starving caller threads.
- **Priority-based Dropping**: Preserves critical `WARN` and `ERROR` logs by automatically shedding low-priority (`DEBUG`, `INFO`) logs during prolonged outages.
- **Pluggable EventSqlBinder**: Customize SQL schema and prepared statement parameter bindings via [`EventSqlBinder`](file:///home/magnus/src/github.com/magnusp/logback-single-writer-jdbc-appender/src/main/java/com/github/magnusp/logback/resilientjdbc/EventSqlBinder.java). Default schema provided by [`DefaultTursoEventSqlBinder`](file:///home/magnus/src/github.com/magnusp/logback-single-writer-jdbc-appender/src/main/java/com/github/magnusp/logback/resilientjdbc/DefaultTursoEventSqlBinder.java).
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
        DataSourceRegistry.register("tursoDataSource", dataSource);
    }
}
```

### 2. Configure `logback.xml`

```xml
<configuration>
    <appender name="TURSO_JDBC" class="com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppender">
        <dataSourceName>tursoDataSource</dataSourceName>
        <maxBufferSize>100</maxBufferSize>
        <flushIntervalSeconds>5</flushIntervalSeconds>
        <queueCapacity>10000</queueCapacity>
        <discardLowPriorityOnStorm>true</discardLowPriorityOnStorm>
    </appender>

    <root level="INFO">
        <appender-ref ref="TURSO_JDBC" />
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

JMH benchmarks run deterministically against a temporary SQLite database configured with WAL journal mode. Benchmarks are tracked across PRs and main branches automatically via GitHub Actions.

---

## Credits & Acknowledgements

Created with the assistance of **Gemini 3.8 Flash**.
