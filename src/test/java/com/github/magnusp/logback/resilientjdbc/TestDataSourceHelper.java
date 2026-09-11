package com.github.magnusp.logback.resilientjdbc;

import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestDataSourceHelper {

    public static DataSource createSqliteDataSource(Path dbPath) {
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:" + dbPath.toAbsolutePath());
        return ds;
    }

    public static DataSource createMemorySqliteDataSource() {
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite::memory:");
        return ds;
    }
}
