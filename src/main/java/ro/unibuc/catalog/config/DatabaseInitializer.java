package ro.unibuc.catalog.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DatabaseInitializer {

    private static final Logger LOG = AppLogger.get(DatabaseInitializer.class);

    private DatabaseInitializer() {
    }

    public static void run() {
        String script = loadScript();
        try (Connection conn = DatabaseConnection.get();
             Statement stmt = conn.createStatement()) {

            for (String statement : script.split(";")) {
                String sql = statement.trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                }
            }
            LOG.info("Database schema ready.");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException(e);
        }
    }

    private static String loadScript() {
        try (InputStream in = DatabaseInitializer.class
                .getClassLoader()
                .getResourceAsStream("schema.sql")) {
            if (in == null) {
                throw new IllegalStateException("schema.sql missing from resources");
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read schema.sql", e);
        }
    }
}
