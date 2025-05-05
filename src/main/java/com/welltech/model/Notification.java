package com.welltech.model;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId; // User to be notified
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private Integer relatedReclamationId; // Use Integer to allow null

    // Constructors
    public Notification() {
    }

    public Notification(int userId, String message, Integer relatedReclamationId) {
        this.userId = userId;
        this.message = message;
        this.isRead = false; // Default to unread
        this.relatedReclamationId = relatedReclamationId;
        // createdAt is usually set by the database default
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getRelatedReclamationId() { return relatedReclamationId; }
    public void setRelatedReclamationId(Integer relatedReclamationId) { this.relatedReclamationId = relatedReclamationId; }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", message='" + message + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                ", relatedReclamationId=" + relatedReclamationId +
                '}';
    }
}