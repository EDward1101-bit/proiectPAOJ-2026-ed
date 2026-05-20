package ro.unibuc.catalog.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(
                ConfigLoader.get("db.url"),
                ConfigLoader.get("db.user"),
                ConfigLoader.get("db.password")
        );
    }
}
