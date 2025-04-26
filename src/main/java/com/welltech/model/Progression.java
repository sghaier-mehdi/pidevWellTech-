package com.welltech.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Progression {
    private int id;
    private int userId;
    private int defiId;
    private String statut;
    private double progression;
    private LocalDate dateCompletion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Progression() {}

    public Progression(int id, int userId, int defiId, String statut, double progression) {
        this.id = id;
        this.userId = userId;
        this.defiId = defiId;
        this.statut = statut;
        this.progression = progression;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Progression(int userId, int defiId, String statut, double progression) {
        this.userId = userId;
        this.defiId = defiId;
        this.statut = statut;
        this.progression = progression;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getDefiId() { return defiId; }
    public void setDefiId(int defiId) { this.defiId = defiId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public double getProgression() { return progression; }
    public void setProgression(double progression) { this.progression = progression; }

    public LocalDate getDateCompletion() { return dateCompletion; }
    public void setDateCompletion(LocalDate dateCompletion) { this.dateCompletion = dateCompletion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Progression{" +
                "id=" + id +
                ", userId=" + userId +
                ", defiId=" + defiId +
                ", statut='" + statut + '\'' +
                ", progression=" + progression +
                ", dateCompletion=" + dateCompletion +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}