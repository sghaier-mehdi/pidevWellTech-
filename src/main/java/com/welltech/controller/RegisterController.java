package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.UserDAO;
import com.welltech.model.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Controller for the registration form
 */
public class RegisterController implements Initializable {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private ComboBox<String> roleComboBox;
    
    @FXML
    private Label errorMessageLabel;
    
    @FXML
    private Label successMessageLabel;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Button resetButton;
    
    @FXML
    private Hyperlink loginLink;
    
    // Data access object for user operations
    private final UserDAO userDAO = new UserDAO();
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialize role combo box
            roleComboBox.setItems(FXCollections.observableArrayList("PATIENT", "PSYCHIATRIST", "ADMIN"));
            
            // Clear error/success messages when input changes
            usernameField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
            confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
            fullNameField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
            emailField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
            phoneField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
            roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> clearMessages());
            
            System.out.println("RegisterController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing RegisterController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle register button click
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            System.out.println("Register button clicked");
            clearMessages();
            
            // Validate inputs
            if (!validateInputs()) {
                System.out.println("Input validation failed");
                return;
            }
            
            System.out.println("Inputs validated successfully");
            
            // Check if username already exists
            try {
                if (userDAO.getUserByUsername(usernameField.getText()) != null) {
                    showError("Username already exists. Please choose another.");
                    System.out.println("Username already exists: " + usernameField.getText());
                    return;
                }
                System.out.println("Username check passed");
            } catch (Exception e) {
                System.err.println("Error checking username: " + e.getMessage());
                e.printStackTrace();
                showError("Error checking username: " + e.getMessage());
                return;
            }
            
            // Create new user
            try {
                System.out.println("Creating User object with role: " + roleComboBox.getValue());
                User user = new User(
                    usernameField.getText(),
                    passwordField.getText(), // In a real app, this should be hashed
                    fullNameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    User.UserRole.valueOf(roleComboBox.getValue())
                );
                
                System.out.println("Inserting user into database");
                int userId = userDAO.insertUser(user);
                
                if (userId > 0) {
                    showSuccess("User registered successfully! User ID: " + userId);
                    System.out.println("User registered successfully with ID: " + userId);
                    resetForm();
                } else {
                    showError("Failed to register user. Please try again.");
                    System.err.println("Insert returned ID <= 0");
                }
            } catch (Exception e) {
                System.err.println("Error creating and inserting user: " + e.getMessage());
                e.printStackTrace();
                showError("Error registering user: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in handleRegister: " + e.getMessage());
            e.printStackTrace();
            showError("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Handle reset button click
     */
    @FXML
    private void handleReset(ActionEvent event) {
        resetForm();
        clearMessages();
    }
    
    /**
     * Handle login link click
     */
    @FXML
    private void handleLoginLink(ActionEvent event) {
        // Navigate to login page
        WellTechApplication.loadFXML("login");
    }
    
    /**
     * Validate all form inputs
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        // Check for empty fields
        if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty() ||
                confirmPasswordField.getText().isEmpty() || fullNameField.getText().isEmpty() ||
                emailField.getText().isEmpty() || phoneField.getText().isEmpty() ||
                roleComboBox.getValue() == null) {
            
            showError("All fields are required");
            return false;
        }
        
        // Check username length
        if (usernameField.getText().length() < 4) {
            showError("Username must be at least 4 characters long");
            return false;
        }
        
        // Check password length
        if (passwordField.getText().length() < 6) {
            showError("Password must be at least 6 characters long");
            return false;
        }
        
        // Check if passwords match
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match");
            return false;
        }
        
        // Validate email format
        if (!EMAIL_PATTERN.matcher(emailField.getText()).matches()) {
            showError("Invalid email format");
            return false;
        }
        
        // Validate phone format (simple check)
        if (!phoneField.getText().matches("\\d{10,15}")) {
            showError("Phone number should contain 10-15 digits only");
            return false;
        }
        
        return true;
    }
    
    /**
     * Reset the form
     */
    private void resetForm() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        fullNameField.clear();
        emailField.clear();
        phoneField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        errorMessageLabel.setText(message);
        successMessageLabel.setText("");
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        successMessageLabel.setText(message);
        errorMessageLabel.setText("");
    }
    
    /**
     * Clear error and success messages
     */
    private void clearMessages() {
        errorMessageLabel.setText("");
        successMessageLabel.setText("");
    }
} 