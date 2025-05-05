package com.welltech.model;

import java.time.LocalDate;

public class Reclamation {
    private int id;
    private String titre;
    private String contenu;
    private LocalDate dateCreation;
    private String sentiment;
    private String typeReclamation;
    private boolean urgent;
    private boolean followUp;
    private int userId;

    // Constructors
    public Reclamation() {}

    public Reclamation(int id, String titre, String contenu, LocalDate dateCreation, String sentiment,
                       String typeReclamation, boolean urgent, boolean followUp, int userId) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.sentiment = sentiment;
        this.typeReclamation = typeReclamation;
        this.urgent = urgent;
        this.followUp = followUp;
        this.userId = userId;
    }

    public Reclamation(String titre, String contenu, LocalDate dateCreation, String sentiment,
                       String typeReclamation, boolean urgent, boolean followUp, int userId) {
        this.titre = titre;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.sentiment = sentiment;
        this.typeReclamation = typeReclamation;
        this.urgent = urgent;
        this.followUp = followUp;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getTypeReclamation() {
        return typeReclamation;
    }

    public void setTypeReclamation(String typeReclamation) {
        this.typeReclamation = typeReclamation;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public boolean isFollowUp() {
        return followUp;
    }

    public void setFollowUp(boolean followUp) {
        this.followUp = followUp;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // toString method
    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", dateCreation=" + dateCreation +
                ", sentiment='" + sentiment + '\'' +
                ", typeReclamation='" + typeReclamation + '\'' +
                ", urgent=" + urgent +
                ", followUp=" + followUp +
                ", userId=" + userId +
                '}';
    }
}
