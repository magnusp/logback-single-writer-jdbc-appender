package com.github.magnusp.logback.resilientjdbc;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;

/**
 * Default binder implementation tailored for SQLite and single-writer relational JDBC databases.
 * Stores logs in a table with columns:
 * (timestamp, level, logger_name, thread_name, message, exception, mdc)
 */
public class DefaultSqliteEventSqlBinder implements EventSqlBinder {

    private String tableName = "application_logs";
    private boolean autoCreateTable = true;

    public DefaultSqliteEventSqlBinder() {}

    public DefaultSqliteEventSqlBinder(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getInsertSql() {
        return "INSERT INTO " + tableName + " (timestamp, level, logger_name, thread_name, message, exception, mdc) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bind(PreparedStatement ps, ILoggingEvent event) throws SQLException {
        // 1. timestamp
        ps.setLong(1, event.getTimeStamp());

        // 2. level
        ps.setString(2, event.getLevel() != null ? event.getLevel().toString() : "UNKNOWN");

        // 3. logger_name
        ps.setString(3, event.getLoggerName());

        // 4. thread_name
        ps.setString(4, event.getThreadName());

        // 5. message
        ps.setString(5, event.getFormattedMessage());

        // 6. exception
        String exceptionTrace = extractStackTrace(event.getThrowableProxy());
        if (exceptionTrace != null) {
            ps.setString(6, exceptionTrace);
        } else {
            ps.setNull(6, Types.VARCHAR);
        }

        // 7. mdc (as key=value json-like or comma separated)
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null && !mdc.isEmpty()) {
            ps.setString(7, formatMdc(mdc));
        } else {
            ps.setNull(7, Types.VARCHAR);
        }
    }

    @Override
    public void init(Connection connection) throws SQLException {
        if (autoCreateTable) {
            String createTableSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "timestamp BIGINT NOT NULL, " +
                    "level VARCHAR(10) NOT NULL, " +
                    "logger_name VARCHAR(255) NOT NULL, " +
                    "thread_name VARCHAR(100), " +
                    "message TEXT, " +
                    "exception TEXT, " +
                    "mdc TEXT" +
                    ")";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSql);
            }
        }
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

    private String formatMdc(Map<String, String> mdc) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : mdc.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("\"").append(escapeJson(entry.getKey())).append("\": \"")
              .append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Escapes a string for embedding as a JSON string value (RFC-8259 compliant).
     * Handles backslash, double-quote, control characters (U+0000–U+001F) and the
     * mandatory named escapes (\b, \f, \n, \r, \t).
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
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
