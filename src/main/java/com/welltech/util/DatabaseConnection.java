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
                        + ") ENGINE=InnoDB";
                stmt.executeUpdate(createUserTableSQL);
                System.out.println("Users table created or already exists");
                
                // Create products table
                String createProductTableSQL = "CREATE TABLE IF NOT EXISTS products ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT,"
                        + "name VARCHAR(100) NOT NULL,"
                        + "description TEXT,"
                        + "price DECIMAL(10,2) NOT NULL,"
                        + "stock INT NOT NULL DEFAULT 0,"
                        + "image_url VARCHAR(255),"
                        + "active BOOLEAN NOT NULL DEFAULT TRUE,"
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                        + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB";
                stmt.executeUpdate(createProductTableSQL);
                System.out.println("Products table created or already exists");
                
                // Create orders table
                String createOrderTableSQL = "CREATE TABLE IF NOT EXISTS orders ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT,"
                        + "user_id INT NOT NULL,"
                        + "total_amount DECIMAL(10,2) NOT NULL,"
                        + "shipping_address TEXT,"
                        + "payment_method VARCHAR(50),"
                        + "status ENUM('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',"
                        + "payment_status ENUM('UNPAID', 'PAID', 'REFUNDED') NOT NULL DEFAULT 'UNPAID',"
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                        + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB";
                stmt.executeUpdate(createOrderTableSQL);
                System.out.println("Orders table created or already exists");
                
                // Add foreign key constraint separately
                try {
                    String addForeignKeySQL = "ALTER TABLE orders ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)";
                    stmt.executeUpdate(addForeignKeySQL);
                    System.out.println("Foreign key constraint added to orders table");
                } catch (SQLException e) {
                    System.out.println("Foreign key constraint already exists or could not be added: " + e.getMessage());
                }
                
                // Create order_items table for order details
                String createOrderItemsTableSQL = "CREATE TABLE IF NOT EXISTS order_items ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT,"
                        + "order_id INT NOT NULL,"
                        + "product_id INT NOT NULL,"
                        + "quantity INT NOT NULL,"
                        + "price_per_unit DECIMAL(10,2) NOT NULL,"
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB";
                stmt.executeUpdate(createOrderItemsTableSQL);
                System.out.println("Order items table created or already exists");
                
                // Add foreign key constraints separately
                try {
                    String addOrderForeignKeySQL = "ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id)";
                    stmt.executeUpdate(addOrderForeignKeySQL);
                    System.out.println("Foreign key constraint for order_id added to order_items table");
                } catch (SQLException e) {
                    System.out.println("Foreign key constraint for order_id already exists or could not be added: " + e.getMessage());
                }
                
                try {
                    String addProductForeignKeySQL = "ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)";
                    stmt.executeUpdate(addProductForeignKeySQL);
                    System.out.println("Foreign key constraint for product_id added to order_items table");
                } catch (SQLException e) {
                    System.out.println("Foreign key constraint for product_id already exists or could not be added: " + e.getMessage());
                }
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