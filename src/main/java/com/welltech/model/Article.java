package com.welltech.model; // Ensure this package matches yours

import java.time.LocalDateTime;
import java.util.Objects; // Import Objects for equals/hashCode

public class Article {
    private int id;
    private String title;
    private String content;
    private int authorId;       // Keep author ID for linking
    private String authorName;  // Keep for display convenience
    private Category category;  // *** CHANGED: Use Category object ***
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isPublished;
    private String imageUrl; // Optional: Add if you plan to use images on cards

    // Default constructor
    public Article() {
    }

    // Constructor often used when CREATING a new article before saving
    public Article(String title, String content, int authorId, Category category, boolean isPublished, String imageUrl) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.category = category;
        this.isPublished = isPublished;
        this.imageUrl = imageUrl;
        // createdAt/updatedAt are usually set by the database or DAO upon insert/update
    }

    // Full constructor - often used when FETCHING from database
    public Article(int id, String title, String content, int authorId, String authorName,
                   Category category, LocalDateTime createdAt, LocalDateTime updatedAt,
                   boolean isPublished, String imageUrl) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.category = category; // *** CHANGED ***
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isPublished = isPublished;
        this.imageUrl = imageUrl;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public Category getCategory() { return category; } // *** CHANGED ***
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public boolean isPublished() { return isPublished; }
    public String getImageUrl() { return imageUrl; }


    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setCategory(Category category) { this.category = category; } // *** CHANGED ***
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setPublished(boolean published) { isPublished = published; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }


    // Optional: toString for debugging
    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", authorId=" + authorId +
                ", category=" + (category != null ? category.getName() : "null") + // Display category name
                ", createdAt=" + createdAt +
                ", isPublished=" + isPublished +
                '}';
    }

    // Optional but recommended: equals and hashCode if you use Articles in Sets or need accurate comparisons
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return id == article.id; // Compare primarily by ID if it's set (>0)
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Use ID for hash code
    }
}