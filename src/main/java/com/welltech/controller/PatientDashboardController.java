package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.model.User;
import javafx.application.Platform;
import com.welltech.service.NotificationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.welltech.util.SessionManager;
import javafx.stage.Popup;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import com.welltech.dao.NotificationDAO; // Import NotificationDAO
import com.welltech.model.Notification; // Import Notification model


/**
 * Controller for the patient dashboard
 */
public class PatientDashboardController implements Initializable {

    @FXML private Button notificationButton; // Button that triggers notification popup
    @FXML private VBox notificationsBox;


    @FXML
    private Label userNameLabel;
    @FXML
    private Button reclamationsButton;
    

    @FXML
    private Button logoutButton;

    @FXML
    private Button dashboardButton;

    // --- Add this FXML injection for the Appointments button ---
    @FXML
    private Button appointmentsButton;
    // ----------------------------------------------------------
    @FXML private Button chatbotButton;
    @FXML
    private Button historyButton; // Assuming this exists in your FXML
    // Note: Articles button seems missing in your patientDashboard.fxml based on snippets,
    //       but if it exists, add its FXML injection too.
    // @FXML private Button articlesButton;

    @FXML
    private Button messagesButton; // Assuming this exists in your FXML

    @FXML
    private Button profileButton;
    private final NotificationDAO notificationDAO = new NotificationDAO(); // Instantiate DAO
    private User currentUser;
    private Popup notificationPopup; // Popup window for notifications
    private Label notificationCountLabel; // Label to show the count on the button
    

    @FXML
    private Button notificationsButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initializing PatientDashboardController");
            // Get current user
             currentUser = LoginController.getCurrentUser();
            
            User currentUser = LoginController.getCurrentUser();

            if (currentUser != null) {
                // Update welcome label with user's name
                userNameLabel.setText("Welcome, " + currentUser.getFullName());
                setupNotificationButton(); // Setup notification button and count
                loadNotifications(); // Initial load of notification count
            } else {
                System.err.println("No user is logged in");
                notificationButton.setDisable(true);
                // Redirect to login if no user
                WellTechApplication.loadFXML("login");
                return; // Stop initialization
            }

            // Set dashboard button as active when on the dashboard view
            // Ensure other buttons like 'appointmentsButton' do *not* have the 'active' class here
            dashboardButton.getStyleClass().add("active");
            // Make sure to remove 'active' from other buttons if they might have it from a previous view
            if (appointmentsButton != null) appointmentsButton.getStyleClass().remove("active");
            // ... remove 'active' from other buttons like historyButton, messagesButton, profileButton


            // Initialize dashboard content (e.g., summary stats - not implemented yet)

            System.out.println("PatientDashboardController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing PatientDashboardController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void setupNotificationButton() {
        // Create a container (HBox) for the icon and count label
        HBox buttonGraphic = new HBox(5); // 5px spacing
        buttonGraphic.setAlignment(Pos.CENTER);

        // You might use a real icon here, for now using text
        Label iconLabel = new Label("🔔"); // Or load an Image/ImageView
        iconLabel.setStyle("-fx-font-size: 16px;");

        notificationCountLabel = new Label("0"); // Initialize count label
        notificationCountLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 1 4 1 4;");
        notificationCountLabel.setVisible(false); // Hide if count is 0

        buttonGraphic.getChildren().addAll(iconLabel, notificationCountLabel);
        notificationButton.setGraphic(buttonGraphic);
        notificationButton.setText(""); // Remove text if you only want the graphic

        // Action to show notifications
        notificationButton.setOnAction(this::handleNotificationClick);

        // Initialize the popup
        createNotificationPopup();
    }

    private void loadNotifications() {
        if (currentUser == null) return;

        int unreadCount = notificationDAO.getUnreadNotificationCount(currentUser.getId());
        Platform.runLater(() -> { // Ensure UI updates happen on the JavaFX Application Thread
            if (unreadCount > 0) {
                notificationCountLabel.setText(String.valueOf(unreadCount));
                notificationCountLabel.setVisible(true);
                notificationButton.setStyle("-fx-background-color: #FFEB3B;"); // Yellowish highlight
            } else {
                notificationCountLabel.setText("0");
                notificationCountLabel.setVisible(false);
                notificationButton.setStyle(""); // Reset style
            }
        });
    }

    // In AdminDashboardController (and Patient/Psychiatrist)

