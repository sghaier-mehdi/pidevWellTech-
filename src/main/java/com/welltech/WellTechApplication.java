package com.welltech;

import com.welltech.db.DatabaseConnection; // Keep this import
import com.welltech.model.Article;
import com.welltech.model.Consultation; // *** ADD THIS IMPORT ***

import javafx.application.Application;
import javafx.application.Platform; // Import Platform
import javafx.application.Platform; // *** ADD THIS IMPORT ***
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert; // Import Alert
import javafx.scene.control.ButtonType; // Import ButtonType
import javafx.stage.Stage;
import javafx.scene.control.Alert; // *** ADD THIS IMPORT ***
import javafx.scene.control.Alert.AlertType; // *** ADD THIS IMPORT ***
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
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
    // Define CSS Path as a constant
    private static final String CSS_PATH = "/css/style.css";


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

    public static Window getPrimaryStage() {
        return null;
    }

    @Override
    public void start(Stage stage) { // Removed 'throws IOException' as we catch it
        // Initialize database
        try {
            System.out.println("Initializing database connection...");
            DatabaseConnection.initializeDatabase(); // Attempt initialization ONCE
            System.out.println("Database initialization check complete.");

        } catch (SQLException se) { // Catch specific SQL errors (like connection failure)
            System.err.println("FATAL: Failed to initialize database (SQL Error): " + se.getMessage());
            se.printStackTrace();
            showErrorAlertAndExit("Database Error", "Could not connect to or initialize the database.\nPlease check configuration and server status.\nError: " + se.getMessage());
            return; // Stop further execution if DB init fails

        }  catch (Exception e) { // Catch any other unexpected exceptions during init
            System.err.println("FATAL: Unexpected error during database initialization: " + e.getMessage());
            e.printStackTrace();
            showErrorAlertAndExit("Initialization Error", "An unexpected error occurred during startup.\nError: " + e.getMessage());
            return; // Stop
        }
        primaryStage = stage;
        primaryStage.setTitle("WellTech Psychiatry Platform");
        // Optional: Add an application icon
        // try {
        //     primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        // } catch (Exception e) {
        //     System.err.println("App icon not found or failed to load.");
        // }

        // Load initial scene using the updated method

        // Load initial scene
        loadFXML("login");
    }

    // Helper method to show error and exit cleanly
    private static void showErrorAlertAndExit(String title, String content) {
        // Ensure UI updates happen on the correct thread
        Runnable alertTask = () -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null); // No header text
            alert.setContentText(content);
            alert.showAndWait(); // Show the alert and wait for user to close it
            Platform.exit(); // Exit the JavaFX application
            System.exit(1); // Exit the JVM (optional, ensures termination)
        };

        if (Platform.isFxApplicationThread()) {
            alertTask.run();
        } else {
            Platform.runLater(alertTask);
        }
    }

    // Keep your existing showErrorAlert method for non-fatal errors if needed
    /**
     * Load a new FXML file, set it as the current scene's root,
     * and ensure the application stylesheet is applied.
     * @param fxml FXML file name without extension (relative to /fxml/)
     */
    public static void loadFXML(String fxml) {
        try {
            System.out.println("Attempting to load FXML: /fxml/" + fxml + ".fxml");
            // Ensure the path starts with a '/' for absolute path from resources root
            URL fxmlUrl = WellTechApplication.class.getResource("/fxml/" + fxml + ".fxml");
            if (fxmlUrl == null) {
                System.err.println("FATAL: FXML file not found: /fxml/" + fxml + ".fxml");
                showErrorAlert("Application Error", "Cannot load interface file: " + fxml + ".fxml");
                return; // Stop if FXML is missing
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load(); // Load the FXML content

            Scene scene = primaryStage.getScene(); // Get the current scene

            // --- Apply Stylesheet Reliably ---
            URL cssUrl = WellTechApplication.class.getResource(CSS_PATH);
            if (cssUrl == null) {
                System.err.println("ERROR: Stylesheet not found at path: " + CSS_PATH);
                // Optionally show a warning, but continue without custom styles
            }

            if (scene == null) { // If this is the first time loading (no scene yet)
                scene = new Scene(root); // Create a new scene
                if (cssUrl != null) {
                    try {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                        System.out.println("Stylesheet applied to new scene: " + cssUrl.toExternalForm());
                    } catch (Exception e) {
                        System.err.println("Error applying CSS to new scene: " + e.getMessage());
                    }
                }
                primaryStage.setScene(scene); // Set the scene on the stage
            } else { // If a scene already exists, just update its root node
                scene.setRoot(root);
                // Ensure the stylesheet is applied to the existing scene
                scene.getStylesheets().clear(); // Clear previous styles first (optional but safer)
                if (cssUrl != null) {
                    try {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                        System.out.println("Stylesheet re-applied to existing scene: " + cssUrl.toExternalForm());
                    } catch (Exception e) {
                        System.err.println("Error applying CSS to existing scene: " + e.getMessage());
                    }
                }
            }
            // --- End Apply Stylesheet ---

            // Optional: Center stage on screen after loading new content
            // primaryStage.centerOnScreen();

            primaryStage.show(); // Show the stage
            System.out.println("Showing scene for: " + fxml);

        } catch (IOException e) {
            System.err.println("Error loading FXML '" + fxml + "': " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Loading Error", "Failed to load interface: " + fxml + ".fxml\n" + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Error: Likely controller issue or FXML loading problem for '" + fxml + "': " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Interface Error", "Could not properly initialize the view: " + fxml + ".fxml");
        } catch (Exception e) {
            System.err.println("Unexpected error during FXML loading or scene setup for '" + fxml + "': " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Unexpected Error", "An error occurred while loading the view.");
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
     * Helper method to show error alerts on the JavaFX Application Thread.
     * @param title The title of the alert window.
     * @param content The main message of the alert.
     */
    private static void showErrorAlert(String title, String content) {
        // Ensure UI updates happen on the correct thread
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null); // No header text
            alert.setContentText(content);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(content);
                alert.showAndWait();
            });
        }
    }

    // --- Existing Static Methods ---


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