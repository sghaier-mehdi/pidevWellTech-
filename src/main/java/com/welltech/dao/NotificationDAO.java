package com.welltech.dao;

import com.welltech.db.DatabaseConnection; // Assuming this is your connection class
import com.welltech.model.Notification;
import com.welltech.model.User;
import com.welltech.model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private final UserDAO userDAO = new UserDAO(); // To find admins

    /**
     * Adds a single notification to the database.
     * @param notification The notification object to add.
     * @return true if successful, false otherwise.
     */
    public boolean addNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, message, related_reclamation_id, is_read) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notification.getUserId());
            pstmt.setString(2, notification.getMessage());
            if (notification.getRelatedReclamationId() != null) {
                pstmt.setInt(3, notification.getRelatedReclamationId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setBoolean(4, notification.isRead()); // Should typically be false initially

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates notifications for all admin users.
     * @param message The notification message.
     * @param relatedReclamationId Optional ID of the related reclamation.
     */
    public void notifyAdmins(String message, Integer relatedReclamationId) {
        List<User> admins = userDAO.getAllUsers().stream()
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .toList();

        for (User admin : admins) {
            Notification notification = new Notification(admin.getId(), message, relatedReclamationId);
            addNotification(notification);
            System.out.println("Admin Notification created for user ID: " + admin.getId());
        }
    }

    /**
     * Creates a notification for a specific user.
     * @param userId The ID of the user to notify.
     * @param message The notification message.
     * @param relatedReclamationId Optional ID of the related reclamation.
     */
    public void notifyUser(int userId, String message, Integer relatedReclamationId) {
        // Optional: Check if user exists before creating notification
        // User user = userDAO.getUserById(userId);
        // if (user != null) { ... }

        Notification notification = new Notification(userId, message, relatedReclamationId);
        addNotification(notification);
        System.out.println("User Notification created for user ID: " + userId);
    }


    /**
     * Retrieves all unread notifications for a specific user.
     * @param userId The ID of the user.
     * @return A list of unread Notification objects.
     */
    public List<Notification> getUnreadNotificationsByUserId(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                notifications.add(extractNotificationFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting unread notifications: " + e.getMessage());
            e.printStackTrace();
        }
        return notifications;
    }

    /**
     * Counts unread notifications for a specific user.
     * @param userId The ID of the user.
     * @return The count of unread notifications.
     */
    public int getUnreadNotificationCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        int count = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error counting unread notifications: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Marks a specific notification as read.
     * @param notificationId The ID of the notification to mark as read.
     * @return true if successful, false otherwise.
     */
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marks all notifications for a specific user as read.
     * @param userId The ID of the user whose notifications should be marked as read.
     * @return true if any rows were updated, false otherwise.
     */
    public boolean markAllAsReadByUserId(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0; // Returns number of rows affected

        } catch (SQLException e) {
            System.err.println("Error marking all notifications as read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    private Notification extractNotificationFromResultSet(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        notification.setRelatedReclamationId(rs.getObject("related_reclamation_id", Integer.class)); // Handles NULL
        return notification;
    }
}