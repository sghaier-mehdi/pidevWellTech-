package com.welltech.controller;

import com.welltech.WellTechApplication;
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
 * Controller for the psychiatrist dashboard
 */
public class PsychiatristDashboardController implements Initializable {
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button dashboardButton;
    
    @FXML
    private Button patientsButton;
    
    @FXML
    private Button appointmentsButton;
    
    @FXML
    private Button messagesButton;
    
    @FXML
    private Button profileButton;
    
    @FXML
    private TableView<?> appointmentsTable;
    
    @FXML
    private TableColumn<?, ?> timeColumn;
    
    @FXML
    private TableColumn<?, ?> patientColumn;
    
    @FXML
    private TableColumn<?, ?> purposeColumn;
    
    @FXML
    private TableColumn<?, ?> statusColumn;
    
    @FXML
    private TableColumn<?, ?> actionColumn;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get current user
            User currentUser = LoginController.getCurrentUser();
            
            if (currentUser != null) {
                // Update welcome label with user's name
                userNameLabel.setText("Welcome, Dr. " + currentUser.getFullName());
            } else {
                System.err.println("No user is logged in");
            }
            
            // Set dashboard button as active
            dashboardButton.getStyleClass().add("active");
            
            // Initialize appointment table (dummy data would be added here)
            // For a real app, we would fetch appointments from database
            
            System.out.println("PsychiatristDashboardController initialized");
        } catch (Exception e) {
            System.err.println("Error initializing PsychiatristDashboardController: " + e.getMessage());
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
} 