package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.UserDAO;
import com.welltech.model.User;
import com.welltech.service.FirebaseNotificationManager;
import com.welltech.service.NotificationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the admin dashboard
 */
public class AdminDashboardController implements Initializable {
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Button logoutButton;
    @FXML
    private Button chatbotButton;
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
    private TableView<User> usersTable;
    
    @FXML
    private TableColumn<User, Integer> userIdColumn;
    
    @FXML
    private TableColumn<User, String> usernameColumn;
    
    @FXML
    private TableColumn<User, String> nameColumn;
    
    @FXML
    private TableColumn<User, String> emailColumn;
    
    @FXML
    private TableColumn<User, User.UserRole> roleColumn;
    
    @FXML
    private TableColumn<User, Void> actionsColumn;
    
    private final UserDAO userDAO = new UserDAO();
    private final NotificationManager notificationManager = new FirebaseNotificationManager();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get current user
            User currentUser = LoginController.getCurrentUser();
            
            if (currentUser != null) {
                // Update welcome label with user's name
                userNameLabel.setText("Welcome, " + currentUser.getFullName());
            } else {
                System.err.println("No user is logged in");
            }
            
            // Set dashboard button as active
            dashboardButton.getStyleClass().add("active");
            
            // Initialize users table (in a real app, we would set cell value factories and populate data)
            // For now, we're just setting up the structure
            
            System.out.println("AdminDashboardController initialized");
        } catch (Exception e) {
            System.err.println("Error initializing AdminDashboardController: " + e.getMessage());
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
    
    /**
     * Navigate to the articles page
     */
    @FXML
    private void navigateToArticles(ActionEvent event) {
        System.out.println("Navigating to articles");
        WellTechApplication.loadFXML("articlesList");
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