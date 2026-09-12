package com.github.magnusp.logback.resilientjdbc;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A standard {@link DataSource} implementation configurable directly via Logback XML.
 * <p>
 * Supports standard JDBC configuration parameters ({@code driverClass}, {@code url},
 * {@code user}, {@code password}) without requiring external connection pool libraries,
 * JNDI, or programmatic setup.
 * </p>
 */
public class DriverManagerDataSource extends ContextAwareBase implements DataSource, LifeCycle {

    private String driverClass;
    private String url;
    private String user;
    private String password;

    private volatile boolean started = false;
    private PrintWriter logWriter;
    private int loginTimeout = 0;

    @Override
    public void start() {
        if (url == null || url.trim().isEmpty()) {
            addError("Missing \"url\" property for DriverManagerDataSource");
            return;
        }

        if (driverClass != null && !driverClass.trim().isEmpty()) {
            try {
                Class.forName(driverClass.trim());
            } catch (ClassNotFoundException e) {
                addError("Could not find JDBC driver class: " + driverClass, e);
                return;
            }
        } else {
            // Attempt to resolve driver via DriverManager using the URL
            try {
                DriverManager.getDriver(url.trim());
            } catch (SQLException e) {
                addWarn("Could not find registered driver for URL: " + url + " (will attempt connection at runtime)");
            }
        }

        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (user != null || password != null) {
            return getConnection(user, password);
        }
        return DriverManager.getConnection(url);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Properties props = new Properties();
        if (username != null) {
            props.put("user", username);
        }
        if (password != null) {
            props.put("password", password);
        }
        return DriverManager.getConnection(url, props);
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger is not supported");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface != null && iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + (iface != null ? iface.getName() : "null"));
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isInstance(this);
    }
}
