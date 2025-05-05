package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.NotificationDAO;
import com.welltech.dao.ReclamationDAO;
import com.welltech.dao.ReponseDAO;
import com.welltech.dao.UserDAO;
import com.welltech.model.Reclamation;
import com.welltech.model.Reponse;
import com.welltech.model.User;
import com.welltech.service.SmsService;
// UserDAO and User should already be imported for other parts
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminReclamationController {
    @FXML private DatePicker dateFilterPicker;
    @FXML private CheckBox filtrerUrgent;
    @FXML private VBox listeReclamationsBox;
    @FXML private CheckBox filtrerSansReponse;
    @FXML private Button refreshButton;
    @FXML private Button homeButton;
    @FXML private Label noReclamationsMessage;
    private final UserDAO userDAO = new UserDAO();


    private final ReclamationDAO reclamationDAO = new ReclamationDAO();
    private final ReponseDAO reponseDAO = new ReponseDAO();

    @FXML
    public void initialize() {
        loadAdminReclamations();

        refreshButton.setOnAction(e -> loadAdminReclamations());
        homeButton.setOnAction(e -> retourDashboard());

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
    private void loadAdminReclamations() {
        listeReclamationsBox.getChildren().clear();
        noReclamationsMessage.setVisible(false);

        List<Reclamation> allReclamations = reclamationDAO.getAllReclamations();
        afficherReclamations(allReclamations);
    }

    @FXML
    private void appliquerFiltres() {
        LocalDate selectedDate = dateFilterPicker.getValue();
        boolean sansReponse = filtrerSansReponse.isSelected();
        boolean seulementUrgentes = filtrerUrgent.isSelected(); // <-- Get state of new checkbox

        List<Reclamation> filtered = reclamationDAO.getAllReclamations().stream()
                .filter(rec -> {
                    // Date filter (match if date null OR date equals reclamation date)
                    boolean matchDate = (selectedDate == null) ||
                            (rec.getDateCreation() != null && rec.getDateCreation().isEqual(selectedDate));

                    // No-response filter (match if checkbox unchecked OR if checked AND no response exists)
                    boolean matchSansReponse = !sansReponse ||
                            (reponseDAO.getReponseByReclamationId(rec.getId()) == null);

                    // Urgent filter (match if checkbox unchecked OR if checked AND reclamation is urgent)
                    boolean matchUrgent = !seulementUrgentes || rec.isUrgent(); // <-- Add urgent condition

                    // Reclamation must match ALL active filters
                    return matchDate && matchSansReponse && matchUrgent; // <-- Include matchUrgent
                })
                .collect(Collectors.toList());

        afficherReclamations(filtered); // Display the filtered results
    }
    @FXML
    private void filtrerParDate() {
        appliquerFiltres();
    }

    private void afficherReclamations(List<Reclamation> reclamations) {
        listeReclamationsBox.getChildren().clear();
        if (reclamations.isEmpty()) {
            noReclamationsMessage.setVisible(true);
        } else {
            noReclamationsMessage.setVisible(false);
            for (Reclamation rec : reclamations) {
                listeReclamationsBox.getChildren().add(createReclamationCardAdmin(rec));
            }
        }
    }

    private VBox createReclamationCardAdmin(Reclamation rec) {
        VBox card = new VBox();
        card.setSpacing(5); // Your spacing
        card.setPadding(new Insets(10)); // Your padding
        // --- YOUR ORIGINAL CARD STYLE ---
        card.setStyle("-fx-background-color: #ffe6cc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // --- Original Fields ---
        Label titreLabel = new Label("✎ " + rec.getTitre());
        titreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;"); // Your style

        Label contenuLabel = new Label("✉ " + rec.getContenu());
        contenuLabel.setStyle("-fx-text-fill: #444;"); // Your style
        contenuLabel.setWrapText(true); // Good practice

        Label sentimentLabel = new Label("💬 Sentiment : " + rec.getSentiment());
        sentimentLabel.setStyle("-fx-text-fill: #888;"); // Your style

        // --- ADDED FIELDS ---
        // Submitter Info
        // Make sure userDAO (private final UserDAO userDAO = new UserDAO();) is declared and initialized at the class level
        User submitter = userDAO.getUserById(rec.getUserId());
        String submitterName = (submitter != null) ? submitter.getFullName()  : "Unknown User ";
        Label userLabel = new Label("👤 Par : " + submitterName);
        userLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;"); // Style similar to others

        // Date Submitted
        Label dateLabel = new Label("📅 Le : " + rec.getDateCreation().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        // Type
        Label typeLabel = new Label("🏷️ Type : " + rec.getTypeReclamation());
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        // Urgent
        Label urgentLabel = new Label("❗ Urgent : " + (rec.isUrgent() ? "Oui" : "Non"));
        // Style similar to others, maybe bolder/redder if urgent
        urgentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (rec.isUrgent() ? "#c0392b;" : "#555;") + (rec.isUrgent() ? " -fx-font-weight: bold;" : ""));

        // Follow-up
        Label followUpLabel = new Label("➡️ Suivi demandé : " + (rec.isFollowUp() ? "Oui" : "Non"));
        followUpLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        // Separator before buttons
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(5, 0, 5, 0));
        // --- END ADDED FIELDS ---


        // --- Buttons ---
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_LEFT); // Your alignment
        buttonsBox.setPadding(new Insets(5, 0, 0, 0)); // Top padding for button box

        Button deleteBtn = new Button("🗑 Supprimer Réclamation");
        deleteBtn.setStyle("-fx-background-color: #ff9999;"); // Your style
        deleteBtn.setOnAction(e -> {
            // Added Confirmation Dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette réclamation et sa réponse éventuelle ?", ButtonType.YES, ButtonType.NO);
            confirmAlert.setHeaderText("Confirmer Suppression");
            confirmAlert.setTitle("Confirmer");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    int userIdToNotify = rec.getUserId();       // Get ID before deleting
                    String reclamationTitle = rec.getTitre(); // Get title before deleting
                    int reclamationId = rec.getId();         // Get reclamation ID before deleting

                    // --- Notify User using NotificationDAO DIRECTLY ---
                    // Create an instance of NotificationDAO (as in your original code)
                    NotificationDAO notificationDAO = new NotificationDAO(); // Make sure NotificationDAO is imported
                    String message = "Votre réclamation '" + reclamationTitle + ") a été supprimée par un administrateur.";
                    // Call the DAO method directly
                    notificationDAO.notifyUser(userIdToNotify, message, null);
                    // +++ Add SMS Logic for Deletion +++
                    try {
                        System.out.println("Attempting to send SMS notification for deletion...");
                        // UserDAO instance should already exist at class level
                        SmsService smsService = new SmsService();

                        User targetUser = userDAO.getUserById(userIdToNotify);

                        if (targetUser != null && targetUser.getPhoneNumber() != null && !targetUser.getPhoneNumber().isBlank()) {
                            String userPhoneNumber = targetUser.getPhoneNumber(); // Assumes E.164
                            if (!userPhoneNumber.matches("^\\+216\\d{8}$")) {
                                System.err.println("SMS Skipped: User phone number ["+userPhoneNumber+"] not in expected +216 format.");
                            } else {
                                String smsMessage = "WellTech: Votre réclamation '" + reclamationTitle + "' a été rejetée par un administrateur.";
                                System.out.println("Target phone number (from DB): [" + userPhoneNumber + "]");
                                boolean smsSent = smsService.sendSms(userPhoneNumber, smsMessage);
                                if (smsSent) {
                                    System.out.println("SMS notification for deletion requested successfully to user ID: " + userIdToNotify);
                                } else {
                                    System.err.println("SMS notification request for deletion FAILED to send to user ID: " + userIdToNotify + ". Check SmsService logs.");
                                }
                            }
                        } else {
                            System.err.println("Cannot send deletion SMS: User " + userIdToNotify + " not found or has no phone number.");
                        }
                    } catch (Exception ex_sms) {
                        System.err.println("Error during post-deletion SMS processing for user ID " + userIdToNotify + ": " + ex_sms.getMessage());
                        ex_sms.printStackTrace();
                    }
// +++ End SMS Logic +++// Pass null for related ID as it's deleted
                    // --- End Notification ---

                    // --- Perform Deletion ---
                    // This assumes reclamationDAO.deleteReclamation handles deletion
                    // of related responses/notifications via DB cascades or internal logic.
                    reclamationDAO.deleteReclamation(reclamationId);

                    // Refresh the admin's view
                    appliquerFiltres(); // Or loadAdminReclamations()
                }
            });
        }); // End of deleteBtn setOnAction

        Button respondBtn = new Button("💬 Répondre");
        respondBtn.setStyle("-fx-background-color: #b3d9ff;"); // Your style
        respondBtn.setOnAction(e -> {
            System.out.println("Respond button clicked for reclamation ID: " + rec.getId());
            openResponseForm(rec);
        });

        // Check if response exists
        Reponse reponse = reponseDAO.getReponseByReclamationId(rec.getId()); // Make sure reponseDAO is initialized

        if (reponse != null) {
            // Response exists: Disable respond, add Edit/Delete Response buttons
            respondBtn.setDisable(true);
            respondBtn.setText("Répondu");
            respondBtn.setStyle("-fx-background-color: #d1e7dd; -fx-text-fill: #146c43; -fx-opacity: 0.8;"); // Greenish disabled look

            Button deleteResponseBtn = new Button("❌ Supprimer Réponse");
            deleteResponseBtn.setStyle("-fx-background-color: #ff6666;"); // Your style
            deleteResponseBtn.setOnAction(e -> {
                Alert confirmRespDel = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la réponse de l'admin ?", ButtonType.YES, ButtonType.NO);
                confirmRespDel.setHeaderText("Confirmer Suppression Réponse");
                confirmRespDel.setTitle("Confirmer");
                confirmRespDel.showAndWait().ifPresent(responseDel -> {
                    if(responseDel == ButtonType.YES) {
                        reponseDAO.deleteReponse(reponse.getId());
                        appliquerFiltres(); // Refresh view
                    }
                });
            });

            Button editResponseBtn = new Button("✎ Modifier Réponse");
            editResponseBtn.setStyle("-fx-background-color: #ffff99;"); // Your style
            editResponseBtn.setOnAction(e -> editResponse(reponse));

            // Add all buttons: Delete Rec, Respond (disabled), Delete Resp, Edit Resp
            buttonsBox.getChildren().addAll(deleteBtn, respondBtn, deleteResponseBtn, editResponseBtn);
        } else {
            // No response exists: Add only Delete Rec and Respond buttons
            buttonsBox.getChildren().addAll(deleteBtn, respondBtn);
        }
        // --- End Buttons ---


        // Add all components to the card, including the new ones and separator
        card.getChildren().addAll(
                titreLabel,
                contenuLabel,
                sentimentLabel,
                userLabel,        // Added
                dateLabel,        // Added
                typeLabel,        // Added
                urgentLabel,      // Added
                followUpLabel,    // Added
                separator,        // Added
                buttonsBox
        );

        return card;
    }
    private void retourDashboard() {
        WellTechApplication.loadFXML("adminDashboard");
    }

    private void openResponseForm(Reclamation reclamation) {
        try {

            // Load the FXML file for the response form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReponseForm.fxml"));
            Parent root = loader.load();
            if (root != null) {
                System.out.println("FXML loaded successfully");
            }


            // Get the controller for the response form
            ReponseFormController controller = loader.getController();
            System.out.println("Reclamation ID: " + reclamation.getId());
            System.out.println("Reclamation Title: " + reclamation.getTitre());


            // Pass the specific Reclamation to the response form controller
            controller.setReclamation(reclamation);

            // Create a new stage and show the response form
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Réponse");
            stage.show();

            System.out.println("Response form stage shown for Reclamation ID: " + reclamation.getId());
        } catch (IOException e) {
            System.err.println("Error opening response form: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void editResponse(Reponse reponse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditReponse.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la fenêtre de modification
            EditReponseController controller = loader.getController();

            // Passer la réponse existante au formulaire pour pré-remplir les champs
            controller.setReponse(reponse);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Réponse");
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du formulaire de modification : " + e.getMessage());
            e.printStackTrace();
        }
    }


}
