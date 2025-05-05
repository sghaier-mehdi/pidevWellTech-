package com.welltech.controller;

import com.welltech.dao.NotificationDAO;
import com.welltech.dao.ReclamationDAO;
import com.welltech.dao.UserDAO;
import com.welltech.model.Reclamation;
import com.welltech.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class EditReclamationController {

    @FXML private TextField titreField;
    @FXML private TextArea contenuArea;
    @FXML private ComboBox<String> typeReclamationComboBox;
    @FXML private ImageView emoji1, emoji2, emoji3;
    @FXML private CheckBox urgentCheckBox;
    @FXML private CheckBox followUpCheckBox;
    @FXML private Label statusLabel;
    @FXML private Label errorMessage; // Added for error display

    private final ReclamationDAO dao = new ReclamationDAO();
    private Reclamation reclamationToEdit;
    private String selectedSentiment = "";

    public void setReclamation(Reclamation r) {
        this.reclamationToEdit = r;
        titreField.setText(r.getTitre());
        contenuArea.setText(r.getContenu());
        selectedSentiment = r.getSentiment(); // Set the initial sentiment

        // Pre-set the sentiment emoji based on the existing sentiment
        setSentimentEmoji(selectedSentiment);

        // Set other fields like type, urgency, and follow-up
        typeReclamationComboBox.setValue(r.getTypeReclamation());
        urgentCheckBox.setSelected(r.isUrgent());
        followUpCheckBox.setSelected(r.isFollowUp());
    }

    @FXML
    private void initialize() {
        // Load the images
        Image grayAngry = new Image(getClass().getResource("/images/angrygr.png").toString());
        Image grayExcited = new Image(getClass().getResource("/images/exgr.png").toString());
        Image grayThinking = new Image(getClass().getResource("/images/thinkinggr.png").toString());

        Image yellowAngry = new Image(getClass().getResource("/images/angry.png").toString());
        Image yellowExcited = new Image(getClass().getResource("/images/excited.png").toString());
        Image yellowThinking = new Image(getClass().getResource("/images/thinking.png").toString());

        // Set the default gray images on all emojis
        emoji1.setImage(grayAngry);
        emoji2.setImage(grayExcited);
        emoji3.setImage(grayThinking);

        // Add the mouse enter/exit effects for hover
        emoji1.setOnMouseEntered(event -> {
            if (!selectedSentiment.equals("Frustré")) emoji1.setImage(yellowAngry);
        });
        emoji1.setOnMouseExited(event -> {
            if (!selectedSentiment.equals("Frustré")) emoji1.setImage(grayAngry);
        });

        emoji2.setOnMouseEntered(event -> {
            if (!selectedSentiment.equals("Satisfait")) emoji2.setImage(yellowExcited);
        });
        emoji2.setOnMouseExited(event -> {
            if (!selectedSentiment.equals("Satisfait")) emoji2.setImage(grayExcited);
        });

        emoji3.setOnMouseEntered(event -> {
            if (!selectedSentiment.equals("Neutre")) emoji3.setImage(yellowThinking);
        });
        emoji3.setOnMouseExited(event -> {
            if (!selectedSentiment.equals("Neutre")) emoji3.setImage(grayThinking);
        });

        // Set mouse click handlers for selecting sentiments
        emoji1.setOnMouseClicked(event -> selectSentiment("Frustré"));
        emoji2.setOnMouseClicked(event -> selectSentiment("Satisfait"));
        emoji3.setOnMouseClicked(event -> selectSentiment("Neutre"));
    }

    private void selectSentiment(String sentiment) {
        // Update the selected sentiment
        this.selectedSentiment = sentiment;

        // Set the images based on selected sentiment
        emoji1.setImage(sentiment.equals("Frustré") ? new Image(getClass().getResource("/images/angry.png").toString()) : new Image(getClass().getResource("/images/angrygr.png").toString()));
        emoji2.setImage(sentiment.equals("Satisfait") ? new Image(getClass().getResource("/images/excited.png").toString()) : new Image(getClass().getResource("/images/exgr.png").toString()));
        emoji3.setImage(sentiment.equals("Neutre") ? new Image(getClass().getResource("/images/thinking.png").toString()) : new Image(getClass().getResource("/images/thinkinggr.png").toString()));
    }
    private void setSentimentEmoji(String sentiment) {
        // Load the images for each sentiment
        Image grayAngry = new Image(getClass().getResource("/images/angrygr.png").toString());
        Image grayExcited = new Image(getClass().getResource("/images/exgr.png").toString());
        Image grayThinking = new Image(getClass().getResource("/images/thinkinggr.png").toString());

        Image yellowAngry = new Image(getClass().getResource("/images/angry.png").toString());
        Image yellowExcited = new Image(getClass().getResource("/images/excited.png").toString());
        Image yellowThinking = new Image(getClass().getResource("/images/thinking.png").toString());

        // Set the images based on the sentiment
        if ("Frustré".equals(sentiment)) {
            emoji1.setImage(yellowAngry);
            emoji2.setImage(grayExcited);
            emoji3.setImage(grayThinking);
        } else if ("Satisfait".equals(sentiment)) {
            emoji1.setImage(grayAngry);
            emoji2.setImage(yellowExcited);
            emoji3.setImage(grayThinking);
        } else if ("Neutre".equals(sentiment)) {
            emoji1.setImage(grayAngry);
            emoji2.setImage(grayExcited);
            emoji3.setImage(yellowThinking);
        } else {
            // Set all to gray if no sentiment is selected
            emoji1.setImage(grayAngry);
            emoji2.setImage(grayExcited);
            emoji3.setImage(grayThinking);
        }
    }

    @FXML
    private void enregistrerModification() {
        if (reclamationToEdit == null) return;

        String titre = titreField.getText();
        String contenu = contenuArea.getText();
        String typeReclamation = typeReclamationComboBox.getValue();
        boolean isUrgent = urgentCheckBox.isSelected();
        boolean wantsFollowUp = followUpCheckBox.isSelected();

        if (titre.isBlank() || contenu.isBlank() || selectedSentiment.isEmpty() || typeReclamation == null) {
            errorMessage.setVisible(true); // Show error message if fields are empty
            return;
        }

        // Update the reclamation object
        reclamationToEdit.setTitre(titre);
        reclamationToEdit.setContenu(contenu);
        reclamationToEdit.setSentiment(selectedSentiment);
        reclamationToEdit.setTypeReclamation(typeReclamation);
        reclamationToEdit.setUrgent(isUrgent);
        reclamationToEdit.setFollowUp(wantsFollowUp);

        // Update the reclamation in the database
        boolean updated = dao.updateReclamation(reclamationToEdit);
        if (updated) {
            statusLabel.setText("Réclamation mise à jour !");
            NotificationDAO notificationDAO = new NotificationDAO();
            UserDAO userDAO = new UserDAO(); // To get username if needed
            User user = userDAO.getUserById(reclamationToEdit.getUserId()); // Get the user who owns it
            String username = (user != null) ? user.getUsername() : "ID: " + reclamationToEdit.getUserId();

            String message = "Reclamation '" + reclamationToEdit.getTitre() +  ") updated by user " + username + ".";
            // Notify all admins
            notificationDAO.notifyAdmins(message, reclamationToEdit.getId());
            ((Stage) titreField.getScene().getWindow()).close();
        } else {
            statusLabel.setText("Erreur de mise à jour.");
        }
    }

    @FXML
    private void annulerModification() {
        // Close the current window (the "Edit" window)
        ((Stage) titreField.getScene().getWindow()).close();
    }
}
