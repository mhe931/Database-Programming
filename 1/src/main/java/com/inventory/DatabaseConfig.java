package com.inventory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseConfig handles the SQLite database connection and schema initialization.
 * The database file is created in the project root directory.
 */
public class DatabaseConfig {

    private static final String DB_URL = "jdbc:sqlite:inventory.db";

    /**
     * Establishes and returns a connection to the SQLite database.
     *
     * @return a Connection object to the SQLite database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes the database by creating the 'products' table if it does not exist.
     * Uses try-with-resources for safe connection and statement handling.
     */
    public static void initializeDatabase() {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS products (
                    id    INTEGER PRIMARY KEY AUTOINCREMENT,
                    name  TEXT    NOT NULL,
                    category TEXT NOT NULL,
                    price REAL   NOT NULL
                )
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("[DB] Database initialized — 'products' table is ready.");
            System.out.println("[DB] Connected to: " + DB_URL);

        } catch (SQLException e) {
            System.err.printf("[DB] Failed to initialize database at %s%n", DB_URL);
            System.err.printf("[DB] SQL State: %s | Error Code: %d | Message: %s%n",
                    e.getSQLState(), e.getErrorCode(), e.getMessage());
        }
    }
}
