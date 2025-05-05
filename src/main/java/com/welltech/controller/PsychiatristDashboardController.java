package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn; // Keep if you still have a table on the dashboard
import javafx.scene.control.TableView;    // Keep if you still have a table on the dashboard
import com.welltech.service.NotificationService;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
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

    @FXML
    private Button notificationsButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get current user
            User currentUser = LoginController.getCurrentUser();

            if (currentUser != null) {
                // Update welcome label with user's name
                userNameLabel.setText("Welcome, Dr. " + currentUser.getFullName());
                // Ensure correct role
                if (currentUser.getRole() != User.UserRole.PSYCHIATRIST && currentUser.getRole() != User.UserRole.ADMIN) {
                    System.err.println("User is not a Psychiatrist or Admin, redirecting from Psychiatrist Dashboard.");
                    WellTechApplication.loadFXML("login"); // Or appropriate dashboard
                    return;
                }
            } else {
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
        NotificationService.showGlobalNotifications();
    }
    // --- Add other navigation handlers if they exist in your FXML ---
    // @FXML private void navigateToPatients(ActionEvent event) { ... }
    // @FXML private void navigateToMessages(ActionEvent event) { ... }
    // -------------------------------------------------------------
}