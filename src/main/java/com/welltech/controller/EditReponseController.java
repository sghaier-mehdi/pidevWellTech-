package com.welltech.controller;

import com.welltech.dao.NotificationDAO;
import com.welltech.dao.ReclamationDAO;
import com.welltech.dao.ReponseDAO;
import com.welltech.model.Reclamation;
import com.welltech.model.Reponse;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class EditReponseController {

    @FXML
    private TextArea contenuField;

    private final ReponseDAO dao = new ReponseDAO();
    private Reponse currentReponse;

    // This method is called when the "EditReponse" screen is initialized
    public void setReponse(Reponse reponse) {
        this.currentReponse = reponse;
        contenuField.setText(reponse.getContenu()); // Set old content into the TextArea
    }

    @FXML
    public void updateReponse() {
        String nouveauContenu = contenuField.getText().trim();
        if (nouveauContenu.isEmpty()) {
            showAlert("Le contenu ne peut pas être vide.");
            return;
        }

        currentReponse.setContenu(nouveauContenu);
        currentReponse.setDateReponse(LocalDateTime.now());

        boolean success = dao.updateReponse(currentReponse);
        if (success) {
            NotificationDAO notificationDAO = new NotificationDAO();
            ReclamationDAO reclamationDAO = new ReclamationDAO(); // Fetch the related reclamation
            Reclamation reclamation = reclamationDAO.getReclamationById(currentReponse.getReclamationId());

            if (reclamation != null) {
                int userIdToNotify = reclamation.getUserId();
                String message = "The response to your reclamation '" + reclamation.getTitre() + "' has been updated by an administrator.";
                // Notify the user who owns the reclamation
                notificationDAO.notifyUser(userIdToNotify, message, reclamation.getId());}
            else {
                System.err.println("Could not find Reclamation with ID: " + currentReponse.getReclamationId() + " to notify user about response update.");
            }

            showAlert("Réponse mise à jour avec succès.");
            ((Stage) contenuField.getScene().getWindow()).close();
        } else {
            showAlert("Échec de la mise à jour.");
        }
    }

    @FXML
    private void annuler() {
        ((Stage) contenuField.getScene().getWindow()).close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
