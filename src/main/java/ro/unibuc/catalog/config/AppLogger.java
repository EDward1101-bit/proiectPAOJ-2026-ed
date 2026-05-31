package ro.unibuc.catalog.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Central factory for {@link Logger} instances.
 *
 * <p>The first time a logger is requested the java.util.logging subsystem is
 * configured from {@code logging.properties} on the classpath, which adds a
 * file handler writing the full INFO trail to {@code catalog.log} while keeping
 * the console restricted to warnings and errors.</p>
 */
public final class AppLogger {

    private static volatile boolean configured = false;

    private AppLogger() {
    }

    public static Logger get(Class<?> clazz) {
        ensureConfigured();
        return Logger.getLogger(clazz.getName());
    }

    private static synchronized void ensureConfigured() {
        if (configured) {
            return;
        }
        try (InputStream in = AppLogger.class.getClassLoader()
                .getResourceAsStream("logging.properties")) {
            if (in != null) {
                LogManager.getLogManager().readConfiguration(in);
            }
        } catch (IOException e) {
            System.err.println("Could not load logging configuration: " + e.getMessage());
        }
        configured = true;
    }
}
