package ro.unibuc.catalog.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigLoader {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new IllegalStateException("application.properties not found on classpath");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not load application.properties", e);
        }
    }

    private ConfigLoader() {
    }

    public static String get(String key) {
        String value = PROPS.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing config key: " + key);
        }
        return value;
    }
}
