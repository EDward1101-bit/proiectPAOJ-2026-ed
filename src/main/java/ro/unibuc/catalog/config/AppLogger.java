package ro.unibuc.catalog.config;

import java.util.logging.Logger;

public final class AppLogger {

    private AppLogger() {
    }

    public static Logger get(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}
