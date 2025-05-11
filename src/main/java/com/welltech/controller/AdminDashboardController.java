package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.OrderDAO;
import com.welltech.dao.ProductDAO;
import com.welltech.dao.UserDAO;
import com.welltech.model.User;
import com.welltech.service.FirebaseNotificationManager;
import com.welltech.service.NotificationManager;
import com.welltech.model.UserRole;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.welltech.dao.NotificationDAO;
import com.welltech.model.Notification;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;

import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the admin dashboard
 */
public class AdminDashboardController implements Initializable {
    @FXML private Button notificationButton; // Button that triggers notification popup
    @FXML private VBox notificationsBox;

    @FXML
    private Label userNameLabel;
    @FXML
    private Button reclamationsButton;

    @FXML
    private Button logoutButton;
    @FXML
    private Button chatbotButton;
    @FXML private Button statsButton;

    @FXML
    private Button dashboardButton;
    
    @FXML
    private Button usersButton;
    
    @FXML
    private Button psychiatristsButton;
    
    @FXML
    private Button patientsButton;
    
    @FXML
    private Button settingsButton;
    
    @FXML
    private Button articlesButton;
    
    @FXML
    private Button couponsButton;
    
    @FXML
    private Button objectivesButton;
    
    @FXML
    private Button sendNotificationButton;
    
    @FXML
    private Button productsButton;
    @FXML
    private TableColumn<User, UserRole> roleColumn;

    @FXML
    private Button ordersButton;
    
    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private Popup notificationPopup; // Popup window for notifications
    private Label notificationCountLabel;
    private User currentUser;

    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final NotificationManager notificationManager = new FirebaseNotificationManager();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get current user
             currentUser = LoginController.getCurrentUser();
            
            if (currentUser != null) {
                // Update welcome label with user's name
                userNameLabel.setText("Welcome, " + currentUser.getFullName());
                setupNotificationButton(); // Setup notification button and count
                loadNotifications(); // Initial load of notification count
            } else {
                System.err.println("No user is logged in");
                notificationButton.setDisable(true);
            }
            
            // Set dashboard button as active
            dashboardButton.getStyleClass().add("active");

            System.out.println("AdminDashboardController initialized");
        } catch (Exception e) {
            System.err.println("Error initializing AdminDashboardController: " + e.getMessage());
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
                // Optional: Add a visual cue like animation or color change
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
    private void navigateToStats(ActionEvent event) {
        System.out.println("Navigating to Reclamation Statistics...");
        WellTechApplication.loadFXML("ReclamationStats"); // Load the new FXML
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
    // In AdminDashboardController.java (and others)

    // Import Region if not already imported
    // Ensure HBox is imported

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
                // Inside populateNotificationPopup loop...

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

                // ... rest of the method (text container, adding children) ...

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

            } // End for loop

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
    private void ouvrirReclamations() {
        WellTechApplication.loadFXML("AdminReclamation");
    }


    @FXML
    private void handleProducts(ActionEvent event) {
        try {
            URL location = AdminDashboardController.class.getResource("/fxml/product/ProductList.fxml");
            if (location == null) {
                throw new IOException("Could not find ProductList.fxml");
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Product Management");
            stage.setScene(scene);

            ProductListController controller = loader.getController();
            controller.setProductDAO(productDAO);

            stage.show();
        } catch (Exception e) {
            showError("Error", "Could not open product management window", e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOrders(ActionEvent event) {
        try {
            URL location = AdminDashboardController.class.getResource("/fxml/order/OrderList.fxml");
            if (location == null) {
                throw new IOException("Could not find OrderList.fxml");
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Order Management");
            stage.setScene(scene);

            OrderListController controller = loader.getController();
            controller.setOrderDAO(orderDAO);

            stage.show();
        } catch (Exception e) {
            showError("Error", "Could not open order management window", e);
            e.printStackTrace();
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content + "\n" + e.getMessage());
        alert.showAndWait();
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
        System.out.println("AdminDashboardController: Navigating to Chatbot.");
        WellTechApplication.loadFXML("chatbotView");
    }

    @FXML
    private void showSendNotificationDialog(ActionEvent event) {
        // Create a dialog for sending notifications
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Send Notification");
        dialog.setHeaderText("Create a new notification");

        // Create the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20 150 10 10;");

        // Create ComboBox for user selection
        ComboBox<String> userSelector = new ComboBox<>();
        userSelector.setPromptText("Select recipient");

        // Add "Global Notification" as first option
        userSelector.getItems().add("Global Notification");

        // Add all users to the ComboBox
        List<User> allUsers = userDAO.getAllUsers();
        for (User user : allUsers) {
            userSelector.getItems().add(user.getFullName() + " (ID: " + user.getId() + ")");
        }

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Enter notification message");
        messageArea.setPrefRowCount(3);

        grid.add(new Label("Recipient:"), 0, 0);
        grid.add(userSelector, 1, 0);
        grid.add(new Label("Message:"), 0, 1);
        grid.add(messageArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Show the dialog and handle the result
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String message = messageArea.getText().trim();
                String selectedOption = userSelector.getValue();

                if (!message.isEmpty()) {
                    String userId = null;
                    if (selectedOption != null && !selectedOption.equals("Global Notification")) {
                        // Extract user ID from the selected option
                        userId = selectedOption.substring(selectedOption.lastIndexOf("(ID: ") + 5, selectedOption.length() - 1);
                    }

                    boolean success = notificationManager.sendNotification(message, userId);

                    Alert alert = new Alert(success ? AlertType.INFORMATION : AlertType.ERROR);
                    alert.setTitle(success ? "Success" : "Error");
                    alert.setHeaderText(null);
                    alert.setContentText(success ?
                        "Notification sent successfully!" :
                        "Failed to send notification. Please try again.");
                    alert.show();
                } else {
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.setHeaderText(null);
                    alert.setContentText("Please enter a message for the notification.");
                    alert.show();
                }
            }
        });
    }
}