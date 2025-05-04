package com.welltech.model;

import java.time.LocalDateTime;

/**
 * Objective model class representing an objective in the system
 */
public class Objective {
    private int id;
    private String title;
    private String description;
    private int pointsRequired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer userId; // Nullable, matches the database schema
    
    // Default constructor
    public Objective() {
    }
    
    // Constructor without ID and timestamps (for creating new objectives)
    public Objective(String title, String description, int pointsRequired, Integer userId) {
        this.title = title;
        this.description = description;
        this.pointsRequired = pointsRequired;
        this.userId = userId;
    }
    
    // Full constructor
    public Objective(int id, String title, String description, int pointsRequired, 
                    LocalDateTime createdAt, LocalDateTime updatedAt, Integer userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.pointsRequired = pointsRequired;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getPointsRequired() {
        return pointsRequired;
    }
    
    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return "Objective{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", pointsRequired=" + pointsRequired +
                ", userId=" + userId +
                '}';
    }
}