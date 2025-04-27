package com.welltech.dao;

import com.welltech.model.User;
import com.welltech.model.User.UserRole; // *** ADD THIS IMPORT ***
import com.welltech.db.DatabaseConnection; // Use YOUR DatabaseConnection

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

        // Using try-with-resources is cleaner and ensures resources are closed
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("Setting parameters...");
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Remember to handle hashing in a real app
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhoneNumber());
            pstmt.setString(6, user.getRole().name()); // Use name() for enum

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

            if (e.getErrorCode() == 1062) {
                System.err.println("Duplicate entry error - username or email already exists");
            } else if (e.getErrorCode() == 1045) {
                System.err.println("Access denied - check your MySQL credentials");
            } else if (e.getErrorCode() == 1049) {
                System.err.println("Unknown database - make sure the database exists");
            } else if (e.getErrorCode() == 0) {
                System.err.println("Connection error - ensure MySQL is running");
            }
            // Don't return -1 inside catch if you rethrow, but here it's a DAO method indicating failure
        } // try-with-resources handles closing conn and pstmt

        System.err.println("User insertion failed");
        return -1;
    }

    /**
     * Get a user by their ID
     * @param id User ID
     * @return User object if found, null otherwise
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs); // Use the helper method
                }
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
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs); // Use the helper method
                }
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
                User user = extractUserFromResultSet(rs); // Use the helper method
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
        String sql = "UPDATE users SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhoneNumber());
            pstmt.setInt(4, user.getId());

            int rowsAffected = pstmt.executeUpdate();

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
            // No print stack trace here for simple delete failure, just error message
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
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPassword); // In a real app, hash the password
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to extract a User object from a ResultSet
     * Ensures correct mapping order to the User constructor.
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        // Create User using the constructor that matches the mapping order
        // Constructor: public User(int id, String username, String password, String fullName, String email, String phoneNumber, UserRole role)
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"), // Sensitive data, handle with care
                rs.getString("full_name"),
                rs.getString("email"), // Order matches constructor
                rs.getString("phone_number"), // Order matches constructor
                User.UserRole.valueOf(rs.getString("role"))
        );
    }

    /**
     * Retrieves a list of users based on their role.
     * Corrected method - removed duplicate definition.
     */
    public List<User> getUsersByRole(User.UserRole role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, full_name, email, phone_number, role FROM users WHERE role = ?";

        try (Connection conn = DatabaseConnection.getConnection(); // Use your DB connection class
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.name());
            System.out.println("Executing query in UserDAO: " + sql + " with role: " + role.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs)); // Use the helper method
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting users by role " + role + ": " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("UserDAO found " + users.size() + " users for role " + role.name()); // Debug print
        return users;
    }
    // === REMOVED THE DUPLICATE getUsersByRole METHOD THAT WAS HERE ===
}