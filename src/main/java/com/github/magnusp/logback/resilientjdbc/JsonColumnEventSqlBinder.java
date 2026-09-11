package com.github.magnusp.logback.resilientjdbc;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * An {@link EventSqlBinder} that formats the entire log event into structured JSON
 * suitable for SQLite / LibSQL JSON1 functions (e.g. {@code json_extract()}, {@code ->>}).
 * <p>
 * Table schema:
 * (id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp BIGINT, level VARCHAR(10), data JSON)
 * <p>
 * This provides query flexibility while allowing indexing on expressions like:
 * {@code CREATE INDEX idx_logs_logger ON application_logs(json_extract(data, '$.logger'))}
 */
public class JsonColumnEventSqlBinder implements EventSqlBinder {

    private String tableName = "application_logs";
    private boolean autoCreateTable = true;

    public JsonColumnEventSqlBinder() {}

    public JsonColumnEventSqlBinder(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getInsertSql() {
        return "INSERT INTO " + tableName + " (timestamp, level, data) VALUES (?, ?, ?)";
    }

    @Override
    public void bind(PreparedStatement ps, ILoggingEvent event) throws SQLException {
        ps.setLong(1, event.getTimeStamp());
        ps.setString(2, event.getLevel() != null ? event.getLevel().toString() : "UNKNOWN");
        ps.setString(3, buildJson(event));
    }

    @Override
    public void init(Connection connection) throws SQLException {
        if (autoCreateTable) {
            String createTableSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "timestamp BIGINT NOT NULL, " +
                    "level VARCHAR(10) NOT NULL, " +
                    "data JSON NOT NULL" +
                    ")";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSql);
            }
        }
    }

    private String buildJson(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("{");

        // logger
        sb.append("\"logger\":\"").append(escape(event.getLoggerName())).append("\",");

        // thread
        sb.append("\"thread\":\"").append(escape(event.getThreadName())).append("\",");

        // message
        sb.append("\"message\":\"").append(escape(event.getFormattedMessage())).append("\"");

        // exception (optional)
        String trace = extractStackTrace(event.getThrowableProxy());
        if (trace != null) {
            sb.append(",\"exception\":\"").append(escape(trace)).append("\"");
        }

        // mdc (optional)
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null && !mdc.isEmpty()) {
            sb.append(",\"mdc\":{");
            boolean first = true;
            for (Map.Entry<String, String> entry : mdc.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(entry.getKey())).append("\":\"")
                  .append(escape(entry.getValue())).append("\"");
                first = false;
            }
            sb.append("}");
        }

        sb.append("}");
        return sb.toString();
    }

    private String extractStackTrace(IThrowableProxy tp) {
        if (tp == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(tp.getClassName()).append(": ").append(tp.getMessage()).append("\n");
        StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
        if (stepArray != null) {
            for (StackTraceElementProxy step : stepArray) {
                sb.append("\tat ").append(step.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isAutoCreateTable() {
        return autoCreateTable;
    }

    public void setAutoCreateTable(boolean autoCreateTable) {
        this.autoCreateTable = autoCreateTable;
    }
}
