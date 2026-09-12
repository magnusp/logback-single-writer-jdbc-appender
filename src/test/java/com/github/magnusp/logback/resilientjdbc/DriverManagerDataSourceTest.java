package com.github.magnusp.logback.resilientjdbc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriverManagerDataSourceTest {

    @TempDir
    Path tempDir;

    @Test
    void testStartFailsWithoutUrl() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.start();
        assertThat(ds.isStarted()).isFalse();
    }

    @Test
    void testStartFailsWithInvalidDriverClass() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl("jdbc:sqlite::memory:");
        ds.setDriverClass("com.nonexistent.Driver");
        ds.start();
        assertThat(ds.isStarted()).isFalse();
    }

    @Test
    void testValidConnection() throws SQLException {
        Path dbFile = tempDir.resolve("direct.db");
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl("jdbc:sqlite:" + dbFile.toAbsolutePath());
        ds.setDriverClass("org.sqlite.JDBC");
        ds.start();

        assertThat(ds.isStarted()).isTrue();

        try (Connection conn = ds.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isClosed()).isFalse();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE test_tbl (id INT)");
            }
        }

        try (Connection conn = ds.getConnection("user", "pass")) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM test_tbl")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isZero();
            }
        }

        ds.stop();
        assertThat(ds.isStarted()).isFalse();
    }

    @Test
    void testUnwrapAndWrapperFor() throws SQLException {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        assertThat(ds.isWrapperFor(DriverManagerDataSource.class)).isTrue();
        assertThat(ds.isWrapperFor(javax.sql.DataSource.class)).isTrue();
        assertThat(ds.isWrapperFor(String.class)).isFalse();

        assertThat(ds.unwrap(DriverManagerDataSource.class)).isSameAs(ds);
        assertThatThrownBy(() -> ds.unwrap(String.class)).isInstanceOf(SQLException.class);
    }
}
