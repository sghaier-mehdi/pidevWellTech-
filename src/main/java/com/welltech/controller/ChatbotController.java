package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.ArticleDAO;
import com.welltech.model.User;
import com.welltech.service.ArticleAIService;
import com.welltech.util.SimpleTTS;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import javafx.animation.AnimationTimer;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ChatbotController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Button logoutButton, dashboardButton, articlesButton, consultationsButton, chatbotButton, profileButton;
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatHistoryBox;
    @FXML private TextField userInputField;
    @FXML private Button sendButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusMessage;
    @FXML private Label typingIndicator;
    @FXML private Canvas particleCanvas;
    // === Add this field ===
    private boolean isReading = false;

    private Timeline typingAnimation;
    private boolean typingActive = false;
    private final Random random = new Random();
    private List<Particle> particles = new ArrayList<>();
    private ArticleAIService articleAIService;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ArticleDAO articleDAO = new ArticleDAO();
        this.articleAIService = new ArticleAIService(articleDAO);
        currentUser = LoginController.getCurrentUser();

        if (currentUser == null) {
            WellTechApplication.loadFXML("login");
            return;
        }

        userNameLabel.setText("Welcome, " + currentUser.getFullName());
        loadingIndicator.setVisible(false);
        statusMessage.setText("");

        removeActiveStyle(dashboardButton);
        removeActiveStyle(articlesButton);
        removeActiveStyle(consultationsButton);
        removeActiveStyle(profileButton);
        chatbotButton.getStyleClass().add("active");

        chatHistoryBox.heightProperty().addListener((obs, oldVal, newVal) -> chatScrollPane.setVvalue(1.0));

        if (particleCanvas != null) {
            startParticleAnimation();
        }
    }

    private void removeActiveStyle(Button button) {
        if (button != null) {
            button.getStyleClass().remove("active");
        }
    }

    @FXML
    private void handleSendAction(ActionEvent event) {
        String userQuery = userInputField.getText().trim();
        if (userQuery.isEmpty()) return;

        displayMessage("You", userQuery, true);
        playSound("send.wav");

        userInputField.clear();
        userInputField.setDisable(true);
        sendButton.setDisable(true);
        loadingIndicator.setVisible(true);
        statusMessage.setText("");

        startTypingAnimation();

        CompletableFuture.supplyAsync(() -> {
            try {
                return articleAIService.getAnswer(userQuery);
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: Unable to get a response from the AI.";
            }
        }).thenAcceptAsync(aiResponse -> {
            stopTypingAnimation();
            displayMessage("AI", aiResponse, false);
            playSound("receive.wav");
            resetInputState();
        }, Platform::runLater).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> {
                stopTypingAnimation();
                displayMessage("AI", "⚠️ Oops! Please try again later!", false);
                shakeChatHistoryBox();
                flashRedGlow();
                resetInputState();
            });
            return null;
        });
    }

    private void displayMessage(String sender, String message, boolean isUser) {
        Platform.runLater(() -> {
            HBox messageContainer = new HBox(10);
            messageContainer.setPadding(new Insets(5));
            messageContainer.getStyleClass().add("chat-bubble-container");

            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(isUser ? "/images/user_icon.png" : "/images/robot_icon.png")));
            icon.setFitHeight(32);
            icon.setFitWidth(32);
            icon.setPreserveRatio(true);

            double maxWidth = 400;
            if (chatHistoryBox.getScene() != null) {
                maxWidth = chatHistoryBox.getWidth() * 0.65;
            }

            if (isUser) {
                Label userLabel = new Label(message);
                userLabel.setWrapText(true);
                userLabel.setMaxWidth(maxWidth);
                userLabel.getStyleClass().addAll("chat-bubble", "user-bubble");

                messageContainer.setAlignment(Pos.CENTER_RIGHT);
                messageContainer.getChildren().addAll(userLabel, icon);

            } else {
                TextFlow textFlow = new TextFlow();
                textFlow.setMaxWidth(maxWidth);
                textFlow.getStyleClass().addAll("chat-bubble", "ai-bubble");

                for (String word : message.split(" ")) {
                    Text textNode = new Text(word + " ");
                    textNode.getStyleClass().add("chat-word");
                    textFlow.getChildren().add(textNode);
                }

                Button readButton = new Button("🔊");
                readButton.setStyle(
                        "-fx-background-color: #87d8b0;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 20;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 5 10 5 10;" +
                                "-fx-cursor: hand;"
                );
                readButton.setOnAction(e -> highlightTextFlowAndSpeak(textFlow));

                Button muteButton = new Button("🔇");
                muteButton.setStyle(
                        "-fx-background-color: #e88080;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 20;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 5 10 5 10;" +
                                "-fx-cursor: hand;"
                );
                muteButton.setOnAction(e -> {
                    isReading = false;
                    SimpleTTS.stop();
                });


                HBox aiBox = new HBox(5, icon, textFlow, readButton, muteButton);


                aiBox.setAlignment(Pos.CENTER_LEFT);

                messageContainer.getChildren().add(aiBox);
            }

            chatHistoryBox.getChildren().add(messageContainer);

            FadeTransition fade = new FadeTransition(Duration.millis(500), messageContainer);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }

    private void highlightTextFlowAndSpeak(TextFlow textFlow) {
        isReading = true;  // Start reading
        new Thread(() -> {
            StringBuilder sentence = new StringBuilder();
            for (Node node : textFlow.getChildren()) {
                if (node instanceof Text textNode) {
                    sentence.append(textNode.getText());
                }
            }

            SimpleTTS.speak(sentence.toString());

            for (Node node : textFlow.getChildren()) {
                if (!isReading) break;  // Stop immediately if muted
                if (node instanceof Text textNode) {
                    Platform.runLater(() -> textNode.setFill(Color.GREEN));
                    try {
                        Thread.sleep(600); // Adjust timing if needed
                    } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> textNode.setFill(Color.BLACK));
                }
            }
        }).start();
    }


    private void startTypingAnimation() {
        typingIndicator.setVisible(true);
        typingAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> typingIndicator.setText("AI is typing")),
                new KeyFrame(Duration.seconds(0.5), e -> typingIndicator.setText("AI is typing.")),
                new KeyFrame(Duration.seconds(1), e -> typingIndicator.setText("AI is typing..")),
                new KeyFrame(Duration.seconds(1.5), e -> typingIndicator.setText("AI is typing..."))
        );
        typingAnimation.setCycleCount(Animation.INDEFINITE);
        typingAnimation.play();
    }

    private void stopTypingAnimation() {
        if (typingAnimation != null) typingAnimation.stop();
        typingIndicator.setVisible(false);
    }

    private void resetInputState() {
        Platform.runLater(() -> {
            userInputField.setDisable(false);
            sendButton.setDisable(false);
            loadingIndicator.setVisible(false);
            userInputField.requestFocus();
        });
    }

    private void flashRedGlow() {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.RED);
        glow.setRadius(20);
        glow.setSpread(0.6);
        chatHistoryBox.setEffect(glow);

        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> chatHistoryBox.setEffect(null));
        pause.play();
    }

    private void shakeChatHistoryBox() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(70), chatHistoryBox);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void playSound(String soundFile) {
        try {
            AudioClip clip = new AudioClip(getClass().getResource("/sounds/" + soundFile).toExternalForm());
            clip.play();
        } catch (Exception e) {
            System.err.println("Sound error: " + e.getMessage());
        }
    }

    private static class Particle {
        double x, y, radius, speedY;
        Particle(double x, double y, double radius, double speedY) {
            this.x = x; this.y = y; this.radius = radius; this.speedY = speedY;
        }
    }

    private void startParticleAnimation() {
        if (particleCanvas == null) return;

        particleCanvas.widthProperty().bind(((Region) particleCanvas.getParent()).widthProperty());
        particleCanvas.heightProperty().bind(((Region) particleCanvas.getParent()).heightProperty());

        particles.clear();
        for (int i = 0; i < 100; i++) {
            particles.add(new Particle(random.nextDouble() * 1000, random.nextDouble() * 768, 1.5 + random.nextDouble() * 2.5, 0.3 + random.nextDouble() * 0.7));
        }

        GraphicsContext gc = particleCanvas.getGraphicsContext2D();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.setFill(Paint.valueOf("rgba(238,245,255,0.2)"));
                gc.fillRect(0, 0, particleCanvas.getWidth(), particleCanvas.getHeight());
                gc.setFill(Paint.valueOf("rgba(150,180,255,0.8)"));
                for (Particle p : particles) {
                    gc.fillOval(p.x, p.y, p.radius, p.radius);
                    p.y += p.speedY;
                    if (p.y > particleCanvas.getHeight()) {
                        p.y = 0;
                        p.x = random.nextDouble() * particleCanvas.getWidth();
                    }
                }
            }
        }.start();
    }

    @FXML private void handleLogout(ActionEvent event) { LoginController.logout(); }
    @FXML private void navigateToDashboard(ActionEvent event) { WellTechApplication.loadFXML("dashboard"); }
    @FXML private void navigateToArticles(ActionEvent event) { WellTechApplication.loadFXML("articlesList"); }
    @FXML private void navigateToConsultations(ActionEvent event) { WellTechApplication.loadFXML("consultationsList"); }
    @FXML private void navigateToChatbot(ActionEvent event) { WellTechApplication.loadFXML("chatbotView"); }
    @FXML private void navigateToProfile(ActionEvent event) { WellTechApplication.loadFXML("profile"); }
}
