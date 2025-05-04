package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.UserDAO;
import com.welltech.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
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
    private void navigateToChatbot(ActionEvent event) {
        System.out.println("AdminDashboardController: Navigating to Chatbot.");
        WellTechApplication.loadFXML("chatbotView");
    }
} 