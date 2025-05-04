package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.CouponDAO;
import com.welltech.model.Coupon;
import com.welltech.model.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the coupon management screen
 */
public class CouponController implements Initializable {
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Label formTitle;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField discountField;
    
    @FXML
    private TextField codeField;
    
    @FXML
    private DatePicker expirationDatePicker;
    
    @FXML
    private CheckBox activeCheckbox;
    
    @FXML
    private Button assignCouponButton;
    
    @FXML
    private VBox couponsContainer;
    
    private CouponDAO couponDAO;
    private ObservableList<Coupon> couponsList = FXCollections.observableArrayList();
    private Coupon currentCoupon;
    private Coupon selectedCoupon;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialize DAO and service
            couponDAO = new CouponDAO();
            
            // Get current user
            User currentUser = LoginController.getCurrentUser();
            if (currentUser != null) {
                userNameLabel.setText("Welcome, " + currentUser.getFullName());
            }
            
            // Initially disable the assign button
            assignCouponButton.setDisable(true);
            
            // Load initial data
            loadCoupons();
            
            System.out.println("CouponController initialized");
        } catch (Exception e) {
            System.err.println("Error initializing CouponController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load all coupons from the database
     */
    private void loadCoupons() {
        couponsList.clear();
        couponsList.addAll(couponDAO.getAllCoupons());
        
        // Clear existing cards and create new ones
        updateCouponCards();
    }
    
    /**
     * Update the coupon cards in the container
     */
    private void updateCouponCards() {
        couponsContainer.getChildren().clear();
        
        for (Coupon coupon : couponsList) {
            couponsContainer.getChildren().add(createCouponCard(coupon));
        }
    }
    
    /**
     * Create a card-like view for a coupon
     */
    private VBox createCouponCard(Coupon coupon) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(Double.MAX_VALUE);
        card.setSpacing(8);
        
        // Coupon name (as title)
        Label nameLabel = new Label(coupon.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        
        // Coupon code
        HBox codeBox = new HBox();
        codeBox.setSpacing(5);
        Label codeTitle = new Label("Code:");
        codeTitle.setStyle("-fx-font-weight: bold;");
        Label codeValue = new Label(coupon.getCode());
        codeBox.getChildren().addAll(codeTitle, codeValue);
        
        // Discount
        HBox discountBox = new HBox();
        discountBox.setSpacing(5);
        Label discountTitle = new Label("Discount:");
        discountTitle.setStyle("-fx-font-weight: bold;");
        Label discountValue = new Label(coupon.getDiscountPercentage() + "%");
        discountBox.getChildren().addAll(discountTitle, discountValue);
        
        // Expiration date
        HBox expirationBox = new HBox();
        expirationBox.setSpacing(5);
        Label expirationTitle = new Label("Expires:");
        expirationTitle.setStyle("-fx-font-weight: bold;");
        String expirationText = coupon.getExpirationDate() != null ? 
                coupon.getExpirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "Never";
        Label expirationValue = new Label(expirationText);
        expirationBox.getChildren().addAll(expirationTitle, expirationValue);
        
        // Usage count
        HBox usageBox = new HBox();
        usageBox.setSpacing(5);
        Label usageTitle = new Label("Usage count:");
        usageTitle.setStyle("-fx-font-weight: bold;");
        Label usageValue = new Label(String.valueOf(coupon.getUsageCount()));
        usageBox.getChildren().addAll(usageTitle, usageValue);
        
        // Status (active/inactive)
        HBox statusBox = new HBox();
        statusBox.setSpacing(5);
        Label statusTitle = new Label("Status:");
        statusTitle.setStyle("-fx-font-weight: bold;");
        Label statusValue = new Label(coupon.isActive() ? "Active" : "Inactive");
        statusValue.setStyle(coupon.isActive() ? 
                "-fx-text-fill: #2ecc71; -fx-font-weight: bold;" : 
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        statusBox.getChildren().addAll(statusTitle, statusValue);
        
        // Buttons
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(10);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        editButton.setOnAction(event -> populateFormForEdit(coupon));
        
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteButton.setOnAction(event -> handleDelete(coupon));
        
        Button selectButton = new Button("Select");
        selectButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        selectButton.setOnAction(event -> {
            selectedCoupon = coupon;
            WellTechApplication.setTempData("selectedCoupon", coupon);
            assignCouponButton.setDisable(false);
        });
        
        buttonsBox.getChildren().addAll(editButton, deleteButton, selectButton);
        
        // Add all components to the card
        card.getChildren().addAll(
                nameLabel,
                codeBox,
                discountBox,
                expirationBox,
                usageBox,
                statusBox,
                buttonsBox
        );
        
        return card;
    }
    
    /**
     * Handle search button action
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim().toLowerCase();
        if (!searchText.isEmpty()) {
            List<Coupon> allCoupons = couponDAO.getAllCoupons();
            couponsList.clear();
            
            for (Coupon coupon : allCoupons) {
                if (coupon.getName().toLowerCase().contains(searchText) || 
                    coupon.getCode().toLowerCase().contains(searchText)) {
                    couponsList.add(coupon);
                }
            }
            
            updateCouponCards();
        } else {
            loadCoupons();
        }
    }
    
    /**
     * Handle show active only button action
     */
    @FXML
    private void handleShowActiveOnly(ActionEvent event) {
        couponsList.clear();
        couponsList.addAll(couponDAO.getActiveCoupons());
        updateCouponCards();
    }
    
    /**
     * Handle show all button action
     */
    @FXML
    private void handleShowAll(ActionEvent event) {
        loadCoupons();
    }
    
    /**
     * Handle save button action (create or update)
     */
    @FXML
    private void handleSave(ActionEvent event) {
        try {
            String name = nameField.getText().trim();
            String discountText = discountField.getText().trim();
            String code = codeField.getText().trim();
            LocalDate expirationDate = expirationDatePicker.getValue();
            boolean isActive = activeCheckbox.isSelected();
            
            // Validate inputs
            if (name.isEmpty() || discountText.isEmpty() || code.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields.");
                return;
            }
            
            // Parse discount percentage
            int discountPercentage;
            try {
                discountPercentage = Integer.parseInt(discountText);
                if (discountPercentage < 0 || discountPercentage > 100) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Discount must be between 0 and 100%.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Discount must be a number.");
                return;
            }
            
            // Convert expiration date to LocalDateTime (end of day)
            LocalDateTime expirationDateTime = null;
            if (expirationDate != null) {
                expirationDateTime = expirationDate.atTime(LocalTime.MAX);
            }
            
            if (currentCoupon == null) {
                // Create new coupon
                int couponId = createCoupon(name, discountPercentage, code, expirationDateTime);
                
                if (couponId > 0) {
                    // If successful, update the coupon's active status if needed
                    if (!isActive) {
                        Coupon newCoupon = couponDAO.getCouponById(couponId);
                        newCoupon.setActive(false);
                        couponDAO.updateCoupon(newCoupon);
                    }
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Coupon created successfully!");
                    clearForm();
                    loadCoupons();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create coupon. Code may be duplicate.");
                }
            } else {
                // Update existing coupon
                currentCoupon.setName(name);
                currentCoupon.setDiscountPercentage(discountPercentage);
                currentCoupon.setCode(code);
                currentCoupon.setExpirationDate(expirationDateTime);
                currentCoupon.setActive(isActive);
                
                boolean updated = couponDAO.updateCoupon(currentCoupon);
                if (updated) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Coupon updated successfully!");
                    clearForm();
                    loadCoupons();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update coupon.");
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
    private void handleDelete(Coupon coupon) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Coupon");
        confirmDialog.setContentText("Are you sure you want to delete this coupon: " + coupon.getName() + "?\n\n" +
                                    "This will also delete any objectives associated with this coupon.");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean deleted = couponDAO.deleteCoupon(coupon.getId());
                if (deleted) {
                    loadCoupons();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Coupon deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete coupon.");
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
     * Handle assign coupon button action
     */
    @FXML
    private void handleAssignCoupon(ActionEvent event) {
        if (selectedCoupon == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a coupon from the table first.");
            return;
        }
        
        // Store the selected coupon ID to be used by the objectives controller
        try {
            // Navigate to objectives page to create a new objective with this coupon
            WellTechApplication.setTempData("selectedCoupon", selectedCoupon);
            WellTechApplication.loadFXML("objectives");
            
            // When the objectives screen loads, we want to access the controller
            // to set the selected coupon
            // This can be achieved by adding a static method in WellTechApplication
            // to get the current controller after loading FXML
            
            showAlert(Alert.AlertType.INFORMATION, "Coupon Selected", 
                     "Coupon \"" + selectedCoupon.getName() + "\" has been selected. " +
                     "Now create a new objective and assign it to a user.");
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Populate form for editing an existing coupon
     */
    private void populateFormForEdit(Coupon coupon) {
        currentCoupon = coupon;
        formTitle.setText("Edit Coupon");
        nameField.setText(coupon.getName());
        discountField.setText(String.valueOf(coupon.getDiscountPercentage()));
        codeField.setText(coupon.getCode());
        
        // Set expiration date if available
        if (coupon.getExpirationDate() != null) {
            expirationDatePicker.setValue(coupon.getExpirationDate().toLocalDate());
        } else {
            expirationDatePicker.setValue(null);
        }
        
        activeCheckbox.setSelected(coupon.isActive());
    }
    
    /**
     * Clear form and reset to "Add New Coupon" mode
     */
    private void clearForm() {
        currentCoupon = null;
        formTitle.setText("Add New Coupon");
        nameField.clear();
        discountField.clear();
        codeField.clear();
        expirationDatePicker.setValue(null);
        activeCheckbox.setSelected(true);
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
     * Navigate to objectives page
     */
    @FXML
    private void navigateToObjectives(ActionEvent event) {
        WellTechApplication.loadFXML("objectives");
    }
    
    /**
     * Navigate to profile page
     */
    @FXML
    private void navigateToProfile(ActionEvent event) {
        WellTechApplication.loadFXML("profile");
    }
    
    /**
     * Navigate to articles page
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
     * Create a new coupon in the database
     * @param name Coupon name
     * @param discountPercentage Discount percentage
     * @param code Coupon code
     * @param expirationDate Expiration date (optional)
     * @return Generated coupon ID if successful, -1 otherwise
     */
    private int createCoupon(String name, int discountPercentage, String code, LocalDateTime expirationDateTime) {
        Coupon newCoupon = new Coupon();
        newCoupon.setName(name);
        newCoupon.setDiscountPercentage(discountPercentage);
        newCoupon.setCode(code);
        newCoupon.setExpirationDate(expirationDateTime);
        newCoupon.setActive(true);
        newCoupon.setUsageCount(0);

        System.out.println("Coupon created: " + newCoupon.getName());
        
        return couponDAO.insertCoupon(newCoupon);
    }
} 