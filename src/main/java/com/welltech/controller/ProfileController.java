package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.UserDAO;
import com.welltech.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the profile view and user information updates
 */
public class ProfileController implements Initializable {
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button dashboardButton;
    
    @FXML
    private Button profileButton;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField roleField;
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private PasswordField currentPasswordField;
    
    @FXML
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Label usernameError;
    
    @FXML
    private Label fullNameError;
    
    @FXML
    private Label emailError;
    
    @FXML
    private Label phoneError;
    
    @FXML
    private Label currentPasswordError;
    
    @FXML
    private Label newPasswordError;
    
    @FXML
    private Label confirmPasswordError;
    
    @FXML
    private Label statusMessage;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private User currentUser;
    private final UserDAO userDAO = new UserDAO();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initializing ProfileController");
            
            // Get current user
            currentUser = LoginController.getCurrentUser();
            
            if (currentUser != null) {
                // Update welcome label
                userNameLabel.setText("Welcome, " + currentUser.getFullName());
                
                // Populate user fields
                populateUserFields();
                
                // Clear error messages when input changes
                setupErrorClearListeners();
            } else {
                System.err.println("No user is logged in");
                // Redirect to login if no user
                WellTechApplication.loadFXML("login");
            }
            
        } catch (Exception e) {
            System.err.println("Error initializing ProfileController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Populate form fields with current user data
     */
    private void populateUserFields() {
        usernameField.setText(currentUser.getUsername());
        roleField.setText(currentUser.getRole().toString());
        fullNameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());
        
        // Phone number might be null
        if (currentUser.getPhoneNumber() != null) {
            phoneField.setText(currentUser.getPhoneNumber());
        }
    }
    
    /**
     * Set up listeners to clear error messages when input changes
     */
    private void setupErrorClearListeners() {
        fullNameField.textProperty().addListener((obs, old, newVal) -> fullNameError.setText(""));
        emailField.textProperty().addListener((obs, old, newVal) -> emailError.setText(""));
        phoneField.textProperty().addListener((obs, old, newVal) -> phoneError.setText(""));
        
        currentPasswordField.textProperty().addListener((obs, old, newVal) -> currentPasswordError.setText(""));
        newPasswordField.textProperty().addListener((obs, old, newVal) -> newPasswordError.setText(""));
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> confirmPasswordError.setText(""));
        
        // Clear status message on any change
        fullNameField.textProperty().addListener((obs, old, newVal) -> statusMessage.setText(""));
        emailField.textProperty().addListener((obs, old, newVal) -> statusMessage.setText(""));
        phoneField.textProperty().addListener((obs, old, newVal) -> statusMessage.setText(""));
        currentPasswordField.textProperty().addListener((obs, old, newVal) -> statusMessage.setText(""));
        newPasswordField.textProperty().addListener((obs, old, newVal) -> statusMessage.setText(""));
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> statusMessage.setText(""));
    }
    
    /**
     * Handle save button click
     */
    @FXML
    private void handleSave(ActionEvent event) {
        try {
            System.out.println("Save button clicked");
            
            // Clear all errors
            clearAllErrors();
            
            // Validate inputs
            if (!validateInputs()) {
                return;
            }
            
            // Save personal information changes
            boolean infoUpdated = updateUserInfo();
            
            // Handle password change if provided
            boolean passwordUpdated = false;
            if (!currentPasswordField.getText().isEmpty()) {
                passwordUpdated = updatePassword();
            }
            
            // Update UI with success message
            if (infoUpdated || passwordUpdated) {
                statusMessage.setText("Profile successfully updated");
                statusMessage.getStyleClass().remove("error-message");
                statusMessage.getStyleClass().add("success-message");
                
                // Reload the user data
                currentUser = userDAO.getUserById(currentUser.getId());
                LoginController.updateCurrentUser(currentUser);
                
                // In case of password update, clear password fields
                if (passwordUpdated) {
                    currentPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error saving profile: " + e.getMessage());
            e.printStackTrace();
            statusMessage.setText("Error: " + e.getMessage());
            statusMessage.getStyleClass().remove("success-message");
            statusMessage.getStyleClass().add("error-message");
        }
    }
    
    /**
     * Update user's personal information
     * @return true if updated successfully
     */
    private boolean updateUserInfo() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        
        // Check if anything changed
        boolean nameChanged = !fullName.equals(currentUser.getFullName());
        boolean emailChanged = !email.equals(currentUser.getEmail());
        boolean phoneChanged = !phone.equals(currentUser.getPhoneNumber() == null ? "" : currentUser.getPhoneNumber());
        
        if (!nameChanged && !emailChanged && !phoneChanged) {
            return false; // Nothing to update
        }
        
        // Update user object
        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        currentUser.setPhoneNumber(phone.isEmpty() ? null : phone);
        
        // Save to database
        return userDAO.updateUser(currentUser);
    }
    
    /**
     * Update user's password
     * @return true if updated successfully
     */
    private boolean updatePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        
        // Verify current password
        if (!currentUser.getPassword().equals(currentPassword)) {
            currentPasswordError.setText("Current password is incorrect");
            return false;
        }
        
        // Update password in user object
        currentUser.setPassword(newPassword);
        
        // Save to database
        return userDAO.updateUserPassword(currentUser.getId(), newPassword);
    }
    
    /**
     * Validate all user inputs
     * @return true if all inputs are valid
     */
    private boolean validateInputs() {
        boolean isValid = true;
        
        // Validate full name
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            fullNameError.setText("Full name is required");
            isValid = false;
        }
        
        // Validate email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("Email is required");
            isValid = false;
        } else if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            emailError.setText("Please enter a valid email address");
            isValid = false;
        }
        
        // Validate phone (optional)
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !phone.matches("\\d{8,15}")) {
            phoneError.setText("Please enter a valid phone number");
            isValid = false;
        }
        
        // Validate password fields if the user is changing password
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            // All password fields must be filled if any are filled
            if (currentPassword.isEmpty()) {
                currentPasswordError.setText("Current password is required");
                isValid = false;
            }
            
            if (newPassword.isEmpty()) {
                newPasswordError.setText("New password is required");
                isValid = false;
            } else if (newPassword.length() < 6) {
                newPasswordError.setText("Password must be at least 6 characters");
                isValid = false;
            }
            
            if (confirmPassword.isEmpty()) {
                confirmPasswordError.setText("Please confirm your new password");
                isValid = false;
            } else if (!confirmPassword.equals(newPassword)) {
                confirmPasswordError.setText("Passwords do not match");
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    /**
     * Clear all error messages
     */
    private void clearAllErrors() {
        usernameError.setText("");
        fullNameError.setText("");
        emailError.setText("");
        phoneError.setText("");
        currentPasswordError.setText("");
        newPasswordError.setText("");
        confirmPasswordError.setText("");
        statusMessage.setText("");
    }
    
    /**
     * Handle cancel button click - revert to original values
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        System.out.println("Cancel button clicked");
        
        // Clear errors
        clearAllErrors();
        
        // Reset fields to original values
        populateUserFields();
        
        // Clear password fields
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
    
    /**
     * Handle logout button click
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logout button clicked");
        LoginController.logout();
    }
    
    /**
     * Navigate back to the appropriate dashboard based on user role
     */
    @FXML
    private void navigateToDashboard(ActionEvent event) {
        System.out.println("Navigating to dashboard");
        
        if (currentUser != null) {
            switch (currentUser.getRole()) {
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
                    System.err.println("Unknown user role: " + currentUser.getRole());
                    WellTechApplication.loadFXML("login");
            }
        } else {
            // If no user, go to login
            WellTechApplication.loadFXML("login");
        }
    }
} 