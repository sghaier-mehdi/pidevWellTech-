package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.model.User;
import com.welltech.service.NotificationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
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

    // --- Add this FXML injection for the Appointments button ---
    @FXML
    private Button appointmentsButton;
    // ----------------------------------------------------------
    @FXML private Button chatbotButton;
    @FXML
    private Button historyButton; // Assuming this exists in your FXML
    // Note: Articles button seems missing in your patientDashboard.fxml based on snippets,
    //       but if it exists, add its FXML injection too.
    // @FXML private Button articlesButton;

    @FXML
    private Button messagesButton; // Assuming this exists in your FXML

    @FXML
    private Button profileButton;

    @FXML
    private Button notificationsButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initializing PatientDashboardController");
            // Get current user
            User currentUser = LoginController.getCurrentUser();

            if (currentUser != null) {
                // Update welcome label with user's name
                userNameLabel.setText("Welcome, " + currentUser.getFullName());
            } else {
                System.err.println("No user is logged in");
                // Redirect to login if no user
                WellTechApplication.loadFXML("login");
                return; // Stop initialization
            }

            // Set dashboard button as active when on the dashboard view
            // Ensure other buttons like 'appointmentsButton' do *not* have the 'active' class here
            dashboardButton.getStyleClass().add("active");
            // Make sure to remove 'active' from other buttons if they might have it from a previous view
            if (appointmentsButton != null) appointmentsButton.getStyleClass().remove("active");
            // ... remove 'active' from other buttons like historyButton, messagesButton, profileButton


            // Initialize dashboard content (e.g., summary stats - not implemented yet)

            System.out.println("PatientDashboardController initialized successfully");
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
    private void navigateToAppointments(ActionEvent event) { // Use a clear name like navigateToConsultations or navigateToAppointments
        System.out.println("Navigating to appointments (consultations list)");
        // Load the FXML for the consultations list view
        WellTechApplication.loadFXML("consultationsList");
    }
    // =======================

    // Assuming you have a navigateToDashboard handler already
    @FXML
    private void navigateToDashboard(ActionEvent event) {
        // This method would typically reload the dashboard FXML itself
        // Since we are already *on* the dashboard, this might just refresh or do nothing,
        // or you might use a common main controller to handle this.
        // For simplicity, let's assume clicking dashboard reloads the dashboard view.
        WellTechApplication.loadFXML("patientDashboard"); // Reloads the current view
    }
    @FXML
    private void navigateToChatbot(ActionEvent event) {
        System.out.println("PatientDashboardController: Navigating to Chatbot.");
        WellTechApplication.loadFXML("chatbotView");
    }

    @FXML
    private void navigateToConsultationsList(ActionEvent event) {
        System.out.println("PatientDashboardController: Navigating to Appointments List."); // Debug print
        WellTechApplication.loadFXML("consultationsList"); // <<< ENSURE THIS IS CORRECT
    }
    @FXML
    private void showNotifications(ActionEvent event) {
        NotificationService.showNotifications();
    }
}