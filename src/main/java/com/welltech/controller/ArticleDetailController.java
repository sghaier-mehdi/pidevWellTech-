package com.welltech.controller;

import com.welltech.model.Article;
import javafx.application.Platform; // Added for potential later use with listeners
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage; // Import Stage

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class ArticleDetailController {

    @FXML private ScrollPane scrollPane;
    @FXML private ImageView articleImageView;
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private TextFlow contentFlow;
    @FXML private StackPane imageContainer;

    private Article article;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    // Called by FXML loader after fields are injected
    public void initialize() {
        // *** Bind managed property to visible property ***
        // This ensures the label/container doesn't take up space when invisible
        imageContainer.managedProperty().bind(imageContainer.visibleProperty());
        categoryLabel.managedProperty().bind(categoryLabel.visibleProperty());

        // Set initial state (optional, can be handled purely in displayArticle)
        imageContainer.setVisible(false);
        categoryLabel.setVisible(false);
    }

    // Method called by ArticlesListController to pass the article data
    public void setArticle(Article article) {
        this.article = article;
        if (article != null) {
            displayArticle();
        } else {
            System.err.println("ArticleDetailController received a null article.");
            // Optionally display an error message or default state
        }
    }

    // Populates the UI elements with the article data
    private void displayArticle() {
        // Assume article is not null because of check in setArticle

        titleLabel.setText(article.getTitle());
        authorLabel.setText("By " + (article.getAuthorName() != null ? article.getAuthorName() : "Unknown"));

        if (article.getCreatedAt() != null) {
            dateLabel.setText(article.getCreatedAt().format(DATE_FORMATTER));
        } else {
            dateLabel.setText(""); // Clear if no date
        }

        // *** Correctly manage category visibility and text in code ***
        if (article.getCategory() != null) {
            categoryLabel.setText(article.getCategory().getName().toUpperCase());
            categoryLabel.setVisible(true); // Show label
        } else {
            categoryLabel.setText(""); // Clear text
            categoryLabel.setVisible(false); // Hide label
        }

        // Load Image
        imageContainer.setVisible(false); // Assume hidden initially for image loading
        if (article.getImageUrl() != null && !article.getImageUrl().trim().isEmpty()) {
            try {
                Image img = new Image(article.getImageUrl(), true); // Background loading

                // Error Listener
                img.errorProperty().addListener((obs, ov, nv) -> {
                    if (nv) {
                        System.err.println("Failed to load article detail image: " + article.getImageUrl());
                        // Ensure container stays hidden (should already be false)
                        Platform.runLater(() -> imageContainer.setVisible(false));
                    }
                });

                // Progress Listener (only show image AND container when load complete and successful)
                img.progressProperty().addListener((obs, ov, nv)-> {
                    if (nv.doubleValue() == 1.0) { // Load complete
                        if (!img.isError()) {
                            Platform.runLater(() -> { // Update UI on FX thread
                                articleImageView.setImage(img);
                                imageContainer.setVisible(true); // Show container
                            });
                        } else {
                            Platform.runLater(() -> imageContainer.setVisible(false)); // Ensure hidden on error
                        }
                    }
                });

                // Don't set image/visibility immediately here, rely on listeners for background loading
                // articleImageView.setImage(img);
                // imageContainer.setVisible(true);

            } catch (IllegalArgumentException iae) {
                // Catch specifically if URL is invalid *before* loading starts
                System.err.println("Invalid Image URL syntax: " + article.getImageUrl() + " - " + iae.getMessage());
                imageContainer.setVisible(false);
            } catch (Exception e) {
                System.err.println("Error creating image object for detail view: " + e.getMessage());
                imageContainer.setVisible(false);
            }
        }
        // If URL was null/empty, container remains hidden (setVisible(false) above)

        // Populate TextFlow Content
        contentFlow.getChildren().clear();
        if (article.getContent() != null) {
            String[] paragraphs = article.getContent().split("\\n\\s*\\n"); // Split on blank lines
            boolean firstPara = true;
            for (String para : paragraphs) {
                String trimmedPara = para.trim();
                if (!trimmedPara.isEmpty()) {
                    // Create Text node for paragraph
                    Text paragraphText = new Text(trimmedPara);
                    // You could apply default text style here if not handled by CSS
                    // paragraphText.setStyle("-fx-fill: #334155; -fx-line-spacing: 5px;");

                    // Add newline Text node *after* each paragraph for spacing
                    Text newline = new Text("\n\n");

                    contentFlow.getChildren().addAll(paragraphText, newline);
                }
            }
        }
    }

    // Handles the Close button action
    @FXML
    private void handleClose() {
        // Get the Stage this scene belongs to
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        if (stage != null) {
            stage.close(); // Close the window
        }
    }
}