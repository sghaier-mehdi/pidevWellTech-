package com.welltech;

import com.welltech.db.DatabaseConnection;
import com.welltech.model.Article;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for the WellTech Psychiatry Platform
 */
public class WellTechApplication extends Application {
    
    private static Stage primaryStage;
    private static Article currentArticle;
    
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database
        try {
            System.out.println("Initializing database connection...");
            DatabaseConnection.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            // Continue with the application even if database initialization fails
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
    
    /**
     * Entry point of the application
     */
    public static void main(String[] args) {
        launch(args);
    }
} 