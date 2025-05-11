package com.welltech.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles database connection for the application
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/pi";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Get a connection to the database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Ensure the driver is loaded (still good practice even if not strictly necessary for modern JDBC)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create connection
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established");
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            // Re-throw as SQLException to indicate a SQL-related failure to the caller
            throw new SQLException("Database driver not found", e);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            // Re-throw the original SQLException
            throw e;
        }
    }

    /**
     * Initialize database by creating necessary tables if they don't exist.
     * === MODIFIED: Now declares it THROWS SQLException ===
     * @throws SQLException if a database error occurs during initialization.
     */
    public static void initializeDatabase() throws SQLException { // <<< ADDED throws SQLException here
        try (Connection connection = getConnection()) {
            // Database connection succeeded
            System.out.println("Database initialized successfully (connection obtained).");
            // --- Add your actual table creation/check SQL here if needed ---
            // Example:
            // try (java.sql.Statement stmt = connection.createStatement()) {
            //    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (...)");
            //    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS articles (...)");
            //    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS categories (...)");
            //    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS consultations (...)");
            //    System.out.println("Database schema check/creation completed.");
            // }
            // --- End of example SQL ---

        } catch (SQLException e) {
            System.err.println("Failed during database initialization process (connection failed or SQL error): " + e.getMessage());
            // === MODIFIED: RE-THROW the exception so the caller (WellTechApplication) can catch it ===
            throw e; // <<< ADDED this line
        }
        // Note: If your initialization logic involves creating tables,
        // move that logic inside the try block above. The SQLException
        // thrown by getConnection() or the executeUpdate() calls will
        // be caught, printed, and then re-thrown by the `catch (SQLException e) { throw e; }` block.
    }

    // You might also want a closeConnection method if you use it elsewhere without try-with-resources
    /*
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    */
}