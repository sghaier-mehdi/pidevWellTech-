package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.ReclamationDAO;
import com.welltech.model.Reclamation;
import com.welltech.model.User;
import com.welltech.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.welltech.dao.ReponseDAO;      // Added Import
import com.welltech.model.Reponse;        // Added Import
import java.time.format.DateTimeFormatter; // Added Import

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class UserReclamationsController {
    @FXML private DatePicker dateFilterPicker;
    @FXML private VBox listeReclamationsBox;
    @FXML private Button refreshButton;
    @FXML private Button homeButton;

    private final ReclamationDAO reclamationDAO = new ReclamationDAO();
    private final ReponseDAO reponseDAO = new ReponseDAO(); // DAO instance is already here
    private int userId;

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getId();
            loadUserReclamations();
            refreshButton.setOnAction(e -> loadUserReclamations());
            homeButton.setOnAction(e -> goToHome());
        } else {
            System.out.println("No user is logged in.");
            // Consider disabling UI elements if no user logged in
        }
        // DatePicker setup remains the same
        dateFilterPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });
    }

    @FXML
    private void filtrerParDate() {
        LocalDate selectedDate = dateFilterPicker.getValue();
        LocalDate today = LocalDate.now();

        if (selectedDate != null) {
            if (selectedDate.isAfter(today)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Date invalide");
                alert.setHeaderText(null);
                alert.setContentText("Vous ne pouvez pas sélectionner une date future.");
                alert.showAndWait();
                return;
            }
            List<Reclamation> reclamations = reclamationDAO.getReclamationsByUserIdAndDate(userId, selectedDate);
            displayReclamations(reclamations); // Use helper method
        } else {
            // If date is cleared, reload all
            loadUserReclamations();
        }
    }

    public void loadUserReclamations() {
        List<Reclamation> reclamations = reclamationDAO.getReclamationsByUserId(userId);
        displayReclamations(reclamations); // Use helper method
    }

    // Helper method to update the VBox with reclamation cards
    private void displayReclamations(List<Reclamation> reclamations) {
        listeReclamationsBox.getChildren().clear(); // Clear previous items
        if (reclamations.isEmpty()) {
            Label noReclamationsLabel = new Label("Aucune réclamation trouvée pour les critères sélectionnés.");
            noReclamationsLabel.setStyle("-fx-padding: 10px; -fx-font-style: italic;");
            listeReclamationsBox.getChildren().add(noReclamationsLabel);
        } else {
            for (Reclamation rec : reclamations) {
                listeReclamationsBox.getChildren().add(createReclamationCard(rec));
            }
        }
    }
    private VBox createReclamationCard(Reclamation rec) {
        VBox card = new VBox();
        card.setSpacing(5); // Reverted spacing
        card.setPadding(new Insets(10)); // Reverted padding
        // --- YOUR ORIGINAL CARD STYLE ---
        card.setStyle("-fx-background-color: #ffe6cc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // --- Reclamation Details (Your Original Styles) ---
        Label titreLabel = new Label("✎ " + rec.getTitre());
        titreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;"); // Your style

        Label contenuLabel = new Label("✉ " + rec.getContenu());
        contenuLabel.setStyle("-fx-text-fill: #444;"); // Your style
        contenuLabel.setWrapText(true); // Keep wrapping

        Label sentimentLabel = new Label("💬 Sentiment : " + rec.getSentiment());
        sentimentLabel.setStyle("-fx-text-fill: #888;"); // Your style

        // Added submission date display (Optional, keep or remove)
        Label dateLabel = new Label("Soumis le: " + rec.getDateCreation().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;"); // Simple style for date


        // --- Response Section ---
        VBox reponseSection = new VBox(5); // VBox to hold response info
        // Basic padding, no extra background or border unless you want to add specific style
        reponseSection.setPadding(new Insets(8, 0, 5, 0));

        // Fetch the response using the DAO
        Reponse adminReponse = reponseDAO.getReponseByReclamationId(rec.getId());

        if (adminReponse != null) {
            // Response exists, display it using simple styles matching yours
            Label reponseTitleLabel = new Label("Réponse de l'Admin:");
            // Style similar to your title label, maybe different color
            reponseTitleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #006400;"); // Dark Green

            Label reponseContenuLabel = new Label(adminReponse.getContenu());
            reponseContenuLabel.setWrapText(true);
            reponseContenuLabel.setStyle("-fx-text-fill: #444;"); // Match your content style

            // Format the response date/time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'à' HH:mm");
            Label reponseDateLabel = new Label("Répondu le: " + adminReponse.getDateReponse().format(formatter));
            reponseDateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;"); // Match your sentiment style

            reponseSection.getChildren().addAll(reponseTitleLabel, reponseContenuLabel, reponseDateLabel);
        } else {
            // No response exists
            Label noReponseLabel = new Label("⏳ Aucune réponse de l'admin pour le moment.");
            // Simple italic style
            noReponseLabel.setStyle("-fx-font-style: italic; -fx-font-size: 13px; -fx-text-fill: #888;");
            reponseSection.getChildren().add(noReponseLabel);
        }
        // --- End Response Section ---


        // --- Action Buttons (Your Original Styles) ---
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_LEFT); // Your alignment
        buttonsBox.setPadding(new Insets(10, 0, 0, 0)); // Added top padding

        Button deleteBtn = new Button("🗑 Supprimer");
        deleteBtn.setStyle("-fx-background-color: #ff9999;"); // Your style
        deleteBtn.setOnAction(e -> {
            // Confirmation Dialog (Recommended)
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cette réclamation ?", ButtonType.YES, ButtonType.NO);
            confirmAlert.setHeaderText(null);
            confirmAlert.setTitle("Confirmer Suppression");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    reclamationDAO.deleteReclamation(rec.getId());
                    loadUserReclamations(); // Refresh list
                }
            });
        });

        Button editBtn = new Button("✎ Modifier");
        editBtn.setStyle("-fx-background-color: #b3d9ff;"); // Your style
        editBtn.setOnAction(e -> modifyReclamation(rec));

        // Keep logic to disable Edit button if there IS an admin response
        if (adminReponse != null) {
            editBtn.setDisable(true);
            Tooltip tt = new Tooltip("Modification impossible après la réponse de l'admin.");
            Tooltip.install(editBtn, tt); // Use Tooltip.install
            // Optionally change style slightly for disabled state, but keep base color idea
            editBtn.setStyle("-fx-background-color: #d1e7ff; -fx-opacity: 0.7;"); // Lighter blue, slightly transparent
        }


        buttonsBox.getChildren().addAll(deleteBtn, editBtn); // Your button order
        // --- End Action Buttons ---


        // Add all components to the card
        // Added a Separator for visual clarity between reclamation and response
        card.getChildren().addAll(titreLabel, contenuLabel, sentimentLabel, dateLabel, new Separator(javafx.geometry.Orientation.HORIZONTAL), reponseSection, buttonsBox);

        return card;
    }


    private void modifyReclamation(Reclamation rec) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditReclamation.fxml"));
            Parent root = loader.load();

            EditReclamationController controller = loader.getController();
            controller.setReclamation(rec); // Pass the reclamation to modify
            // Optional: Pass reference back if Edit controller needs to call refresh here
            // controller.setCallingController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier Réclamation");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML for EditReclamation: " + e.getMessage());
            e.printStackTrace();
            // Show error Alert
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir la fenêtre de modification.", ButtonType.OK);
            errorAlert.showAndWait();
        }
    }

    // Rewriting switch to be Java 11 compatible (colon and break)
    private void goToHome() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            switch (user.getRole()) {
                case ADMIN:
                    WellTechApplication.loadFXML("adminDashboard");
                    break;
                case PSYCHIATRIST:
                    WellTechApplication.loadFXML("psychiatristDashboard");
                    break;
                case PATIENT:
                    WellTechApplication.loadFXML("patientDashboard");
                    break;
                default:
                    WellTechApplication.loadFXML("login");
                    break;
            }
        } else {
            WellTechApplication.loadFXML("login");
        }
    }
}