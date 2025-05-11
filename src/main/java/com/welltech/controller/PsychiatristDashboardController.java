package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.model.User;
import com.welltech.model.UserRole;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import com.welltech.util.SessionManager;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;
import javafx.scene.layout.VBox;
import com.welltech.dao.NotificationDAO; // Import NotificationDAO
import com.welltech.model.Notification;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn; // Keep if you still have a table on the dashboard
import javafx.scene.control.TableView;    // Keep if you still have a table on the dashboard
import com.welltech.service.NotificationService;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Controller for the psychiatrist dashboard
 */
public class PsychiatristDashboardController implements Initializable {

    @FXML private Button notificationButton; // Button that triggers notification popup
    @FXML private VBox notificationsBox;


    @FXML
    private Label userNameLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button dashboardButton;

    // --- Add this FXML injection for the Appointments button ---
    @FXML
    private Button appointmentsButton;
    // ----------------------------------------------------------

    @FXML
    private Button patientsButton; // Assuming this exists in your FXML

    @FXML
    private Button messagesButton; // Assuming this exists in your FXML

    @FXML
    private Button profileButton;
    @FXML
    private Button chatbotButton;

    // Assuming these are part of a TableView on the dashboard (can be kept or removed if table is removed)
    @FXML private TableView<?> appointmentsTable;
    @FXML private TableColumn<?, ?> timeColumn;
    @FXML private TableColumn<?, ?> patientColumn;
    @FXML private TableColumn<?, ?> purposeColumn;
    @FXML private TableColumn<?, ?> statusColumn;
    @FXML private TableColumn<?, ?> actionColumn;
    // End TableView injections

    @FXML
    private Button articlesButton;
    
    @FXML
    private Button couponsButton;
    
    @FXML
    private Button objectivesButton;
    private final NotificationDAO notificationDAO = new NotificationDAO(); // Instantiate DAO
    private User currentUser;
    private Popup notificationPopup; // Popup window for notifications
    private Label notificationCountLabel; // Label to show the count on the button
    @FXML
    private Button reclamationsButton;
    
    @FXML
    private Button notificationsButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get current user
             currentUser = LoginController.getCurrentUser();
            
            User currentUser = LoginController.getCurrentUser();

            if (currentUser != null) {
                // Update welcome label with user's name
                userNameLabel.setText("Welcome, Dr. " + currentUser.getFullName());
                setupNotificationButton(); // Setup notification button and count
                loadNotifications(); // Initial load of notification count

                // Ensure correct role
                if (currentUser.getRole() != UserRole.PSYCHIATRIST && currentUser.getRole() != UserRole.ADMIN) {
                    System.err.println("User is not a Psychiatrist or Admin, redirecting from Psychiatrist Dashboard.");
                    WellTechApplication.loadFXML("login"); // Or appropriate dashboard
                    return;
                }
            } else {
                System.err.println("No user is logged in");
                notificationButton.setDisable(true);
                System.err.println("No user is logged in for PsychiatristDashboardController");
                WellTechApplication.loadFXML("login");
                return; // Stop initialization
            }

            // Set dashboard button as active when on the dashboard view
            dashboardButton.getStyleClass().add("active");
            // --- Ensure Appointments button does NOT have 'active' style here ---
            if (appointmentsButton != null) appointmentsButton.getStyleClass().remove("active");
            // Make sure to remove 'active' from other buttons if they might have it
            // ... remove 'active' from patientsButton, messagesButton, profileButton


            // Initialize appointment table (dummy data would be added here if you still have a table)
            // For a real app, we would fetch appointments from database

            System.out.println("PsychiatristDashboardController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing PsychiatristDashboardController: " + e.getMessage());
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

        // NOTE: Psychiatrists might not get direct reclamation notifications
        // unless specifically designed. Adjust this logic if needed.
        // Currently, it will show notifications sent *specifically* to this psychiatrist's user ID.
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

                // --- Text Content VBox ---
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

    /**
     * Navigate to the articles page
     */
    @FXML
    private void navigateToArticles(ActionEvent event) {
        System.out.println("Navigating to articles");
        WellTechApplication.loadFXML("articlesList");
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
     * === ADD THIS METHOD ===
     * Navigate to the consultations/appointments list page
     */
    @FXML
    private void navigateToAppointments(ActionEvent event) { // Or navigateToConsultations
        System.out.println("Navigating to appointments (consultations list)");
        // Load the FXML for the consultations list view
        WellTechApplication.loadFXML("consultationsList");
    }
    // =======================

    /**
     * Navigate back to the dashboard (likely reloads the current view)
     */
    @FXML
    private void navigateToDashboard(ActionEvent event) {
        System.out.println("Navigating back to Psychiatrist dashboard");
        WellTechApplication.loadFXML("psychiatristDashboard"); // Reloads the current view
    }

    @FXML
    private void navigateToCoupons(ActionEvent event) {
        System.out.println("Navigating to coupons");
        WellTechApplication.loadFXML("coupon");
    }

    @FXML
    private void navigateToObjectives(ActionEvent event) {
        System.out.println("Navigating to objectives");
        WellTechApplication.loadFXML("objectives");
    }

    @FXML
    private void navigateToChatbot(ActionEvent event) {
        System.out.println("PsychiatristDashboardController: Navigating to Chatbot.");
        WellTechApplication.loadFXML("chatbotView");
    }

    @FXML
    private void showNotifications(ActionEvent event) {
        NotificationService.showNotifications();
    }
    // --- Add other navigation handlers if they exist in your FXML ---
    // @FXML private void navigateToPatients(ActionEvent event) { ... }
    // @FXML private void navigateToMessages(ActionEvent event) { ... }
    // -------------------------------------------------------------
}