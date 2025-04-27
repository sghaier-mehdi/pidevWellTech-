package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.ArticleDAO;
import com.welltech.model.User;
import com.welltech.service.ArticleAIService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ChatbotController implements Initializable {

    // === FXML Injections for Header/Sidebar ===
    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    @FXML private Button dashboardButton;
    @FXML private Button articlesButton;
    @FXML private Button consultationsButton;
    @FXML private Button chatbotButton;
    @FXML private Button profileButton;
    // Add injections for other sidebar buttons if applicable

    // --- FXML Injections for Chat UI ---
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatHistoryBox;
    @FXML private TextField userInputField;
    @FXML private Button sendButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusMessage; // The label with the bound visible property

    // --- End Chat UI Injections ---

    private ArticleAIService articleAIService;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ArticleDAO articleDAO = new ArticleDAO();
        this.articleAIService = new ArticleAIService(articleDAO);

        currentUser = LoginController.getCurrentUser();

        if (currentUser == null) {
            System.err.println("ChatbotController: No user logged in."); // Debug
            WellTechApplication.loadFXML("login");
            return;
        }
        System.out.println("ChatbotController: Initializing for user: " + currentUser.getUsername()); // Debug

        // === Sidebar Button Styling ===
        if (dashboardButton != null) removeActiveStyle(dashboardButton);
        if (articlesButton != null) removeActiveStyle(articlesButton);
        if (consultationsButton != null) removeActiveStyle(consultationsButton);
        if (chatbotButton != null) removeActiveStyle(chatbotButton);
        if (profileButton != null) removeActiveStyle(profileButton);

        if (chatbotButton != null) {
            chatbotButton.getStyleClass().add("active");
        }
        // ==============================

        if (userNameLabel != null) {
            userNameLabel.setText("Welcome, " + currentUser.getFullName());
        }

        // Set up UI state initially
        loadingIndicator.setVisible(false);

        // Ensure status message label starts hidden by setting its text empty (handled by FXML binding)
        if (statusMessage != null) {
            statusMessage.managedProperty().bind(statusMessage.visibleProperty()); // Ensure this binding is set if not in FXML
            statusMessage.setText(""); // Set text to empty to make it invisible initially via binding
        }

        // Bind ScrollPane to automatically scroll to the bottom
        if (chatHistoryBox != null && chatScrollPane != null) {
            chatHistoryBox.heightProperty().addListener((obs, oldVal, newVal) -> {
                chatScrollPane.setVvalue(1.0);
            });
        } else {
            System.err.println("ChatbotController: chatHistoryBox or chatScrollPane not injected!"); // Debug
        }
    }

    // Helper to remove 'active' style safely (for sidebar buttons)
    private void removeActiveStyle(Button button) {
        if (button != null) {
            button.getStyleClass().remove("active");
        }
    }


    @FXML
    private void handleSendAction(ActionEvent event) {
        String userQuery = userInputField.getText().trim();
        if (userQuery.isEmpty()) {
            return;
        }
        System.out.println("ChatbotController: Sending user query: '" + userQuery + "'"); // Debug

        displayMessage("You", userQuery, true);
        userInputField.clear();
        userInputField.setDisable(true);
        sendButton.setDisable(true);
        loadingIndicator.setVisible(true);

        // Hide any previous status message by setting its text to empty (visibility controlled by FXML binding)
        if (statusMessage != null) {
            statusMessage.setText(""); // CORRECTED: Control visibility via text
        }


        // --- Call AI Service in Background ---
        CompletableFuture.supplyAsync(() -> {
                    System.out.println("ChatbotController: Calling ArticleAIService.getAnswer..."); // Debug
                    try {
                        return articleAIService.getAnswer(userQuery);
                    } catch (Exception e) {
                        System.err.println("ChatbotController: Error during AI service call: " + e.getMessage()); // Debug
                        e.printStackTrace();
                        return "Error: Unable to get a response from the AI service.";
                    }
                })
                .thenAcceptAsync(aiResponse -> {
                    System.out.println("ChatbotController: Received AI response. Updating UI."); // Debug
                    displayMessage("AI", aiResponse, false);
                    resetInputState();
                }, Platform::runLater)
                .exceptionally(ex -> {
                    System.err.println("ChatbotController: Unexpected error in CompletableFuture: " + ex.getMessage()); // Debug
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        displayMessage("AI", "An unexpected error occurred while processing your request.", false);
                        resetInputState();
                    });
                    return null;
                });
        // --- End Background Task ---
    }

    private void resetInputState() {
        Platform.runLater(() -> {
            System.out.println("ChatbotController: Resetting input state."); // Debug
            if (userInputField != null) userInputField.setDisable(false);
            if (sendButton != null) sendButton.setDisable(false);
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
            if (userInputField != null) userInputField.requestFocus();
            // Do NOT set statusMessage.setVisible(false) here either
        });
    }

    /**
     * Helper method to display messages in the chat history VBox.
     * @param sender The sender name ("You" or "AI").
     * @param message The message text.
     * @param isUser True if the message is from the user, false if from the AI.
     */
    private void displayMessage(String sender, String message, boolean isUser) {
        Platform.runLater(() -> {
            if (chatHistoryBox == null) {
                System.err.println("ChatbotController: chatHistoryBox is not injected!"); // Debug
                return;
            }

            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);

            // Ensure chatHistoryBox has been rendered to get its width for max width calculation
            if (chatHistoryBox.getScene() != null && chatHistoryBox.getScene().getWindow() != null && chatHistoryBox.getScene().getWindow().getWidth() > 0) {
                messageLabel.setMaxWidth(chatHistoryBox.getWidth() * 0.75); // Limit bubble width
            } else {
                // Fallback if width not available yet (e.g., during initialization)
                // This print might spam if called often before layout
                // System.out.println("ChatbotController: Using fallback max width for message label."); // Debug
                messageLabel.setMaxWidth(500); // Arbitrary max width
            }


            messageLabel.setPadding(new Insets(8, 12, 8, 12));
            messageLabel.getStyleClass().add("chat-bubble");

            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(0, 0, 5, 0));

            if (isUser) {
                messageLabel.getStyleClass().add("user-bubble");
                messageContainer.setAlignment(Pos.CENTER_RIGHT);
            } else {
                messageLabel.getStyleClass().add("ai-bubble");
                messageContainer.setAlignment(Pos.CENTER_LEFT);
            }

            messageContainer.getChildren().add(messageLabel);
            chatHistoryBox.getChildren().add(messageContainer);

            // Scroll to the bottom after adding a new message (already handled by listener)
            // if (chatScrollPane != null) chatScrollPane.setVvalue(1.0);
        });
    }

    /**
     * Sets the status/feedback message. Ensures UI update is on FX thread.
     */
    private void setStatus(String message, Color color) {
        Platform.runLater(() -> {
            if (statusMessage == null) { return; }

            statusMessage.setText(message); // Set the text
            statusMessage.setTextFill(color);
            // Visibility is now correctly controlled by FXML binding based on text (!message.isEmpty())
            // statusMessage.setVisible(true); // REMOVED

            statusMessage.getStyleClass().removeAll("success-message", "error-message", "info-message");
            if (color == Color.GREEN) statusMessage.getStyleClass().add("success-message");
            else if (color == Color.RED || color == Color.ORANGE) statusMessage.getStyleClass().add("error-message");
            else statusMessage.getStyleClass().add("info-message");

            // If text is set, the binding will make it visible. If text is empty, binding makes it invisible.
        });
    }


    // === Navigation Handlers (Ensure these exist and match FXML onAction) ===
    @FXML private void handleLogout(ActionEvent event) {
        System.out.println("ChatbotController: Logging out."); // Debug
        LoginController.logout();
    }

    @FXML private void navigateToDashboard(ActionEvent event) {
        System.out.println("ChatbotController: Navigating to Dashboard."); // Debug
        if (currentUser != null) {
            switch (currentUser.getRole()) {
                case ADMIN: WellTechApplication.loadFXML("adminDashboard"); break;
                case PSYCHIATRIST: WellTechApplication.loadFXML("psychiatristDashboard"); break;
                case PATIENT: WellTechApplication.loadFXML("patientDashboard"); break;
                default: WellTechApplication.loadFXML("login");
            }
        } else {
            WellTechApplication.loadFXML("login");
        }
    }

    @FXML private void navigateToArticles(ActionEvent event) {
        System.out.println("ChatbotController: Navigating to Articles."); // Debug
        WellTechApplication.loadFXML("articlesList");
    }

    @FXML private void navigateToConsultations(ActionEvent event) {
        System.out.println("ChatbotController: Navigating to Consultations."); // Debug
        WellTechApplication.loadFXML("consultationsList");
    }

    @FXML private void navigateToChatbot(ActionEvent event) {
        System.out.println("ChatbotController: Already on Chatbot page (reloading)."); // Debug
        WellTechApplication.loadFXML("chatbotView");
    }

    @FXML private void navigateToProfile(ActionEvent event) {
        System.out.println("ChatbotController: Navigating to Profile."); // Debug
        WellTechApplication.loadFXML("profile");
    }

    // Add other navigation methods if applicable
    // @FXML private void navigateToPatients(ActionEvent event) { ... }
    // @FXML private void navigateToMessages(ActionEvent event) { ... }
    // ==================================================================
}