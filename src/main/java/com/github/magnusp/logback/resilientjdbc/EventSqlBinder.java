package com.github.magnusp.logback.resilientjdbc;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Strategy interface for preparing and binding {@link ILoggingEvent} objects into SQL PreparedStatement.
 */
public interface EventSqlBinder {

    /**
     * The SQL insert query string with positional placeholders (e.g. ?).
     *
     * @return parameterized SQL insert query
     */
    String getInsertSql();

    /**
     * Binds values from the {@link ILoggingEvent} to the given {@link PreparedStatement}.
     * Note: Do NOT call {@code ps.addBatch()} or {@code ps.executeUpdate()} inside this method;
     * the caller handles batching.
     *
     * @param ps    the prepared statement to set parameters on
     * @param event the logging event to extract data from
     * @throws SQLException if a database access error occurs
     */
    void bind(PreparedStatement ps, ILoggingEvent event) throws SQLException;

    /**
     * Optional initialization hook, e.g. to create tables or verify schema.
     *
     * @param connection active database connection
     * @throws SQLException if schema initialization fails
     */
    default void init(Connection connection) throws SQLException {
        // Default no-op
    }
}
