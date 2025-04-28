package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.ArticleDAO;
import com.welltech.model.User;
import com.welltech.service.ArticleAIService;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node; // Needed for javafx.scene.Node
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
// === Keep Text and TextFlow imports if you were using them before WebView ===
// Otherwise, remove them if not used elsewhere
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
// ===========================================================================
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.util.Duration;

// === REMOVE flexmark Imports ===
// import com.vladsch.flexmark.html.HtmlRenderer;
// import com.vladsch.flexmark.parser.Parser;
// import com.vladsch.flexmark.util.ast.Node;
// import com.vladsch.flexmark.util.data.MutableDataSet;
// ... remove any other flexmark imports ...
// ===============================


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
    @FXML private Label typingIndicator;
    private Timeline typingAnimation;
    private boolean typingActive = false;
    // --- End Chat UI Injections ---

    // === REMOVE flexmark Setup fields ===
    // private Parser markdownParser;
    // private HtmlRenderer htmlRenderer;
    // private MutableDataSet flexmarkOptions;
    // ====================================

    // --- Data ---
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

        // === REMOVE flexmark Initialization ===
        // flexmarkOptions = new MutableDataSet();
        // markdownParser = Parser.builder(flexmarkOptions).build();
        // htmlRenderer = HtmlRenderer.builder(flexmarkOptions).build();
        // ======================================


        // === Sidebar Button Styling ===
        if (dashboardButton != null) removeActiveStyle(dashboardButton);
        if (articlesButton != null) removeActiveStyle(articlesButton);
        if (consultationsButton != null) removeActiveStyle(consultationsButton);
        if (chatbotButton != null) removeActiveStyle(chatbotButton);
        if (profileButton != null) removeActiveStyle(profileButton);

        if (chatbotButton != null) {
            chatbotButton.getStyleClass().add("active");
        }

        if (userNameLabel != null) {
            userNameLabel.setText("Welcome, " + currentUser.getFullName());
        }

        loadingIndicator.setVisible(false);

        // Ensure status message label starts hidden by setting its text empty (handled by FXML binding)
        if (statusMessage != null) {
            // statusMessage.managedProperty().bind(statusMessage.visibleProperty()); // This binding is typically in FXML
            statusMessage.setText(""); // Control visibility via text
        }

        // Bind ScrollPane to automatically scroll to the bottom
        if (chatHistoryBox != null && chatScrollPane != null) {
            chatHistoryBox.heightProperty().addListener((obs, oldVal, newVal) -> {
                chatScrollPane.setVvalue(1.0);
            });
        } else {
            System.err.println("ChatbotController: chatHistoryBox or chatScrollPane not injected!");
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
        System.out.println("ChatbotController: Sending user query: '" + userQuery + "'");

        displayMessage("You", userQuery, true); // Display user's message immediately

        playSound("send.wav"); // <-- Play send sound

        userInputField.clear();
        userInputField.setDisable(true);
        sendButton.setDisable(true);
        loadingIndicator.setVisible(true);

        if (statusMessage != null) {
            statusMessage.setText(""); // Hide previous status messages
        }

        startTypingAnimation();

        CompletableFuture.supplyAsync(() -> {
                    System.out.println("ChatbotController: Calling ArticleAIService.getAnswer...");
                    try {
                        // Simulate a delay for even more real typing effect if you want (optional):
                        // Thread.sleep(1000); // <- Adds 1 second fake delay
                        return articleAIService.getAnswer(userQuery);
                    } catch (Exception e) {
                        System.err.println("ChatbotController: Error during AI service call: " + e.getMessage());
                        e.printStackTrace();
                        return "Error: Unable to get a response from the AI service.";
                    }
                })
                .thenAcceptAsync(aiResponse -> {
                    System.out.println("ChatbotController: Received AI response. Updating UI.");

                    stopTypingAnimation();


                    displayMessage("AI", aiResponse, false); // Show AI response
                    playSound("receive.wav");
                    resetInputState(); // Reset input field and send button
                }, Platform::runLater)
                .exceptionally(ex -> {
                    System.err.println("ChatbotController: Unexpected error in CompletableFuture: " + ex.getMessage());
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        if (typingIndicator != null) {
                            typingIndicator.setVisible(false); // Hide typing if error
                        }
                        displayMessage("AI", "⚠️ Oops! allah ghaleb... try again later! ", false);
                        shakeChatHistoryBox(); // <-- Add this line to SHAKE the chat box
                        flashRedGlow();
                        resetInputState();
                    });
                    return null;
                });
    }
    private void flashRedGlow() {
        if (chatHistoryBox == null) return;

        DropShadow redGlow = new DropShadow();
        redGlow.setColor(Color.RED);
        redGlow.setRadius(20);
        redGlow.setSpread(0.6);

        chatHistoryBox.setEffect(redGlow);

        PauseTransition pause = new PauseTransition(Duration.millis(300)); // Glow duration
        pause.setOnFinished(event -> chatHistoryBox.setEffect(null)); // Remove glow after pause
        pause.play();
    }


    private void resetInputState() {
        Platform.runLater(() -> {
            System.out.println("ChatbotController: Resetting input state.");
            if (userInputField != null) userInputField.setDisable(false);
            if (sendButton != null) sendButton.setDisable(false);
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
            if (userInputField != null) userInputField.requestFocus();
        });
    }


    /**
     * Helper method to display messages in the chat history VBox.
     * Now uses Label for BOTH user and AI messages (plain text).
     * @param sender The sender name ("You" or "AI").
     * @param message The message text (plain text expected for AI now).
     * @param isUser True if the message is from the user, false if from the AI.
     */
    private void displayMessage(String sender, String message, boolean isUser) {
        Platform.runLater(() -> {
            if (chatHistoryBox == null) {
                System.err.println("ChatbotController: chatHistoryBox is not injected!");
                return;
            }

            // Create container for message
            HBox messageContainer = new HBox(10); // spacing 10px between icon and text
            messageContainer.setPadding(new Insets(5));
            messageContainer.getStyleClass().add("chat-bubble-container");

            // Create ImageView for user/AI icon
            ImageView icon = new ImageView();
            String iconPath = isUser ? "/images/user_icon.png" : "/images/robot_icon.png"; // adjust paths
            icon.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(iconPath)));
            icon.setFitHeight(32);
            icon.setFitWidth(32);
            icon.setPreserveRatio(true);

            // Create the message label
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.getStyleClass().addAll("chat-bubble", isUser ? "user-bubble" : "ai-bubble");

            double maxWidth = 400; // maximum width of the message
            if (chatHistoryBox.getScene() != null && chatHistoryBox.getScene().getWindow() != null && chatHistoryBox.getScene().getWindow().getWidth() > 0) {
                maxWidth = chatHistoryBox.getWidth() * 0.65;
            }
            messageLabel.setMaxWidth(maxWidth);

            if (isUser) {
                messageContainer.setAlignment(Pos.CENTER_RIGHT);
                messageContainer.getChildren().addAll(messageLabel, icon); // Label first, then icon for user
            } else {
                messageContainer.setAlignment(Pos.CENTER_LEFT);
                messageContainer.getChildren().addAll(icon, messageLabel); // Icon first, then label for AI
            }

            chatHistoryBox.getChildren().add(messageContainer);


// === ADD THIS FADE IN ANIMATION ===
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), messageContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), messageContainer);
            scaleUp.setFromX(0.8);
            scaleUp.setFromY(0.8);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), messageContainer);
            scaleDown.setFromX(1.05);
            scaleDown.setFromY(1.05);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);

