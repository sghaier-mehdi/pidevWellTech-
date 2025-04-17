package com.welltech.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for managing database connections
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/";
    private static final String DB_NAME = "pi";
    private static final String FULL_URL = URL + DB_NAME;
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; // Updated with your MySQL password
    
    private static Connection connection;
    
    static {
        try {
            // Initialize the database and tables
            initializeDatabase();
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize the database and create necessary tables if they don't exist
     */
    private static void initializeDatabase() throws SQLException {
        try {
            // First create the database if it doesn't exist
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                 Statement stmt = conn.createStatement()) {
                
                // We don't need to create the database since 'pi' already exists
                System.out.println("Using existing database: " + DB_NAME);
            }
            
            // Now create tables
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Create users table
                String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT,"
                        + "username VARCHAR(50) NOT NULL UNIQUE,"
                        + "password VARCHAR(255) NOT NULL,"
                        + "full_name VARCHAR(100) NOT NULL,"
                        + "email VARCHAR(100) NOT NULL UNIQUE,"
                        + "phone_number VARCHAR(20),"
                        + "role ENUM('PATIENT', 'PSYCHIATRIST', 'ADMIN') NOT NULL,"
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                        + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                        + ")";
                stmt.executeUpdate(createUserTableSQL);
                System.out.println("Users table created or already exists");
                
                // More tables can be added here as needed
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    /**
     * Get a connection to the database
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(FULL_URL, USERNAME, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Database driver not found", e);
            }
        }
        return connection;
    }
    
    /**
     * Close the database connection
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
} 