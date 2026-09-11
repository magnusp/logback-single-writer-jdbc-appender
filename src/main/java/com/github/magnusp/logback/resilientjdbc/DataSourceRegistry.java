package com.github.magnusp.logback.resilientjdbc;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Global registry for DataSource instances.
 * <p>
 * This facilitates decoupling Logback configuration from application dependency injection frameworks
 * like Spring Boot or CDI. Applications can register DataSources programmatically on startup.
 */
public final class DataSourceRegistry {

    private static final ConcurrentMap<String, DataSource> REGISTRY = new ConcurrentHashMap<>();

    private DataSourceRegistry() {}

    /**
     * Registers a DataSource with a specific name.
     *
     * @param name       the unique identifier for the DataSource
     * @param dataSource the DataSource instance
     */
    public static void register(String name, DataSource dataSource) {
        if (name == null || dataSource == null) {
            throw new IllegalArgumentException("DataSource name and instance must not be null");
        }
        REGISTRY.put(name, dataSource);
    }

    /**
     * Unregisters a DataSource by name.
     *
     * @param name the identifier of the DataSource to remove
     * @return the removed DataSource, or null if none was registered under that name
     */
    public static DataSource unregister(String name) {
        if (name == null) {
            return null;
        }
        return REGISTRY.remove(name);
    }

    /**
     * Retrieves a registered DataSource by name.
     *
     * @param name the identifier of the DataSource
     * @return the DataSource, or null if not registered
     */
    public static DataSource get(String name) {
        if (name == null) {
            return null;
        }
        return REGISTRY.get(name);
    }

    /**
     * Clears all registered DataSources. Primarily for testing.
     */
    public static void clear() {
        REGISTRY.clear();
    }
}
