package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.ObjectiveDAO;
import com.welltech.model.Objective;
import com.welltech.model.User;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the objectives management screen
 */
public class ObjectivesController implements Initializable {
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private TableView<Objective> objectivesTable;
    
    @FXML
    private TableColumn<Objective, Integer> idColumn;
    
    @FXML
    private TableColumn<Objective, String> titleColumn;
    
    @FXML
    private TableColumn<Objective, Integer> pointsColumn;
    
    @FXML
    private TableColumn<Objective, String> dateCreatedColumn;
    
    @FXML
    private TableColumn<Objective, String> actionColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> pointsFilterComboBox;
    
    @FXML
    private Label formTitle;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private TextArea descriptionField;
    
    @FXML
    private TextField pointsField;
    
    @FXML
    private ComboBox<User> userComboBox;
    
    @FXML
    private DatePicker fromDatePicker;
    
    @FXML
    private DatePicker toDatePicker;
    
    private ObjectiveDAO objectiveDAO;
    private ObservableList<Objective> objectivesList = FXCollections.observableArrayList();
    private Objective currentObjective;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialize repository
            objectiveDAO = new ObjectiveDAO();
            
            // Get current user
            User currentUser = LoginController.getCurrentUser();
            if (currentUser != null) {
                userNameLabel.setText("Welcome, " + currentUser.getFullName());
            }
            
            // Check if there's a selected coupon from the CouponController
            Object selectedCouponObject = WellTechApplication.getTempData("selectedCoupon");
            if (selectedCouponObject instanceof com.welltech.model.Coupon) {
                com.welltech.model.Coupon selectedCoupon = (com.welltech.model.Coupon) selectedCouponObject;
                // We have a selected coupon, prepare the form for creating a new objective
                clearForm();
                // Optionally update the form title to indicate we're creating an objective with a coupon
                formTitle.setText("Create Objective with Coupon: " + selectedCoupon.getName());
                
                // Remove the temp data to avoid re-using it if user navigates back to this screen
                WellTechApplication.removeTempData("selectedCoupon");
            }
            
            // Setup points filter combo box
            pointsFilterComboBox.getItems().addAll(
                "10+ points",
                "25+ points",
                "50+ points",
                "75+ points"
            );
            
            // Initialize table columns
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            pointsColumn.setCellValueFactory(new PropertyValueFactory<>("pointsRequired"));
            
