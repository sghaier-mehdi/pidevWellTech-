package com.welltech.model;

import java.time.LocalDateTime;

public class Reponse {

    private int id;
    private String contenu;
    private LocalDateTime dateReponse;
    private int reclamationId;

    public Reponse(String contenu, LocalDateTime dateReponse, int reclamationId) {
        this.contenu = contenu;
        this.dateReponse = dateReponse;
        this.reclamationId = reclamationId;
    }

    public Reponse() {

    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateReponse() { return dateReponse; }
    public void setDateReponse(LocalDateTime dateReponse) { this.dateReponse = dateReponse; }

    public int getReclamationId() { return reclamationId; }
    public void setReclamationId(int reclamationId) { this.reclamationId = reclamationId; }
}
