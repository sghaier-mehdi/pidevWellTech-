package com.welltech.controller;

import com.welltech.dao.NotificationDAO;
import com.welltech.dao.ReponseDAO;
import com.welltech.dao.UserDAO;
import com.welltech.model.Reclamation;
import com.welltech.model.Reponse;
import com.welltech.model.User;
import com.welltech.service.SmsService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class ReponseFormController {

    @FXML private TextArea contenuReponseArea;
    @FXML private Label statusLabel;

    private Reclamation reclamation;
    private final ReponseDAO dao = new ReponseDAO();
    private Runnable refreshCallback;

    public void setReclamation(Reclamation r) {
        this.reclamation = r;
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @FXML
    private void envoyerReponse() {
        String contenu = contenuReponseArea.getText();

        if (contenu == null || contenu.isBlank()) {
            statusLabel.setText("Le contenu ne peut pas être vide.");
            return;
        }

        Reponse reponse = new Reponse();
        reponse.setContenu(contenu);
        reponse.setDateReponse(LocalDateTime.now());
        reponse.setReclamationId(reclamation.getId());

        boolean success = dao.addReponse(reponse);

        if (success) {
            // +++ Add SMS Logic +++
            try {
                System.out.println("Attempting to send SMS notification for response...");
                UserDAO userDAO = new UserDAO();
                SmsService smsService = new SmsService(); // Create instance

                // Ensure 'reclamation' object is available here
                User targetUser = userDAO.getUserById(reclamation.getUserId());

                if (targetUser != null && targetUser.getPhoneNumber() != null && !targetUser.getPhoneNumber().isBlank()) {
                    String userPhoneNumber = targetUser.getPhoneNumber(); // Assumes E.164 format!
                    // Check format just in case, though service layer also checks
                    if (!userPhoneNumber.matches("^\\+216\\d{8}$")) {
                        System.err.println("SMS Skipped: User phone number ["+userPhoneNumber+"] not in expected +216 format.");
                    } else {
                        String smsMessage = "WellTech:  votre réclamation a été répondue '" + reclamation.getTitre() + "'.";
                        System.out.println("Target phone number (from DB): [" + userPhoneNumber + "]");
                        boolean smsSent = smsService.sendSms(userPhoneNumber, smsMessage);
                        if (smsSent) {
                            System.out.println("SMS notification for response requested successfully to user ID: " + reclamation.getUserId());
                        } else {
                            System.err.println("SMS notification request for response FAILED to send to user ID: " + reclamation.getUserId() + ". Check SmsService logs.");
                        }
                    }
                } else {
                    System.err.println("Cannot send SMS: User " + reclamation.getUserId() + " not found or has no phone number in profile.");
                }
            } catch (Exception e) {
                System.err.println("Error during post-response SMS processing for user ID " + reclamation.getUserId() + ": " + e.getMessage());
                e.printStackTrace();
            }
// +++ End SMS Logic +++
            NotificationDAO notificationDAO = new NotificationDAO();
            // We already have the 'reclamation' object passed to this controller
            int userIdToNotify = reclamation.getUserId();
            String message = "An administrator has responded to your reclamation '" + reclamation.getTitre() + "'.";
            // Notify the user who owns the reclamation
            notificationDAO.notifyUser(userIdToNotify, message, reclamation.getId());

            statusLabel.setText("Réponse envoyée !");
            if (refreshCallback != null) refreshCallback.run();
            ((Stage) contenuReponseArea.getScene().getWindow()).close();
        } else {
            statusLabel.setText("Erreur lors de l'envoi.");
        }
    }
}
