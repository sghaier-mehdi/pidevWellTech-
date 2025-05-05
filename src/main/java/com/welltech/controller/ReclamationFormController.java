package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.NotificationDAO;
import com.welltech.dao.ReclamationDAO;
import com.welltech.model.Reclamation;
import javafx.scene.control.Button;
import com.welltech.model.User;
import com.welltech.service.SmsService;
import com.welltech.util.SessionManager; // Use SessionManager consistently
import javafx.event.ActionEvent; // Import ActionEvent if envoyerReclamation takes it
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
// Import Alert classes for better feedback
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.time.LocalDate;

public class ReclamationFormController {

    @FXML private TextField titreField;
    @FXML private TextArea contenuArea;
    @FXML private ComboBox<String> typeReclamationComboBox;
    @FXML private ImageView emoji1, emoji2, emoji3;
    @FXML private CheckBox urgentCheckBox;
    @FXML private CheckBox followUpCheckBox;
    @FXML private Button envoyerButton;
    @FXML private Button retourButton;
    @FXML private Label statusLabel; // Keep or remove based on preference

    // Use final for DAOs initialized once
    private final ReclamationDAO reclamationDAO = new ReclamationDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private String selectedSentiment = "";

    // Store image instances to avoid reloading them repeatedly
    private Image grayAngry, grayExcited, grayThinking;
    private Image yellowAngry, yellowExcited, yellowThinking;


    @FXML
    private void initialize() {
        // Load images once during initialization
        try {
            grayAngry = new Image(getClass().getResourceAsStream("/images/angrygr.png"));
            grayExcited = new Image(getClass().getResourceAsStream("/images/exgr.png"));
            grayThinking = new Image(getClass().getResourceAsStream("/images/thinkinggr.png"));
            yellowAngry = new Image(getClass().getResourceAsStream("/images/angry.png"));
            yellowExcited = new Image(getClass().getResourceAsStream("/images/excited.png"));
            yellowThinking = new Image(getClass().getResourceAsStream("/images/thinking.png"));

            // Set the default gray images on all emojis
            resetEmojiImages();

            // Add the mouse enter/exit effects for hover
            setupHoverEffect(emoji1, yellowAngry, grayAngry, "Frustré");
            setupHoverEffect(emoji2, yellowExcited, grayExcited, "Satisfait");
            setupHoverEffect(emoji3, yellowThinking, grayThinking, "Neutre");

            // Set mouse click handlers for selecting sentiments
            emoji1.setOnMouseClicked(event -> selectSentiment("Frustré"));
            emoji2.setOnMouseClicked(event -> selectSentiment("Satisfait"));
            emoji3.setOnMouseClicked(event -> selectSentiment("Neutre"));

            // Populate ComboBox if needed (example)
            // typeReclamationComboBox.getItems().addAll("Technical Issue", "Billing", "Suggestion", "Other");

        } catch (NullPointerException e) {
            System.err.println("Error loading emoji images. Check file paths in /resources/images/");
            statusLabel.setText("Error loading resources.");
            // Optionally disable emoji functionality if images fail to load
            emoji1.setDisable(true);
            emoji2.setDisable(true);
            emoji3.setDisable(true);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during initialization: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Initialization error.");
        }
    }

    private void setupHoverEffect(ImageView emoji, Image hoverImage, Image defaultImage, String sentiment) {
        emoji.setOnMouseEntered(event -> {
            if (!selectedSentiment.equals(sentiment)) emoji.setImage(hoverImage);
        });
        emoji.setOnMouseExited(event -> {
            if (!selectedSentiment.equals(sentiment)) emoji.setImage(defaultImage);
        });
    }

    private void selectSentiment(String sentiment) {
        this.selectedSentiment = sentiment;
        // Set the images based on selected sentiment
        emoji1.setImage(sentiment.equals("Frustré") ? yellowAngry : grayAngry);
        emoji2.setImage(sentiment.equals("Satisfait") ? yellowExcited : grayExcited);
        emoji3.setImage(sentiment.equals("Neutre") ? yellowThinking : grayThinking);
    }

    private void resetEmojiImages() {
        if (grayAngry != null && grayExcited != null && grayThinking != null) {
            emoji1.setImage(grayAngry);
            emoji2.setImage(grayExcited);
            emoji3.setImage(grayThinking);
        }
        selectedSentiment = ""; // Reset selected sentiment state
    }

    private void clearForm() {
        titreField.clear();
        contenuArea.clear();
        typeReclamationComboBox.setValue(null); // Clear ComboBox selection
        urgentCheckBox.setSelected(false);
        followUpCheckBox.setSelected(false);
        resetEmojiImages(); // Reset emojis to default gray state
        statusLabel.setText(""); // Clear status label
    }

    @FXML
    private void envoyerReclamation() { // Consider adding ActionEvent event if needed by FXML
        // 1. Get data from form fields
        String titre = titreField.getText();
        String contenu = contenuArea.getText();
        String typeReclamation = typeReclamationComboBox.getValue();
        boolean isUrgent = urgentCheckBox.isSelected();
        boolean wantsFollowUp = followUpCheckBox.isSelected();
        LocalDate dateCreation = LocalDate.now();

        // 2. Validate input
        if (titre.isBlank() || contenu.isBlank() || selectedSentiment.isEmpty() || typeReclamation == null) {
            // Use Alert for better feedback
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Information");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in Title, Content, select a Type, and choose a Sentiment.");
            alert.showAndWait();
            // statusLabel.setText("Veuillez remplir tous les champs et sélectionner un sentiment/type.");
            return;
        }