    private void createNotificationPopup() {
        notificationPopup = new Popup();
        notificationPopup.setAutoHide(true);

        // Main content VBox - Apply style class ONLY
        VBox popupContent = new VBox(5); // Adjust spacing if needed
        popupContent.getStyleClass().add("notification-popup-content"); // Apply style class
        popupContent.setMinWidth(350);
        popupContent.setMaxWidth(450);
        popupContent.setMaxHeight(500);
        // Padding is handled by the CSS class now

        // Title Label
        Label title = new Label("Notifications");
        title.getStyleClass().add("notification-popup-title");
        title.setMaxWidth(Double.MAX_VALUE);
        // Padding handled by CSS

        // ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("notification-scroll-pane"); // Apply class
        // Remove inline style for transparency, let CSS handle it
        // scrollPane.setStyle("-fx-background-color: transparent; ...");

        // List VBox
        VBox notificationList = new VBox(0);
        notificationList.setId("notificationListVBox");
        notificationList.getStyleClass().add("notification-list-vbox"); // Apply class
        // Padding handled by CSS
        scrollPane.setContent(notificationList);

        // Button
        Button markAllReadButton = new Button("Mark All as Read");
        markAllReadButton.getStyleClass().add("notification-mark-read-button"); // Apply class
        HBox buttonBox = new HBox(markAllReadButton);
        buttonBox.setAlignment(Pos.CENTER);
        // Styling handled by CSS and container padding
        buttonBox.setPadding(new Insets(10, 0, 5, 0)); // Adjusted padding for button box


        markAllReadButton.setOnAction(e -> {
            if (currentUser != null) {
                notificationDAO.markAllAsReadByUserId(currentUser.getId());
                loadNotifications();
                notificationPopup.hide();
            }
        });

        // Assemble
        popupContent.getChildren().addAll(title, scrollPane, buttonBox);
        notificationPopup.getContent().clear();
        notificationPopup.getContent().add(popupContent);
    }
    @FXML
    private void handleNotificationClick(ActionEvent event) {
        if (currentUser == null) return;

        // Use the notificationButton itself to get coordinates
        Button btn = notificationButton; // Use the @FXML reference directly

        if (notificationPopup == null) {
            createNotificationPopup(); // Ensure popup is created if null
        }

        if (notificationPopup.isShowing()) {
            notificationPopup.hide();
        } else {
            populateNotificationPopup(); // Load content just before showing

            // --- Calculate Position ---
            // Get button's bounds in screen coordinates
            javafx.geometry.Bounds screenBounds = btn.localToScreen(btn.getBoundsInLocal());
            if (screenBounds == null) {
                System.err.println("Cannot get screen bounds for notification button.");
                // Show at default location as fallback
                notificationPopup.show(btn.getScene().getWindow());
                return;
            }

            // Position below and slightly to the left/aligned with the button's left edge
            double popupX = screenBounds.getMinX(); // Align with left edge
            double popupY = screenBounds.getMaxY() + 5; // 5px below button

            // Optional: Check if popup goes off-screen and adjust X
            // Screen screen = Screen.getPrimary();
            // double screenWidth = screen.getVisualBounds().getWidth();
            // if (popupX + notificationPopup.getWidth() > screenWidth) {
            //     popupX = screenWidth - notificationPopup.getWidth() - 10; // Adjust with margin
            // }

            notificationPopup.show(btn.getScene().getWindow(), popupX, popupY);
            // --- End Calculate Position ---


            // Mark as read (keep this logic)
            notificationDAO.markAllAsReadByUserId(currentUser.getId());
            loadNotifications();
        }
    }
    private void populateNotificationPopup() {
        if (currentUser == null) return;

        VBox popupRoot = (VBox) notificationPopup.getContent().get(0);
        ScrollPane scrollPane = (ScrollPane) popupRoot.getChildren().get(1);
        VBox notificationList = (VBox) scrollPane.getContent();

        if (notificationList == null) return;
        notificationList.getChildren().clear();

        List<Notification> notifications = notificationDAO.getUnreadNotificationsByUserId(currentUser.getId());

        if (notifications.isEmpty()) {
            Label placeholder = new Label("No new notifications.");
            placeholder.getStyleClass().add("notification-placeholder-label");
            placeholder.setMaxWidth(Double.MAX_VALUE);
            notificationList.getChildren().add(placeholder);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm");

            for (Notification n : notifications) {
                // Main HBox entry container
                HBox notificationEntry = new HBox();
                notificationEntry.getStyleClass().add("notification-entry");

                Label iconPlaceholder = new Label("🔔");
                iconPlaceholder.getStyleClass().add("notification-icon-placeholder");

                String messageLower = n.getMessage().toLowerCase();
                // --- Add check for URGENT ---
                if (messageLower.contains("[urgent]") || messageLower.contains("urgent")) { // Check for keyword
                    iconPlaceholder.setText("❗"); // Urgent icon
                    notificationEntry.getStyleClass().add("notification-entry-urgent"); // Add specific class
                }
                // --- End check for URGENT ---
                else if (messageLower.contains("submitted")) { // Keep other checks
                    iconPlaceholder.setText("➕");
                    notificationEntry.getStyleClass().add("notification-entry-new");
                } else if (messageLower.contains("responded")) {
                    iconPlaceholder.setText("💬");
                    notificationEntry.getStyleClass().add("notification-entry-response");
                } else if (messageLower.contains("updated")) {
                    iconPlaceholder.setText("✎");
                    notificationEntry.getStyleClass().add("notification-entry-update");
                } else if (messageLower.contains("deleted")) {
                    iconPlaceholder.setText("🗑️");
                    notificationEntry.getStyleClass().add("notification-entry-deleted");
                }
                VBox textContainer = new VBox(2);
                HBox.setHgrow(textContainer, Priority.ALWAYS);
                // DEBUG: textContainer.setStyle("-fx-background-color: lightgreen;"); // Temporary background

                Label msgLabel = new Label(n.getMessage());
                msgLabel.getStyleClass().add("notification-message");

                Label timeLabel = new Label(n.getCreatedAt().format(formatter));
                timeLabel.getStyleClass().add("notification-timestamp");

                textContainer.getChildren().addAll(msgLabel, timeLabel);

                // --- Assemble Entry ---
                notificationEntry.getChildren().addAll(iconPlaceholder, textContainer);

                // Add Tooltips (AFTER adding entry to list maybe?)
                String tooltipMsg = n.getMessage() + "\n" + n.getCreatedAt().format(formatter);
                Tooltip tt = new Tooltip(tooltipMsg);
                tt.setStyle("-fx-font-size: 12px;");
                // Tooltip.install might be better applied outside loop after nodes are added

                notificationList.getChildren().add(notificationEntry);

            }
        }
    }
    @FXML
    private void goToReclamations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserReclamationMenu.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) reclamationsButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * Handle logout button click
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logging out");
        LoginController.logout();
    }

    /**
     * Navigate to the profile page
     */
    @FXML
    private void navigateToProfile(ActionEvent event) {
        System.out.println("Navigating to profile");
        WellTechApplication.loadFXML("profile");
    }


    @FXML
    private void navigateToArticles(ActionEvent event) {
        System.out.println("Navigating to articles");
        WellTechApplication.loadFXML("articlesList");
    }




    /**
     * === ADD THIS METHOD ===
     * Navigate to the consultations/appointments list page
     */
    @FXML
    private void navigateToAppointments(ActionEvent event) { // Use a clear name like navigateToConsultations or navigateToAppointments
        System.out.println("Navigating to appointments (consultations list)");
        // Load the FXML for the consultations list view
        WellTechApplication.loadFXML("consultationsList");
    }
    // =======================

    // Assuming you have a navigateToDashboard handler already
    @FXML
    private void navigateToDashboard(ActionEvent event) {
        // This method would typically reload the dashboard FXML itself
        // Since we are already *on* the dashboard, this might just refresh or do nothing,
        // or you might use a common main controller to handle this.
        // For simplicity, let's assume clicking dashboard reloads the dashboard view.
        WellTechApplication.loadFXML("patientDashboard"); // Reloads the current view
    }
    @FXML
    private void navigateToChatbot(ActionEvent event) {
        System.out.println("PatientDashboardController: Navigating to Chatbot.");
        WellTechApplication.loadFXML("chatbotView");
    }

    @FXML
    private void navigateToConsultationsList(ActionEvent event) {
        System.out.println("PatientDashboardController: Navigating to Appointments List."); // Debug print
        WellTechApplication.loadFXML("consultationsList"); // <<< ENSURE THIS IS CORRECT
    }
    @FXML
    private void showNotifications(ActionEvent event) {
        NotificationService.showNotifications();
    }
}