package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.*;
import com.welltech.model.Objective;
import com.welltech.model.Progression;
import com.welltech.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the patient dashboard
 */
public class PatientDashboardController implements Initializable {
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Button logoutButton;

    @FXML
    private Button dashboardButton;
    
    @FXML
    private Button appointmentsButton;
    
    @FXML
    private Button historyButton;
    
    @FXML
    private Button messagesButton;
    
    @FXML
    private Button profileButton;

    private final UserDAO userDAO = new UserDAO();

    public PatientDashboardController() throws SQLException {
    }


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
            
            System.out.println("PatientDashboardController initialized");
        } catch (Exception e) {
            System.err.println("Error initializing PatientDashboardController: " + e.getMessage());
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