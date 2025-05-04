package com.welltech.service;

public interface NotificationManager {
    /**
     * Sends a notification to a specific user or globally
     * @param message The notification message
     * @param userId The user ID to send the notification to, or null/empty for global notification
     * @return true if the notification was sent successfully, false otherwise
     */
    boolean sendNotification(String message, String userId);
    
    /**
     * Sends a global notification to all users
     * @param message The notification message
     * @return true if the notification was sent successfully, false otherwise
     */
    boolean sendGlobalNotification(String message);
} 