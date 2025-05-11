package com.welltech.dao;

import com.welltech.model.User;
import com.welltech.model.UserRole;
// Adjust import if DatabaseConnection is in a different package
import com.welltech.db.DatabaseConnection;
// import com.welltech.util.DatabaseConnection;

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
        // Input validation (basic)
        if (user == null || user.getUsername() == null || user.getPassword() == null || user.getRole() == null) {
            System.err.println("User object or essential fields (username, password, role) are null. Cannot insert.");
            return -1;
        }

        System.out.println("Attempting to insert user: " + user.getUsername());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (conn == null) { System.err.println("DB Connection is null in insertUser."); return -1;}

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Store HASHED password
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhoneNumber()); // Ensure E.164 format
            pstmt.setString(6, user.getRole().name()); // Use enum's name() method

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Insert executed. Affected rows: " + affectedRows);

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs != null && rs.next()) {
                        int id = rs.getInt(1);
                        System.out.println("User inserted with ID: " + id);
                        user.setId(id); // Set the ID back on the original object
                        return id;
                    } else {
                        System.err.println("User insertion succeeded but failed to retrieve generated ID.");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL Error inserting user '" + user.getUsername() + "': " + e.getMessage() + " (SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + ")");
            // Only print stack trace during development/debugging
            // e.printStackTrace();

            if (e.getErrorCode() == 1062) { // Specific code for MySQL duplicate entry
                System.err.println("-> Hint: Duplicate entry error - username or email likely already exists.");
            }
        } catch (Exception e) { // Catch other potential exceptions
            System.err.println("Unexpected error inserting user: " + e.getMessage());
            e.printStackTrace();
        }

        System.err.println("User insertion failed for username: " + user.getUsername());
        return -1;
    }

    /**
     * Get a user by their ID
     * @param id User ID
     * @return User object if found, null otherwise
     */
    public User getUserById(int id) {
        String sql = "SELECT id, username, password, full_name, email, phone_number, role FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) { System.err.println("DB Connection is null in getUserById."); return null;}

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs); // Use the helper method
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error getting user by ID " + id + ": " + e.getMessage());
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
        if (username == null || username.isBlank()) {
            return null; // Cannot search for null/empty username
        }
        String sql = "SELECT id, username, password, full_name, email, phone_number, role FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) { System.err.println("DB Connection is null in getUserByUsername."); return null;}

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs); // Use the helper method
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by username '" + username + "': " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error getting user by username '" + username + "': " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all users from the database
     * @return List of User objects, potentially empty.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, full_name, email, phone_number, role FROM users ORDER BY full_name"; // Added ORDER BY

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (conn == null) { System.err.println("DB Connection is null in getAllUsers."); return users;}


            while (rs.next()) {
                User user = extractUserFromResultSet(rs); // Use the helper method
                if (user != null) { // Check if extraction was successful
                    users.add(user);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error getting all users: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Retrieved " + users.size() + " users.");
        return users;
    }

    /**
     * Update basic user profile information (name, email, phone).
     * Does NOT update password or role via this method.
     * @param user User object with updated info and correct ID.
     * @return true if successful, false otherwise.
     */
    public boolean updateUser(User user) {
        if (user == null || user.getId() <= 0) {
            System.err.println("Cannot update user: User object is null or ID is invalid.");
            return false;
        }
        String sql = "UPDATE users SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) { System.err.println("DB Connection is null in updateUser."); return false;}

            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhoneNumber()); // Ensure E.164 format if setting
            pstmt.setInt(4, user.getId());

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("User update executed for ID " + user.getId() + ". Rows affected: " + rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user ID " + user.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error updating user ID " + user.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a user from the database by ID.
     * Note: Foreign key constraints should handle related data (reclamations, notifications) deletion.
     * @param id User ID
     * @return true if successful, false otherwise.
     */
    public boolean deleteUser(int id) {
        if (id <= 0) {
            System.err.println("Cannot delete user: Invalid ID " + id);
            return false;
        }
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) { System.err.println("DB Connection is null in deleteUser."); return false;}

            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            System.out.println("User delete executed for ID " + id + ". Rows affected: " + affectedRows);
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting user ID " + id + ": " + e.getMessage());
            // e.printStackTrace(); // Only log stack trace if really needed for delete
        } catch (Exception e) {
            System.err.println("Unexpected error deleting user ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update user password in the database.
     * IMPORTANT: The new password should be securely HASHED before calling this method.
     * @param userId User ID
     * @param hashedPassword The new securely hashed password.
     * @return true if the update was successful.
     */
    public boolean updateUserPassword(int userId, String hashedPassword) {
        if (userId <= 0 || hashedPassword == null || hashedPassword.isBlank()) {
            System.err.println("Cannot update password: Invalid user ID or password hash.");
            return false;
        }
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) { System.err.println("DB Connection is null in updateUserPassword."); return false;}

            pstmt.setString(1, hashedPassword); // Store the HASH
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Password update executed for user ID " + userId + ". Rows affected: " + rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating password for user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error updating password for user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to extract a User object from a ResultSet row.
     * Uses setters on a default-constructed object.
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        if (rs == null) return null;

        User user = new User(); // Use default constructor
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password")); // Retrieving hash from DB
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number")); // Retrieve as is

        // Convert role string from DB back to Enum safely
        String roleString = rs.getString("role");
        try {
            if (roleString != null) {
                user.setRole(UserRole.valueOf(roleString.toUpperCase())); // Use separate UserRole enum
            } else {
                System.err.println("Warning: Null role found for user ID: " + user.getId());
                // Handle null role case if necessary (e.g., set a default or throw exception)
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Invalid role string '" + roleString + "' found in database for user ID: " + user.getId());
            // Handle invalid role case (e.g., set null, default, or throw exception)
            user.setRole(null); // Example: set to null if invalid
        }

        return user; // Return the object populated via setters
        // Removed the duplicate return statement that used the constructor
    }

    /**
     * Retrieves a list of users based on their role.
     */
    public List<User> getUsersByRole(UserRole role) {
        List<User> users = new ArrayList<>();
        if (role == null) {
            System.err.println("Cannot get users by role: Role parameter is null.");
            return users; // Return empty list
        }
        // Select specific columns to avoid retrieving potentially sensitive password hash unless needed
        String sql = "SELECT id, username, password, full_name, email, phone_number, role FROM users WHERE role = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) { System.err.println("DB Connection is null in getUsersByRole."); return users;}

            pstmt.setString(1, role.name()); // Convert enum to string for query
            System.out.println("Executing query in UserDAO: getUsersByRole with role: " + role.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = extractUserFromResultSet(rs);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting users by role " + role + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error getting users by role " + role + ": " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("UserDAO found " + users.size() + " users for role " + role.name());
        return users;
    }
}