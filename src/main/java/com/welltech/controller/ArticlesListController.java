package com.welltech.controller;

import com.welltech.WellTechApplication;
// DAOs and Models
import com.welltech.dao.ArticleDAO;
import com.welltech.dao.CategoryDAO;
import com.welltech.model.Article;
import com.welltech.model.Category;
import com.welltech.model.User;
// JavaFX Imports
import javafx.application.Platform;
import javafx.beans.binding.Bindings; // *** ADDED IMPORT ***
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle; // Import for clip shape
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
// Other Imports
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArticlesListController implements Initializable {

    // --- FXML Injections ---
    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    @FXML private Button dashboardButton;
    @FXML private Button articlesButton;
    @FXML private Button profileButton;
    @FXML private Button createArticleButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label statusLabel;
    @FXML private FlowPane articleContainer;

    // --- DAOs and Data ---
    private ArticleDAO articleDAO;
    private CategoryDAO categoryDAO;
    private User currentUser;
    private ObservableList<Article> allArticlesData = FXCollections.observableArrayList();
    private Article selectedArticle = null;
    private Node selectedArticleCard = null;

    // --- Formatting ---
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        articleDAO = new ArticleDAO();
        categoryDAO = new CategoryDAO();

        try {
            System.out.println("Initializing ArticlesListController (Card View - Reverted Style)");
            currentUser = LoginController.getCurrentUser();

            if (currentUser != null) {
                userNameLabel.setText("Welcome, " + currentUser.getFullName());
                setupFilterComboBox();
                setupSearchField();
                loadArticles(); // Initial load

                if (currentUser.getRole() == User.UserRole.PATIENT) {
                    createArticleButton.setVisible(false);
                    createArticleButton.setManaged(false);
                }
            } else {
                System.err.println("No user is logged in");
                WellTechApplication.loadFXML("login");
            }
        } catch (Exception e) {
            System.err.println("Error initializing ArticlesListController: " + e.getMessage());
            e.printStackTrace();
            setStatusLabel("Error loading articles view.", Color.RED);
        }
    }

    private void setupFilterComboBox() {
        filterComboBox.getItems().addAll("All", "Published", "Drafts");
        filterComboBox.setValue("All");
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayArticles());
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayArticles());
    }

    /**
     * Loads articles from the database based on user role. Handles "effectively final" error.
     * This populates the `allArticlesData` list.
     */
    private void loadArticles() {
        setStatusLabel("Loading articles...", Color.BLUE);
        Runnable fetchTask = () -> {
            List<Article> articlesFromDB = new ArrayList<>();
            try {
                if (currentUser.getRole() == User.UserRole.ADMIN) {
                    articlesFromDB = articleDAO.getAllArticles();
                } else if (currentUser.getRole() == User.UserRole.PSYCHIATRIST) {
                    List<Article> ownArticles = articleDAO.getArticlesByAuthor(currentUser.getId());
                    List<Article> publishedArticles = articleDAO.getPublishedArticles();
                    Set<Article> combinedSet = new LinkedHashSet<>(ownArticles);
                    combinedSet.addAll(publishedArticles);
                    articlesFromDB = new ArrayList<>(combinedSet);
                    articlesFromDB.sort((a1, a2) -> {
                        if (a1.getCreatedAt() == null && a2.getCreatedAt() == null) return 0;
                        if (a1.getCreatedAt() == null) return 1;
                        if (a2.getCreatedAt() == null) return -1;
                        return a2.getCreatedAt().compareTo(a1.getCreatedAt());
                    });
                } else { // PATIENT
                    articlesFromDB = articleDAO.getPublishedArticles();
                }

                final List<Article> finalArticlesForUI = articlesFromDB;
                Platform.runLater(() -> {
                    allArticlesData.setAll(finalArticlesForUI);
                    filterAndDisplayArticles();
                    String msg = allArticlesData.isEmpty() ? "No articles available." : allArticlesData.size() + " articles loaded.";
                    setStatusLabel(msg, Color.DARKSLATEGRAY);
                });

            } catch (Exception e) {
                System.err.println("Error loading articles in background: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> setStatusLabel("Failed to load articles.", Color.RED));
            }
        };
        new Thread(fetchTask).start();
    }


    private void filterAndDisplayArticles() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String filterValue = filterComboBox.getValue();
        Stream<Article> articleStream = allArticlesData.stream();

        if ("Published".equals(filterValue)) {
            articleStream = articleStream.filter(Article::isPublished);
        } else if ("Drafts".equals(filterValue)) {
            articleStream = articleStream.filter(article -> !article.isPublished());
        }

        if (!searchText.isEmpty()) {
            articleStream = articleStream.filter(article ->
                    article.getTitle().toLowerCase().contains(searchText) ||
                            (article.getContent() != null && article.getContent().toLowerCase().contains(searchText)) ||
                            (article.getAuthorName() != null && article.getAuthorName().toLowerCase().contains(searchText)) ||
                            (article.getCategory() != null && article.getCategory().getName().toLowerCase().contains(searchText))
            );
        }
        List<Article> filteredList = articleStream.collect(Collectors.toList());
        displayArticlesAsCards(filteredList);
    }

    private void displayArticlesAsCards(List<Article> articlesToDisplay) {
        articleContainer.getChildren().clear();
        if (articlesToDisplay.isEmpty()) {
            Label emptyLabel = new Label("No articles found matching your criteria.");
            emptyLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
            articleContainer.getChildren().add(emptyLabel);
        } else {
            for (Article article : articlesToDisplay) {
                Node articleCard = createArticleCard(article);
                articleContainer.getChildren().add(articleCard);
            }
        }
        String msg = articlesToDisplay.size() + " articles shown.";
        setStatusLabel(msg, Color.DARKSLATEGRAY);
        if (selectedArticle != null && articlesToDisplay.stream().noneMatch(a -> a.getId() == selectedArticle.getId())) {
            clearSelection();
        }
    }

    // Create card WITHOUT design-specific CSS classes
    private Node createArticleCard(Article article) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.getStyleClass().add("article-card");
        card.setUserData(article);
        card.setPrefWidth(280);
        card.setMinWidth(250);
        card.setAlignment(javafx.geometry.Pos.TOP_LEFT); // Set alignment

        // --- Start Image Handling ---
        Node imageNode;
        String imageUrlFromDB = article.getImageUrl();

        if (imageUrlFromDB != null && !imageUrlFromDB.trim().isEmpty()) {
            imageNode = createCardImageView(imageUrlFromDB);
            if (imageNode == null) { // Handle case where image loading failed
                Region placeholder = new Region();
                placeholder.setStyle("-fx-background-color: #e0e0e0; -fx-min-height: 120px; -fx-background-radius: 5px;");
                imageNode = placeholder;
            }
        } else {
            // Use placeholder if URL is null/empty
            Region placeholder = new Region();
            placeholder.setStyle("-fx-background-color: #e0e0e0; -fx-min-height: 120px; -fx-background-radius: 5px;");
            imageNode = placeholder;
        }
        // --- End Image Handling ---

        // --- Rest of Card Creation (Labels, Buttons, etc.) ---
        Label titleLabel = new Label(article.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        titleLabel.setWrapText(true);

        String authorDateText = "By " + (article.getAuthorName() != null ? article.getAuthorName() : "Unknown");
        if (article.getCreatedAt() != null) {
            authorDateText += " on " + article.getCreatedAt().format(DATE_FORMATTER);
        }
        Label authorDateLabel = new Label(authorDateText);
        authorDateLabel.setFont(Font.font("System", 11));
        authorDateLabel.setTextFill(Color.DARKSLATEGRAY);

        HBox categoryStatusBox = new HBox(8);
        categoryStatusBox.setPadding(new Insets(5, 0, 5, 0));
        Label categoryLabel = new Label(article.getCategory() != null ? article.getCategory().getName() : "Uncategorized");
        categoryLabel.setFont(Font.font("System", 10));

        Label statusDisplayLabel = new Label(article.isPublished() ? "Published" : "Draft");
        statusDisplayLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        statusDisplayLabel.setTextFill(article.isPublished() ? Color.GREEN : Color.ORANGE);
        categoryStatusBox.getChildren().addAll(categoryLabel, statusDisplayLabel);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox buttonBox = new HBox(8);
        Button viewBtn = new Button("View");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");

        viewBtn.setOnAction(event -> viewArticle(article));
        editBtn.setOnAction(event -> editArticle(article));
        deleteBtn.setOnAction(event -> deleteArticle(article));

        buttonBox.getChildren().add(viewBtn);
        boolean canModify = currentUser.getRole() == User.UserRole.ADMIN || article.getAuthorId() == currentUser.getId();
        if (canModify) {
            buttonBox.getChildren().addAll(editBtn, deleteBtn);
        }

        // Add elements in desired order
        card.getChildren().addAll(imageNode, titleLabel, authorDateLabel, categoryStatusBox, spacer, buttonBox);

        // Corrected Click Handler
        card.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Object target = event.getTarget();
            boolean handleSelection = true;
            if (target instanceof Node) {
                Node targetNode = (Node) target;
                Node currentNode = targetNode;
                while (currentNode != null && currentNode != card) {
                    if (currentNode instanceof Button) {
                        handleSelection = false;
                        break;
                    }
                    currentNode = currentNode.getParent();
                }
            } else {
                handleSelection = false;
            }
            if (handleSelection) {
                handleCardSelection(card, article);
            }
        });

        return card;
    }

    // Create ImageView, return null on failure
    private ImageView createCardImageView(String imageUrl) {
        ImageView imageView = new ImageView();
        try {
            Image img = new Image(imageUrl, true); // Background loading ONLY

            img.errorProperty().addListener((obs, oldError, newError) -> {
                if (newError) {
                    System.err.println("    ERROR detected via listener for URL: " + imageUrl);
                    if(img.getException() != null) img.getException().printStackTrace();
                }
            });
            img.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() == 1.0) {
                    System.out.println("    Image load complete (progress=1.0) for " + imageUrl + ". Final Width: " + img.getWidth());
                }
            });

            imageView.setImage(img);
            imageView.setFitWidth(270);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(imageView.fitWidthProperty()); // Bind to fitWidth
            // *** CORRECTED BINDING FOR JAVAFX 11 ***
            clip.heightProperty().bind(Bindings.createDoubleBinding(
                    () -> imageView.getLayoutBounds().getHeight(), // Supplier function
                    imageView.layoutBoundsProperty()              // Dependency
            ));
            // *** END OF CORRECTION ***
            clip.setArcWidth(10);
            clip.setArcHeight(10);
            imageView.setClip(clip);

            return imageView;

        } catch (Exception e) {
            System.err.println("    EXCEPTION during ImageView setup for URL: " + imageUrl + " - " + e.getClass().getName() + ": " + e.getMessage());
            return null; // Return null to indicate failure
        }
    }

    private void handleCardSelection(Node cardNode, Article article) {
        clearSelectionStyle();
        cardNode.getStyleClass().add("article-card-selected");
        selectedArticleCard = cardNode;
        selectedArticle = article;
    }

    private void clearSelectionStyle() {
        if (selectedArticleCard != null) {
            selectedArticleCard.getStyleClass().remove("article-card-selected");
            selectedArticleCard = null;
        }
    }

    private void clearSelection() {
        clearSelectionStyle();
        selectedArticle = null;
    }

    // View Article Dialog
    private void viewArticle(Article article) {
        if (article == null) return;
        Alert viewAlert = new Alert(Alert.AlertType.INFORMATION);
        viewAlert.setTitle("View Article");
        viewAlert.setHeaderText(article.getTitle());
        VBox contentBox = new VBox(10);
        Label metaLabel = new Label("By: " + article.getAuthorName() +
                " | Category: " + (article.getCategory() != null ? article.getCategory().getName() : "None") +
                " | Status: " + (article.isPublished() ? "Published" : "Draft"));
        metaLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
        TextArea contentArea = new TextArea(article.getContent());
        contentArea.setWrapText(true);
        contentArea.setEditable(false);
        contentArea.setPrefRowCount(15);
        contentArea.setMaxWidth(Double.MAX_VALUE);
        contentBox.getChildren().addAll(metaLabel, contentArea);
        viewAlert.getDialogPane().setContent(contentBox);
        viewAlert.setResizable(true);
        viewAlert.getDialogPane().setPrefSize(600, 450);
        viewAlert.showAndWait();
        clearSelection();
    }

    // Edit Article Navigation
    private void editArticle(Article article) {
        if (article == null) return;
        WellTechApplication.setCurrentArticle(article);
        WellTechApplication.loadFXML("articleEdit");
    }

    // Delete Article Logic
    private void deleteArticle(Article article) {
        if (article == null) return;
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Article: " + article.getTitle());
        confirmAlert.setContentText("Are you sure you want to permanently delete this article?");
        ButtonType okButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(okButton, cancelButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == okButton) {
            setStatusLabel("Deleting article...", Color.ORANGE);
            Runnable deleteTask = () -> {
                boolean deleted = articleDAO.deleteArticle(article.getId());
                Platform.runLater(() -> {
                    if (deleted) {
                        setStatusLabel("Article '" + article.getTitle() + "' deleted.", Color.RED);
                        allArticlesData.remove(article);
                        filterAndDisplayArticles(); // Refresh the view
                    } else {
                        setStatusLabel("Failed to delete article.", Color.RED);
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Deletion Failed");
                        errorAlert.setContentText("Could not delete the article from the database.");
                        errorAlert.showAndWait();
                    }
                });
            };
            new Thread(deleteTask).start();
        } else {
            clearSelection();
        }
    }

    // Create Article Navigation
    @FXML
    private void handleCreateArticle(ActionEvent event) {
        WellTechApplication.setCurrentArticle(null);
        WellTechApplication.loadFXML("articleEdit");
    }

    // Simplified Feedback Method
    private void setStatusLabel(String message, Color color) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setTextFill(color);
        });
    }

    // --- Navigation Handlers ---
    @FXML private void navigateToDashboard(ActionEvent event) {
        if (currentUser != null) {
            switch (currentUser.getRole()) {
                case ADMIN: WellTechApplication.loadFXML("adminDashboard"); break;
                case PSYCHIATRIST: WellTechApplication.loadFXML("psychiatristDashboard"); break;
                case PATIENT: WellTechApplication.loadFXML("patientDashboard"); break;
                default: WellTechApplication.loadFXML("login");
            }
        } else { WellTechApplication.loadFXML("login");}
    }
    @FXML private void navigateToProfile(ActionEvent event) { WellTechApplication.loadFXML("profile"); }
    @FXML private void handleLogout(ActionEvent event) { LoginController.logout(); }
}