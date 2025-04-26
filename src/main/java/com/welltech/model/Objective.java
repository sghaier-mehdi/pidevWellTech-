package com.welltech.model;

import java.time.LocalDateTime;

public class Objective {
    private int id;
    private String title;
    private String description;
    private int pointsRequired;
    private int userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Objective() {}

    public Objective(String title, String description, int pointsRequired, int userId) {
        this.title = title;
        this.description = description;
        this.pointsRequired = pointsRequired;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Objective(int id,String title, String description, int pointsRequired, int userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.pointsRequired = pointsRequired;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPointsRequired() { return pointsRequired; }
    public void setPointsRequired(int pointsRequired) { this.pointsRequired = pointsRequired; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Objective{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", pointsRequired=" + pointsRequired +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}