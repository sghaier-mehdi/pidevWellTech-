package com.welltech;

import com.welltech.db.DatabaseConnection; // Keep this import
import com.welltech.model.Article;
import com.welltech.model.Consultation; // *** ADD THIS IMPORT ***

import javafx.application.Application;
import javafx.application.Platform; // *** ADD THIS IMPORT ***
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert; // *** ADD THIS IMPORT ***
import javafx.scene.control.Alert.AlertType; // *** ADD THIS IMPORT ***

import java.io.IOException;
// Remove if not explicitly used here, relies on DAOs
// import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * Main application class for the WellTech Psychiatry Platform
 */
public class WellTechApplication extends Application {

    private static Stage primaryStage;
    private static Article currentArticle;

    // --- ADD THIS STATIC FIELD FOR CONSULTATION ---
    private static Consultation currentConsultation;
    // ----------------------------------------------

    // Add temporary data storage
    private static final Map<String, Object> tempData = new HashMap<>();

    /**
     * Store temporary data with a key
     * @param key The key to store the data under
     * @param value The data to store
     */
    public static void setTempData(String key, Object value) {
        tempData.put(key, value);
    }

    /**
     * Retrieve temporary data by key
     * @param key The key to retrieve data for
     * @return The stored data or null if not found
     */
    public static Object getTempData(String key) {
        return tempData.get(key);
    }

    /**
     * Remove temporary data by key
     * @param key The key to remove
     */
    public static void removeTempData(String key) {
        tempData.remove(key);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database
        try {
            System.out.println("Initializing database connection...");
            // === Ensure you are using YOUR DatabaseConnection here ===
            DatabaseConnection.initializeDatabase(); // Use YOUR DB class
            // ======================================================

        } catch (SQLException e) { // Catch SQLException specifically for DB connection errors
            System.err.println("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
            // Show a fatal error dialog
            Alert dbErrorAlert = new Alert(AlertType.ERROR); // Use AlertType from import
            dbErrorAlert.setTitle("Database Connection Error");
            dbErrorAlert.setHeaderText("Failed to connect to the database.");
            dbErrorAlert.setContentText("The application cannot connect to the database. Please check your database configuration and ensure the database server is running."); // Adjusted message
            dbErrorAlert.showAndWait();
            Platform.exit(); // Exit the application if DB fails
            return; // Stop execution
        } catch (Exception e) { // Catch other potential init errors
            System.err.println("Failed during database initialization process: " + e.getMessage());
            e.printStackTrace();
            Alert otherErrorAlert = new Alert(AlertType.ERROR); // Use AlertType from import
            otherErrorAlert.setTitle("Initialization Error");
            otherErrorAlert.setHeaderText("An error occurred during application startup.");
            otherErrorAlert.setContentText("Details: " + e.getMessage());
            otherErrorAlert.showAndWait();
            Platform.exit(); // Exit the application
            return;
        }


        primaryStage = stage;
        primaryStage.setTitle("WellTech Psychiatry Platform");

        // Load initial scene
        loadFXML("login");
    }

    /**
     * Load a new FXML file and set it as the current scene
     * @param fxml FXML file name without extension
     */
    public static void loadFXML(String fxml) {
        try {
            System.out.println("Loading FXML: " + fxml);
            FXMLLoader loader = new FXMLLoader(WellTechApplication.class.getResource("/fxml/" + fxml + ".fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
            // Optionally show an error dialog for the user
            Alert fxmlErrorAlert = new Alert(AlertType.ERROR); // Use AlertType from import
            fxmlErrorAlert.setTitle("Application Error");
            fxmlErrorAlert.setHeaderText("Could not load necessary screen.");
            fxmlErrorAlert.setContentText("An error occurred while loading the interface for '" + fxml + "'. Details: " + e.getMessage());
            fxmlErrorAlert.showAndWait();
            // Decide if this is a fatal error or if the app can continue
        }
    }

    /**
     * Set the current article for editing
     * @param article Article to edit or null for creating a new one
     */
    public static void setCurrentArticle(Article article) {
        currentArticle = article;
    }

    /**
     * Get the current article being edited
     * @return Current article or null if creating a new one
     */
    public static Article getCurrentArticle() {
        return currentArticle;
    }

    // --- ADD THESE STATIC METHODS FOR CONSULTATION ---
    /**
     * Set the current consultation for editing
     * @param consultation Consultation to edit or null for scheduling a new one
     */
    public static void setCurrentConsultation(Consultation consultation) {
        currentConsultation = consultation;
    }

    /**
     * Get the current consultation being edited
     * @return Current consultation or null if scheduling a new one
     */
    public static Consultation getCurrentConsultation() {
        return currentConsultation;
    }
    // -------------------------------------------------

    /**
     * Entry point of the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}