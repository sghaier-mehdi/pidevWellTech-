package com.welltech.model;

import java.time.LocalDateTime;

public class Recompense {
    private int id;
    private String nom;
    private String description;
    private int pointsRequis;
    private String type;
    private String statu;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Recompense() {}

    public Recompense(int id, String nom, String description, int pointsRequis, String type, String statu) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.pointsRequis = pointsRequis;
        this.type = type;
        this.statu = statu;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Recompense( String nom, String description, int pointsRequis, String type, String statu) {
        this.nom = nom;
        this.description = description;
        this.pointsRequis = pointsRequis;
        this.type = type;
        this.statu = statu;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPointsRequis() { return pointsRequis; }
    public void setPointsRequis(int pointsRequis) { this.pointsRequis = pointsRequis; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatu() { return statu; }
    public void setStatu(String statu) { this.statu = statu; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Recompense{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", pointsRequis=" + pointsRequis +
                ", type='" + type + '\'' +
                ", statu='" + statu + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
