package com.welltech.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Defi {
    private int id;
    private String titre;
    private String description;
    private int points;
    private String statut;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String conditions;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Defi() {}

    public Defi(String titre, String description, int points, String statut, LocalDate dateDebut, LocalDate dateFin, String conditions, String type) {
        this.titre = titre;
        this.description = description;
        this.points = points;
        this.statut = statut;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.conditions = conditions;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Defi(int id ,String titre, String description, int points, String statut, LocalDate dateDebut, LocalDate dateFin, String conditions, String type) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.points = points;
        this.statut = statut;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.conditions = conditions;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Defi{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", points=" + points +
                ", statut='" + statut + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", conditions='" + conditions + '\'' +
                ", type='" + type + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}