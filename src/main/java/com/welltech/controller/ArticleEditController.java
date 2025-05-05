package com.welltech.controller;

import com.welltech.WellTechApplication;
// DAOs
import com.welltech.dao.ArticleDAO;
import com.welltech.dao.CategoryDAO; // *** Import CategoryDAO ***
// Models
import com.welltech.model.Article;
import com.welltech.model.Category;   // *** Import Category ***
import com.welltech.model.User;
// JavaFX
import com.welltech.model.UserRole;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList; // *** Import ObservableList ***
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;      // For status message colors
import javafx.scene.text.Text;
// Other
import java.net.URL;
import java.util.List;
import java.util.Optional; // For TextInputDialog
import java.util.ResourceBundle;

public class ArticleEditController implements Initializable {

    // --- FXML Injections ---
    // Navbar/Sidebar (Keep as is)
    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    @FXML private Button dashboardButton;
    @FXML private Button articlesButton;
    @FXML private Button profileButton;

    // Form Fields
    @FXML private Text pageTitle;
    @FXML private TextField titleField;
    @FXML private ComboBox<Category> categoryComboBox; // *** CHANGED: Use Category object ***
    @FXML private Button addCategoryButton;         // *** ADDED: Button to add new category ***
    @FXML private TextArea contentArea;
    @FXML private CheckBox publishCheckBox;
    @FXML private TextField imageUrlField;         // *** ADDED: Field for Image URL ***

    // Buttons & Feedback
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label titleError;
    @FXML private Label contentError;
    @FXML private Label statusMessage;

    // --- DAOs and Data ---
    private ArticleDAO articleDAO;
    private CategoryDAO categoryDAO;             // *** ADDED: CategoryDAO instance ***
    private User currentUser;
    private Article currentArticle;              // Article being edited (null if creating)
    private boolean isEditMode = false;

    // ObservableList to hold categories for the ComboBox
    private ObservableList<Category> categoryList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        articleDAO = new ArticleDAO();
        categoryDAO = new CategoryDAO(); // *** Initialize CategoryDAO ***