            // Format date created column
            dateCreatedColumn.setCellValueFactory(cellData -> {
                LocalDateTime dateTime = cellData.getValue().getCreatedAt();
                if (dateTime != null) {
                    return new SimpleStringProperty(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
                return new SimpleStringProperty("N/A");
            });
            
            // Add action buttons to the table
            actionColumn.setCellFactory(createActionCellFactory());
            
            // Load users for the user combo box
            loadUsers();
            
            // Load initial data
            loadObjectives();
            
            System.out.println("ObjectivesController initialized");
        } catch (Exception e) {
            System.err.println("Error initializing ObjectivesController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load all objectives from the repository
     */
    private void loadObjectives() {
        objectivesList.clear();
        objectivesList.addAll(objectiveDAO.getAllObjectives());
        objectivesTable.setItems(objectivesList);
    }
    
    /**
     * Create cell factory for action column
     */
    private Callback<TableColumn<Objective, String>, TableCell<Objective, String>> createActionCellFactory() {
        return column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonsBox = new HBox(5, editButton, deleteButton);
            
            {
                // Setup edit button
                editButton.setOnAction(event -> {
                    Objective objective = getTableView().getItems().get(getIndex());
                    populateFormForEdit(objective);
                });
                
                // Setup delete button
                deleteButton.setOnAction(event -> {
                    Objective objective = getTableView().getItems().get(getIndex());
                    handleDelete(objective);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        };
    }
    
    /**
     * Handle search button action
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            List<Objective> results = objectiveDAO.findByTitle(searchText);
            objectivesList.clear();
            objectivesList.addAll(results);
            objectivesTable.setItems(objectivesList);
        } else {
            loadObjectives();
        }
    }
    
    /**
     * Handle points filter action
     */
    @FXML
    private void handlePointsFilter(ActionEvent event) {
        String selected = pointsFilterComboBox.getValue();
        if (selected != null) {
            int points = 0;
            
            if (selected.equals("10+ points")) points = 10;
            else if (selected.equals("25+ points")) points = 25;
            else if (selected.equals("50+ points")) points = 50;
            else if (selected.equals("75+ points")) points = 75;
            
            List<Objective> results = objectiveDAO.findObjectivesWithMinimumPoints(points);
            objectivesList.clear();
            objectivesList.addAll(results);
            objectivesTable.setItems(objectivesList);
        }
    }
    
    /**
     * Handle date filter action
     */
    @FXML
    private void handleDateFilter(ActionEvent event) {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        
        if (fromDate != null && toDate != null) {
            LocalDateTime from = fromDate.atStartOfDay();
            LocalDateTime to = toDate.atTime(LocalTime.MAX);
            
            List<Objective> results = objectiveDAO.findByDateRange(from, to);
            objectivesList.clear();
            objectivesList.addAll(results);
            objectivesTable.setItems(objectivesList);
        } else {
            showAlert(Alert.AlertType.WARNING, "Date Selection Error", "Please select both from and to dates.");
        }
    }
    
    /**
     * Handle reset filters action
     */
    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        pointsFilterComboBox.getSelectionModel().clearSelection();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        loadObjectives();
    }
    
    /**
     * Handle show top objectives action
     */
    @FXML
    private void handleShowTopObjectives(ActionEvent event) {
        List<Objective> topObjectives = objectiveDAO.findTopObjectives(5); // Show top 5
        objectivesList.clear();
        objectivesList.addAll(topObjectives);
        objectivesTable.setItems(objectivesList);
    }
    
    /**
     * Handle save button action (create or update)
     */
    @FXML
    private void handleSave(ActionEvent event) {
        try {
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            String pointsText = pointsField.getText().trim();
            
            // Validate inputs
            if (title.isEmpty() || description.isEmpty() || pointsText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields.");
                return;
            }
            
            int points;
            try {
                points = Integer.parseInt(pointsText);
                if (points < 0 || points > 100) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Points must be between 0 and 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Points must be a number.");
                return;
            }
            
            User selectedUser = userComboBox.getValue();
            Integer userId = selectedUser != null ? selectedUser.getId() : null;
            
            if (selectedUser == null) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a user for this objective.");
                return;
            }
            
            if (currentObjective == null) {
                // Create new objective
                Objective newObjective = new Objective();
                newObjective.setTitle(title);
                newObjective.setDescription(description);
                newObjective.setPointsRequired(points);
                newObjective.setUserId(userId);
                
                int id = objectiveDAO.insertObjective(newObjective);
                if (id > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Objective created successfully!");
                    clearForm();
                    loadObjectives();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create objective.");
                }
            } else {
                // Update existing objective
                currentObjective.setTitle(title);
                currentObjective.setDescription(description);
                currentObjective.setPointsRequired(points);
                currentObjective.setUserId(userId);
                
                boolean updated = objectiveDAO.updateObjective(currentObjective);
                if (updated) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Objective updated successfully!");
                    clearForm();
                    loadObjectives();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update objective.");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle delete button action
     */
    private void handleDelete(Objective objective) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Objective");
        confirmDialog.setContentText("Are you sure you want to delete this objective: " + objective.getTitle() + "?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean deleted = objectiveDAO.deleteObjective(objective.getId());
                if (deleted) {
                    loadObjectives();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Objective deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete objective.");
                }
            }
        });
    }
    
    /**
     * Handle clear button action
     */
    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }
    
    /**
     * Populate form for editing an existing objective
     */
    private void populateFormForEdit(Objective objective) {
        currentObjective = objective;
        formTitle.setText("Edit Objective");
        titleField.setText(objective.getTitle());
        descriptionField.setText(objective.getDescription());
        pointsField.setText(String.valueOf(objective.getPointsRequired()));
        
        // Set selected user if available
        if (objective.getUserId() != null) {
            for (User user : userComboBox.getItems()) {
                if (user.getId() == objective.getUserId()) {
                    userComboBox.setValue(user);
                    break;
                }
            }
        } else {
            userComboBox.setValue(null);
        }
    }
    
    /**
     * Clear form and reset to "Add New Objective" mode
     */
    private void clearForm() {
        currentObjective = null;
        formTitle.setText("Add New Objective");
        titleField.clear();
        descriptionField.clear();
        pointsField.clear();
        userComboBox.setValue(null);
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Navigate to dashboard based on user role
     */
    @FXML
    private void navigateToDashboard(ActionEvent event) {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            switch (currentUser.getRole()) {
                case ADMIN:
                    WellTechApplication.loadFXML("adminDashboard");
                    break;
                case PSYCHIATRIST:
                    WellTechApplication.loadFXML("psychiatristDashboard");
                    break;
                case PATIENT:
                    WellTechApplication.loadFXML("patientDashboard");
                    break;
            }
        }
    }
    
    /**
     * Navigate to profile
     */
    @FXML
    private void navigateToProfile(ActionEvent event) {
        WellTechApplication.loadFXML("profile");
    }
    
    /**
     * Navigate to articles
     */
    @FXML
    private void navigateToArticles(ActionEvent event) {
        WellTechApplication.loadFXML("articlesList");
    }
    
    /**
     * Handle logout button click
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        LoginController.logout();
    }
    
    /**
     * Load all users from the database into the user combo box
     */
    private void loadUsers() {
        try {
            // Create UserDAO to access user data
            com.welltech.dao.UserDAO userDAO = new com.welltech.dao.UserDAO();
            
            // Get all users from the database
            List<User> users = userDAO.getAllUsers();
            
            // Clear existing items and add all users
            userComboBox.getItems().clear();
            userComboBox.getItems().addAll(users);
            
            // Setup the display string for each user in the combo box
            userComboBox.setCellFactory(param -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getFullName() + " (" + user.getUsername() + ")");
                    }
                }
            });
            
            // Also set how the selected item is displayed in the combo box when collapsed
            userComboBox.setButtonCell(new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getFullName() + " (" + user.getUsername() + ")");
                    }
                }
            });
            
            System.out.println("Loaded " + users.size() + " users into combo box");
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Method to set the selected user from another controller (e.g., CouponController)
     */
    public void selectUserForObjective(User user) {
        if (user != null) {
            userComboBox.setValue(user);
        }
    }
} 