// === Play fade + bounce together ===
            SequentialTransition bounceAndFade = new SequentialTransition(fadeIn, scaleUp, scaleDown);
            bounceAndFade.play();
        });
    }


    // === REMOVE convertMarkdownToHtml method ===
    /*
    private String convertMarkdownToHtml(String markdown) {
        if (markdown == null) return "";
        com.vladsch.flexmark.util.ast.Node document = markdownParser.parse(markdown);
        String html = htmlRenderer.render(document);
        String styledHtml = "<html><head><style>" +
                           "body { font-family: 'System', 'Arial', sans-serif; font-size: 1em; margin: 0; padding: 0; }" +
                           "p { margin-bottom: 0.5em; }" +
                           "ul, ol { margin-top: 0.5em; margin-bottom: 0.5em; padding-left: 20px; }" +
                           "li { margin-bottom: 0.3em; }" +
                           "strong, b { font-weight: bold; }" +
                           "em, i { font-style: italic; }" +
                           "</style></head><body>" + html + "</body></html>";

        return styledHtml;
    }
    */
    // =========================================


    /**
     * Sets the status/feedback message. Ensures UI update is on FX thread.
     */
    private void setStatus(String message, Color color) {
        Platform.runLater(() -> {
            if (statusMessage == null) { return; }

            statusMessage.setText(message);
            statusMessage.setTextFill(color);
            // Visibility is correctly controlled by FXML binding based on text (!message.isEmpty())

            statusMessage.getStyleClass().removeAll("success-message", "error-message", "info-message");
            if (color == Color.GREEN) statusMessage.getStyleClass().add("success-message");
            else if (color == Color.RED || color == Color.ORANGE) statusMessage.getStyleClass().add("error-message");
            else statusMessage.getStyleClass().add("info-message");

        });
    }

    private void startTypingAnimation() {
        typingIndicator.setVisible(true);
        typingActive = true;

        typingAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> typingIndicator.setText("AI is typing")),
                new KeyFrame(Duration.seconds(0.5), e -> typingIndicator.setText("AI is typing.")),
                new KeyFrame(Duration.seconds(1.0), e -> typingIndicator.setText("AI is typing..")),
                new KeyFrame(Duration.seconds(1.5), e -> typingIndicator.setText("AI is typing..."))
        );
        typingAnimation.setCycleCount(Timeline.INDEFINITE);
        typingAnimation.play();
    }

    private void stopTypingAnimation() {
        typingActive = false;
        if (typingAnimation != null) {
            typingAnimation.stop();
        }
        typingIndicator.setVisible(false);
    }
    private void playSound(String soundFileName) {
        try {
            AudioClip clip = new AudioClip(getClass().getResource("/sounds/" + soundFileName).toExternalForm());
            clip.play();
        } catch (Exception e) {
            System.err.println("ChatbotController: Failed to play sound - " + e.getMessage());
        }
    }
    private void shakeChatHistoryBox() {
        if (chatHistoryBox == null) return;

        TranslateTransition tt = new TranslateTransition(Duration.millis(70), chatHistoryBox);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }


    // === Navigation Handlers ===
    @FXML private void handleLogout(ActionEvent event) { System.out.println("ChatbotController: Logging out."); LoginController.logout(); }
    @FXML private void navigateToDashboard(ActionEvent event) { System.out.println("ChatbotController: Navigating to Dashboard."); if (currentUser != null) { switch (currentUser.getRole()) { case ADMIN: WellTechApplication.loadFXML("adminDashboard"); break; case PSYCHIATRIST: WellTechApplication.loadFXML("psychiatristDashboard"); break; case PATIENT: WellTechApplication.loadFXML("patientDashboard"); break; default: WellTechApplication.loadFXML("login"); } } else { WellTechApplication.loadFXML("login"); }}
    @FXML private void navigateToArticles(ActionEvent event) { System.out.println("ChatbotController: Navigating to Articles."); WellTechApplication.loadFXML("articlesList"); }
    @FXML private void navigateToConsultations(ActionEvent event) { System.out.println("ChatbotController: Navigating to Consultations."); WellTechApplication.loadFXML("consultationsList"); }
    @FXML private void navigateToChatbot(ActionEvent event) { System.out.println("ChatbotController: Already on Chatbot page (reloading)."); WellTechApplication.loadFXML("chatbotView"); }
    @FXML private void navigateToProfile(ActionEvent event) { System.out.println("ChatbotController: Navigating to Profile."); WellTechApplication.loadFXML("profile"); }
    // Add other navigation methods if applicable
}