        try {
            System.out.println("Initializing ArticleEditController");
            currentUser = LoginController.getCurrentUser();

            if (currentUser != null) {
                userNameLabel.setText("Welcome, " + currentUser.getFullName());

                setupCategoryComboBox(); // Setup ComboBox first
                loadCategories();        // Load data into it

                setupErrorClearListeners();

                currentArticle = WellTechApplication.getCurrentArticle(); // Get article passed from list view

                if (currentArticle != null) {
                    // Edit Mode
                    isEditMode = true;
                    pageTitle.setText("Edit Article");
                    populateArticleFields();
                } else {
                    // Create Mode
                    isEditMode = false;
                    pageTitle.setText("Create New Article");
                    publishCheckBox.setSelected(false); // Default for new article
                    // Optionally clear imageUrlField here if needed
                }

                // Permission check (Keep as is)
                if (currentUser.getRole() == UserRole.PATIENT) {
                    setFeedback("You don't have permission to create or edit articles", Color.RED);
                    saveButton.setDisable(true);
                    addCategoryButton.setDisable(true); // Disable category add for patients too
                }

            } else {
                System.err.println("No user is logged in for ArticleEditController");
                WellTechApplication.loadFXML("login");
            }

        } catch (Exception e) {
            System.err.println("Error initializing ArticleEditController: " + e.getMessage());
            e.printStackTrace();
            setFeedback("Error loading editor: " + e.getMessage(), Color.RED);
        }
    }

    /**
     * Configures the Category ComboBox properties.
     */
    private void setupCategoryComboBox() {
        // Set the items to the observable list (data loaded later)
        categoryComboBox.setItems(categoryList);

        // How to display Category objects: uses category.toString() by default.
        // If toString() isn't suitable, use setCellFactory and setButtonCell:
        /*
        categoryComboBox.setCellFactory(lv -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                setText(empty ? null : category.getName());
            }
        });
        categoryComboBox.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                setText(empty ? null : (category != null ? category.getName() : "Select category..."));
            }
        });
        */
        // Add a listener to clear status when selection changes
        categoryComboBox.valueProperty().addListener((obs, old, newVal) -> {
            statusMessage.setText("");
        });
    }

    /**
     * Loads categories from the database into the categoryList.
     * Should run on a background thread if list can be large.
     */
    private void loadCategories() {
        // Consider background thread for large lists
        setFeedback("Loading categories...", Color.BLUE);
        Runnable loadTask = () -> {
            try {
                List<Category> fetchedCategories = categoryDAO.getAllCategories();
                Platform.runLater(()-> {
                    categoryList.setAll(fetchedCategories); // Update the list for ComboBox
                    // Optional: Add a "None" option if needed, represented by null selection
                    // categoryComboBox.getItems().add(0, null); // Requires custom cell factory to display "None"

                    // If editing, re-select the category after loading list
                    if (isEditMode && currentArticle != null && currentArticle.getCategory() != null) {
                        categoryComboBox.setValue(currentArticle.getCategory());
                    }
                    setFeedback("Categories loaded.", Color.GREEN); // Clear loading message
                });
            } catch (Exception e) {
                System.err.println("Error loading categories: " + e.getMessage());
                Platform.runLater(()-> setFeedback("Failed to load categories.", Color.RED));
            }
        };
        new Thread(loadTask).start(); // Run in background
    }


    /**
     * Set up listeners to clear error messages (add imageUrlField).
     */
    private void setupErrorClearListeners() {
        titleField.textProperty().addListener((obs, old, newVal) -> clearError(titleError));
        contentArea.textProperty().addListener((obs, old, newVal) -> clearError(contentError));
        imageUrlField.textProperty().addListener((obs, old, newVal) -> clearStatus()); // Image URL is optional
        publishCheckBox.selectedProperty().addListener((obs, old, newVal) -> clearStatus());
        categoryComboBox.valueProperty().addListener((obs, old, newVal) -> clearStatus());
    }

    /** Helper to clear a specific error label and the main status */
    private void clearError(Label errorLabel) {
        errorLabel.setText("");
        statusMessage.setText("");
    }
    /** Helper to clear only the main status message */
    private void clearStatus() {
        statusMessage.setText("");
    }


    /**
     * Populate form fields with current article data.
     */
    private void populateArticleFields() {
        if (currentArticle == null) return;

        titleField.setText(currentArticle.getTitle());
        contentArea.setText(currentArticle.getContent());

        // Set Category ComboBox value - relies on Category.equals()
        // This needs to happen *after* categories are loaded if load is async
        // It's handled in loadCategories() success callback now.
        // categoryComboBox.setValue(currentArticle.getCategory());

        publishCheckBox.setSelected(currentArticle.isPublished());
        imageUrlField.setText(currentArticle.getImageUrl() != null ? currentArticle.getImageUrl() : ""); // Populate image URL
    }

    /**
     * Handle Add New Category button click.
     */
    @FXML
    private void handleAddCategory(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Category");
        dialog.setHeaderText("Enter the name for the new category:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            if (name.trim().isEmpty()) {
                setFeedback("Category name cannot be empty.", Color.ORANGE);
                return;
            }
            // Check if category already exists (optional but good)
            boolean exists = categoryList.stream().anyMatch(cat -> cat.getName().equalsIgnoreCase(name.trim()));
            if (exists) {
                setFeedback("Category '" + name.trim() + "' already exists.", Color.ORANGE);
                // Select the existing one
                categoryList.stream()
                        .filter(cat -> cat.getName().equalsIgnoreCase(name.trim()))
                        .findFirst()
                        .ifPresent(existingCat -> categoryComboBox.setValue(existingCat));
                return;
            }


            Category newCategory = new Category(name.trim());
            // Add to DB (consider background thread for robustness)
            boolean success = categoryDAO.addCategory(newCategory);

            if (success) {
                setFeedback("Category '" + newCategory.getName() + "' added.", Color.GREEN);
                // Add to list and select it (ID should be set by DAO)
                categoryList.add(newCategory);
                categoryComboBox.setValue(newCategory);
            } else {
                setFeedback("Failed to add category (maybe it already exists in DB?).", Color.RED);
            }
        });
    }

    /**
     * Handle save button click.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        clearStatus(); // Clear previous messages
        if (!validateForm()) {
            setFeedback("Please fix the errors above.", Color.RED);
            return;
        }

        // Get values from form
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        Category selectedCategory = categoryComboBox.getValue(); // *** Get Category object ***
        boolean isPublished = publishCheckBox.isSelected();
        String imageUrl = imageUrlField.getText() != null ? imageUrlField.getText().trim() : null; // Get image URL

        // Show loading/saving feedback
        setFeedback("Saving article...", Color.BLUE);
        saveButton.setDisable(true); // Prevent double clicks

        // Prepare article object
        Article articleToSave;
        if (isEditMode) {
            articleToSave = currentArticle; // Modify existing object
        } else {
            articleToSave = new Article(); // Create new object
            articleToSave.setAuthorId(currentUser.getId()); // Set author only for new articles
        }

        articleToSave.setTitle(title);
        articleToSave.setContent(content);
        articleToSave.setCategory(selectedCategory); // *** Set Category object ***
        articleToSave.setPublished(isPublished);
        articleToSave.setImageUrl(imageUrl); // Set image URL

        // --- Perform DB operation in background ---
        Runnable saveTask = () -> {
            boolean success = false;
            try {
                if (isEditMode) {
                    success = articleDAO.updateArticle(articleToSave);
                } else {
                    int newId = articleDAO.insertArticle(articleToSave);
                    success = newId > 0;
                    if(success) {
                        articleToSave.setId(newId); // Update object with new ID if needed
                    }
                }

                boolean finalSuccess = success; // Effectively final for lambda
                Platform.runLater(() -> { // Update UI on FX thread
                    if (finalSuccess) {
                        String action = isEditMode ? "updated" : "created";
                        setFeedback("Article " + action + " successfully!", Color.GREEN);
                        // Optionally navigate back after a short delay or immediately
                        navigateToArticles(null); // Go back to list view
                    } else {
                        String action = isEditMode ? "update" : "create";
                        setFeedback("Failed to " + action + " article.", Color.RED);
                        saveButton.setDisable(false); // Re-enable button on failure
                    }
                });

            } catch (Exception e) {
                System.err.println("Error saving article to DB: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    setFeedback("Database error while saving.", Color.RED);
                    saveButton.setDisable(false); // Re-enable button on failure
                });
            }
        };
        new Thread(saveTask).start();
        // --- End background task ---
    }

    /**
     * Validate the form inputs.
     * @return true if form is valid
     */
    private boolean validateForm() {
        boolean isValid = true;
        clearError(titleError); // Clear previous errors
        clearError(contentError);

        // Validate title
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            titleError.setText("Title is required.");
            isValid = false;
        } else if (title.length() < 5) {
            titleError.setText("Title must be at least 5 characters.");
            isValid = false;
        }

        // Validate content
        String content = contentArea.getText().trim();
        if (content.isEmpty()) {
            contentError.setText("Content is required.");
            isValid = false;
        } else if (content.length() < 20) {
            contentError.setText("Content must be at least 20 characters.");
            isValid = false;
        }

        // Validate Image URL (basic format check - optional)
        String imageUrl = imageUrlField.getText().trim();
        if (!imageUrl.isEmpty() && !(imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            // You could add a specific error label for image URL if needed
            setFeedback("Image URL should start with http:// or https://.", Color.ORANGE);
            // Decide if this makes the form invalid or just a warning
            // isValid = false;
        }


        // Category selection is optional based on current DAO/DB setup (allows NULL)
        // If category becomes required, add validation:
        /*
        if (categoryComboBox.getValue() == null) {
            // Add a categoryError Label in FXML if needed
            setFeedback("Please select a category.", Color.RED);
            isValid = false;
        }
        */

        return isValid;
    }

    /**
     * Clear the form fields (not typically needed if navigating away on success/cancel).
     */
    private void clearForm() {
        titleField.clear();
        contentArea.clear();
        categoryComboBox.getSelectionModel().clearSelection(); // Clear selection
        publishCheckBox.setSelected(false);
        imageUrlField.clear(); // Clear image URL
        clearError(titleError);
        clearError(contentError);
        clearStatus();
    }

    /**
     * Sets the feedback message and color. Ensures UI update is on FX thread.
     */
    private void setFeedback(String message, Color color) {
        // Make sure UI updates are on the JavaFX Application Thread
        if (Platform.isFxApplicationThread()) {
            statusMessage.setText(message);
            statusMessage.setTextFill(color);
            // Add/remove CSS classes for more distinct styling (optional)
            statusMessage.getStyleClass().removeAll("success-message", "error-message", "info-message");
            if (color == Color.GREEN) statusMessage.getStyleClass().add("success-message");
            else if (color == Color.RED || color == Color.ORANGE) statusMessage.getStyleClass().add("error-message");
            else statusMessage.getStyleClass().add("info-message");
        } else {
            Platform.runLater(() -> setFeedback(message, color));
        }
    }

    /** Handle cancel button click */
    @FXML
    private void handleCancel(ActionEvent event) {
        System.out.println("Cancel button clicked");
        navigateToArticles(null); // Go back to articles list
    }

    // --- Navigation Methods (Keep as is) ---
    @FXML private void navigateToArticles(ActionEvent event) { WellTechApplication.loadFXML("articlesList"); }
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