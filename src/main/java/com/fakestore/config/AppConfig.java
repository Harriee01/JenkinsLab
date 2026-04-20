package com.fakestore.config;

/**
 * AppConfig – central place for all runtime configuration values.
 *
 * <p>Values are resolved in priority order:
 * <ol>
 *   <li>System property  (e.g. -Dbase.url=https://...)
 *   <li>Environment variable (e.g. BASE_URL=https://...)
 *   <li>Hard-coded default (last resort, only for local dev convenience)
 * </ol>
 *
 * Usage: {@code AppConfig.getBaseUrl()}
 */
public final class AppConfig {

    // Private constructor – utility class, not instantiatable
    private AppConfig() {}

    // ---------------------------------------------------------------
    // API coordinates
    // ---------------------------------------------------------------

    /** Base URL of the Fake Store API. */
    public static String getBaseUrl() {
        return resolve("BASE_URL", "base.url", "https://fakestoreapi.com");
    }

    /** Default request timeout in milliseconds. */
    public static int getConnectionTimeoutMs() {
        return Integer.parseInt(resolve("CONNECTION_TIMEOUT_MS", "connection.timeout.ms", "30000"));
    }

    /** Default socket timeout in milliseconds. */
    public static int getSocketTimeoutMs() {
        return Integer.parseInt(resolve("SOCKET_TIMEOUT_MS", "socket.timeout.ms", "30000"));
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    /**
     * Resolves a configuration value by checking system properties first,
     * then environment variables, and finally falling back to the supplied default.
     *
     * @param envKey      the OS environment variable name
     * @param sysPropKey  the Java system property name (e.g. via -D flag)
     * @param defaultVal  value returned when neither source is set
     * @return resolved configuration value (never {@code null})
     */
    private static String resolve(String envKey, String sysPropKey, String defaultVal) {
        // 1. System property wins (allows -D overrides on CI)
        String sysProp = System.getProperty(sysPropKey);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim();
        }
        // 2. Environment variable (useful for Docker / CI injected secrets)
        String envVar = System.getenv(envKey);
        if (envVar != null && !envVar.isBlank()) {
            return envVar.trim();
        }
        // 3. Compiled-in default
        return defaultVal;
    }
}
