package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.UserDAO;
import com.welltech.model.User;

import com.welltech.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the login form
 */
public class LoginController implements Initializable {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Hyperlink registerLink;
    
    // Data access object for user operations
    private final UserDAO userDAO = new UserDAO();
    
    // Current logged in user
    private static User currentUser;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("LoginController initialized");
            
            // Clear error message when input changes
            usernameField.textProperty().addListener((obs, oldVal, newVal) -> errorLabel.setText(""));
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> errorLabel.setText(""));
            
        } catch (Exception e) {
            System.err.println("Error initializing LoginController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            System.out.println("Login button clicked");
            
            // Clear previous error
            errorLabel.setText("");
            
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            
            // Validate inputs
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username and password are required");
                return;
            }
            
            System.out.println("Attempting to authenticate user: " + username);
            
            // Get user from database
            User user = userDAO.getUserByUsername(username);
            
            // Check if user exists and password matches
            if (user != null && user.getPassword().equals(password)) {  // In a real app, use password hashing
                System.out.println("Authentication successful for user: " + username);
                
                // Store current user
                currentUser = user;
                SessionManager.setCurrentUser(user);


                // Navigate to dashboard based on user role
                navigateToDashboard(user);
            } else {
                System.out.println("Authentication failed for user: " + username);
                errorLabel.setText("Invalid username or password");
            }
            
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Error: " + e.getMessage());
        }
    }
    
    /**
     * Handle register link click
     */
    @FXML
    private void handleRegisterLink(ActionEvent event) {
        System.out.println("Register link clicked");
        WellTechApplication.loadFXML("register");
    }
    
    /**
     * Navigate to the appropriate dashboard based on user role
     */
    private void navigateToDashboard(User user) {
        switch (user.getRole()) {
            case ADMIN:
                System.out.println("Navigating to admin dashboard");
                WellTechApplication.loadFXML("adminDashboard");
                break;
            case PSYCHIATRIST:
                System.out.println("Navigating to psychiatrist dashboard");
                WellTechApplication.loadFXML("psychiatristDashboard");
                break;
            case PATIENT:
                System.out.println("Navigating to patient dashboard");
                WellTechApplication.loadFXML("patientDashboard");
                break;
            default:
                System.err.println("Unknown user role: " + user.getRole());
                // Default to patient dashboard
                WellTechApplication.loadFXML("patientDashboard");
        }
    }
    
    /**
     * Get the currently logged in user
     * @return Current user or null if not logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Update the current user object with new values
     * @param user Updated user object
     */
    public static void updateCurrentUser(User user) {
        currentUser = user;
    }
    
    /**
     * Log out the current user
     */
    public static void logout() {
        currentUser = null;
        SessionManager.clear();
        WellTechApplication.loadFXML("login");
    }
} 