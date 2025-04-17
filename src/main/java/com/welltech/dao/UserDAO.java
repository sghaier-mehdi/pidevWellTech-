package com.welltech.dao;

import com.welltech.model.User;
import com.welltech.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User-related database operations
 */
public class UserDAO {
    
    /**
     * Insert a new user into the database
     * @param user User object to insert
     * @return Generated user ID if successful, -1 otherwise
     */
    public int insertUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, email, phone_number, role) VALUES (?, ?, ?, ?, ?, ?)";
        
        System.out.println("Attempting to insert user: " + user.getUsername());
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            // Get connection
            System.out.println("Getting database connection...");
            conn = DatabaseConnection.getConnection();
            System.out.println("Connection successful");
            
            // Prepare statement
            System.out.println("Preparing SQL statement...");
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // Set parameters
            System.out.println("Setting parameters...");
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhoneNumber());
            pstmt.setString(6, user.getRole().toString());
            
            // Execute update
            System.out.println("Executing SQL: " + sql);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Update executed. Affected rows: " + affectedRows);
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        System.out.println("User inserted with ID: " + id);
                        return id;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("SQL Error inserting user: " + e.getMessage());
            e.printStackTrace();
            
            // Check for common MySQL error codes
            if (e.getErrorCode() == 1062) {
                System.err.println("Duplicate entry error - username or email already exists");
            } else if (e.getErrorCode() == 1045) {
                System.err.println("Access denied - check your MySQL credentials");
            } else if (e.getErrorCode() == 1049) {
                System.err.println("Unknown database - make sure the database exists");
            } else if (e.getErrorCode() == 0) {
                System.err.println("Connection error - ensure MySQL is running");
            }
        } finally {
            // Clean up resources
            try {
                if (pstmt != null) pstmt.close();
                // Don't close the connection here as it's managed by DatabaseConnection
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        System.err.println("User insertion failed");
        return -1;
    }
    
    /**
     * Get a user by their ID
     * @param id User ID
     * @return User object if found, null otherwise
     */
    public User getUserById(int id) {
        try {
            // Get database connection
            Connection connection = DatabaseConnection.getConnection();
            
            // Create query
            String query = "SELECT * FROM users WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            
            // Execute query
            ResultSet resultSet = statement.executeQuery();
            
            // Process results
            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get a user by their username
     * @param username Username
     * @return User object if found, null otherwise
     */
    public User getUserByUsername(String username) {
        try {
            // Get database connection
            Connection connection = DatabaseConnection.getConnection();
            
            // Create query
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            
            // Execute query
            ResultSet resultSet = statement.executeQuery();
            
            // Process results
            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all users from the database
     * @return List of User objects
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = extractUserFromResultSet(rs);
                users.add(user);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Update a user in the database
     * @param user User object to update
     * @return true if successful, false otherwise
     */
    public boolean updateUser(User user) {
        try {
            // Get database connection
            Connection connection = DatabaseConnection.getConnection();
            
            // Create query
            String query = "UPDATE users SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhoneNumber());
            statement.setInt(4, user.getId());
            
            // Execute query
            int rowsAffected = statement.executeUpdate();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a user from the database
     * @param id User ID
     * @return true if successful, false otherwise
     */
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update user password in the database
     * @param userId User ID
     * @param newPassword New password (should be hashed in a real app)
     * @return true if the update was successful
     */
    public boolean updateUserPassword(int userId, String newPassword) {
        try {
            // Get database connection
            Connection connection = DatabaseConnection.getConnection();
            
            // Create query
            String query = "UPDATE users SET password = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, newPassword); // In a real app, hash the password
            statement.setInt(2, userId);
            
            // Execute query
            int rowsAffected = statement.executeUpdate();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Helper method to extract a User object from a ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        return user;
    }
} 