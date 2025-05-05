package com.welltech;

import com.welltech.db.DatabaseConnection;
import com.welltech.model.Article;
import javafx.application.Application;
import javafx.application.Platform; // Import Platform
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert; // Import Alert
import javafx.scene.control.ButtonType; // Import ButtonType
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Main application class for the WellTech Psychiatry Platform
 */
public class WellTechApplication extends Application {

    private static Stage primaryStage;
    private static Article currentArticle;
    // Define CSS Path as a constant
    private static final String CSS_PATH = "/css/style.css";
    
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database
        try {
            System.out.println("Initializing database connection...");
            DatabaseConnection.initializeDatabase();
            System.out.println("Database initialization check complete.");
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            // Show error but continue
            showErrorAlert("Database Error", "Could not initialize database.\nApplication may not function correctly.");
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
        loadFXML("login");
    }

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
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setCurrentArticle(Article article) {
        currentArticle = article;
    }

    public static Article getCurrentArticle() {
        return currentArticle;
    }

    public static void main(String[] args) {
        launch(args);
    }
}