        // 3. Get current user
        User currentUser = SessionManager.getCurrentUser(); // Use SessionManager consistently
        if (currentUser == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Authentication Error");
            alert.setHeaderText(null);
            alert.setContentText("Cannot submit reclamation: No user logged in. Please log in again.");
            alert.showAndWait();
            // statusLabel.setText("Utilisateur non connecté.");
            // Optionally navigate to login screen here
            return;
        }

        // 4. Create Reclamation object
        Reclamation reclamation = new Reclamation();
        reclamation.setTitre(titre);
        reclamation.setContenu(contenu);
        reclamation.setSentiment(selectedSentiment);
        reclamation.setDateCreation(dateCreation); // Use the date variable
        reclamation.setUserId(currentUser.getId());
        reclamation.setTypeReclamation(typeReclamation);
        reclamation.setUrgent(isUrgent);
        reclamation.setFollowUp(wantsFollowUp);

        // 5. Call the MODIFIED addReclamation method - expecting an int ID
        int newReclamationId = reclamationDAO.addReclamation(reclamation); // Capture the returned ID

        // 6. Check the result and notify admins
        if (newReclamationId > 0) { // Check if the returned ID is valid (positive)
            // Success!
            // statusLabel.setText("Réclamation envoyée avec succès !"); // Optional status label update

            // Show success Alert
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Reclamation submitted successfully !");
            successAlert.showAndWait();

            // Notify Admins using the CORRECT ID and existing DAO/User instances
            String message = "New reclamation '" + reclamation.getTitre() + "' submitted by user " + currentUser.getUsername() + ".";
            // Use the existing notificationDAO instance and the retrieved newReclamationId
            this.notificationDAO.notifyAdmins(message, newReclamationId);
            // +++ Add SMS Logic for URGENT Reclamation +++
            if (reclamation.isUrgent()) {
                System.out.println("Urgent reclamation detected, attempting to send SMS to admin...");
                String adminPhoneNumber = System.getenv("ADMIN_SMS_NUMBER"); // Read from Environment Variable

                if (adminPhoneNumber != null && !adminPhoneNumber.isBlank()) {
                    adminPhoneNumber = adminPhoneNumber.trim(); // Trim just in case
                    // Validate format expected for ADMIN number (might be Tunisian or other)
                    if (!adminPhoneNumber.matches("^\\+\\d+$")) { // Basic E.164 check
                        System.err.println("Cannot send urgent SMS: ADMIN_SMS_NUMBER environment variable ["+adminPhoneNumber+"] is not in valid E.164 format (must start with +).");
                    } else {
                        try {
                            SmsService smsService = new SmsService();
                            String smsMessage = "[URGENT] Réclamation WellTech - Titre: '" + reclamation.getTitre() + "' par " + currentUser.getFullName() + ".";
                            System.out.println("Admin phone number (from env var): [" + adminPhoneNumber + "]");
                            boolean smsSent = smsService.sendSms(adminPhoneNumber, smsMessage); // Directly calling sendSms

                            if (smsSent) {
                                System.out.println("URGENT SMS notification request sent successfully to admin number.");
                            } else {
                                System.err.println("URGENT SMS notification request FAILED to send to admin number. Check SmsService logs.");
                            }
                        } catch (Exception ex_sms_admin) {
                            System.err.println("Error during urgent SMS processing: " + ex_sms_admin.getMessage());
                            ex_sms_admin.printStackTrace();
                        }
                    }
                } else {
                    System.err.println("Cannot send urgent SMS: ADMIN_SMS_NUMBER environment variable not set or empty.");
                }
            }
// +++ End SMS Logic for URGENT +++

            // Clear the form for the next entry
            clearForm();

        } else {
            // Failure
            // statusLabel.setText("Erreur lors de l'envoi."); // Optional status label update

            // Show error Alert
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Submission Failed");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Failed to submit reclamation. Database error occurred. Please try again later.");
            errorAlert.showAndWait();
        }
    }
    @FXML
    private void handleRetour(ActionEvent event) {
        System.out.println("Retour button clicked - navigating back.");
        // Get the current user to determine which dashboard to return to
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            switch (currentUser.getRole()) {
                case ADMIN:
                    WellTechApplication.loadFXML("adminDashboard"); // Should admin be submitting? Maybe UserReclamations instead?
                    break;
                case PSYCHIATRIST:
                    WellTechApplication.loadFXML("psychiatristDashboard"); // Or UserReclamations?
                    break;
                case PATIENT:
                    WellTechApplication.loadFXML("patientDashboard"); // Or UserReclamations?
                    break;
                default:
                    WellTechApplication.loadFXML("login"); // Fallback
                    break;
            }
            // --- ALTERNATIVE: Always go back to the Reclamation List view ---
            // WellTechApplication.loadFXML("UserReclamationMenu"); // Or whatever your list view is called
            // --- End Alternative ---

        } else {
            // Fallback if user somehow got here without being logged in
            WellTechApplication.loadFXML("login");
        }
    }
    // --- END ADDED METHOD ---

    // ... other existing methods ...
}
