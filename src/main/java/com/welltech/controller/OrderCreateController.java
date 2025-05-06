package com.welltech.controller;

import com.welltech.dao.OrderDAO;
import com.welltech.dao.ProductDAO;
import com.welltech.dao.UserDAO;
import com.welltech.model.Order;
import com.welltech.model.OrderItem;
import com.welltech.model.Product;
import com.welltech.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.Scene;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderCreateController {
    @FXML
    private ComboBox<User> userComboBox;
    @FXML
    private TextArea shippingAddressField;
    @FXML
    private ComboBox<String> paymentMethodComboBox;
    @FXML
    private ComboBox<Product> productComboBox;
    @FXML
    private TextField quantityField;
    @FXML
    private TableView<OrderItem> orderItemsTable;
    @FXML
    private TableColumn<OrderItem, String> productNameColumn;
    @FXML
    private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML
    private TableColumn<OrderItem, BigDecimal> unitPriceColumn;
    @FXML
    private TableColumn<OrderItem, BigDecimal> subtotalColumn;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private TextField phoneNumberField;

    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private UserDAO userDAO;
    private Order order;
    private ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize DAOs
        orderDAO = new OrderDAO();
        productDAO = new ProductDAO();
        userDAO = new UserDAO();

        // Initialize order
        order = new Order();
        order.setOrderItems(orderItems);

        // Initialize table columns
        productNameColumn.setCellValueFactory(cellData -> 
            cellData.getValue().getProduct().nameProperty());
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Set up table
        orderItemsTable.setItems(orderItems);

        // Load users
        List<User> users = userDAO.getAllUsers();
        userComboBox.setItems(FXCollections.observableArrayList(users));

        // Load products
        List<Product> products = productDAO.findAll();
        productComboBox.setItems(FXCollections.observableArrayList(products));

        // Set up payment methods
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(
            "Credit Card", "PayPal", "Bank Transfer", "Cash on Delivery"
        ));

        // Add listener to update total amount when items change
        orderItems.addListener((javafx.collections.ListChangeListener<OrderItem>) change -> {
            updateTotalAmount();
        });
    }

    @FXML
    private void handleAddItem() {
        Product selectedProduct = productComboBox.getValue();
        String quantityText = quantityField.getText().trim();

        if (selectedProduct == null) {
            showError("Error", "Please select a product");
            return;
        }

        if (quantityText.isEmpty()) {
            showError("Error", "Please enter a quantity");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showError("Error", "Quantity must be greater than zero");
                return;
            }

            // Check if product is already in the order
            for (OrderItem item : orderItems) {
                if (item.getProduct().getId() == selectedProduct.getId()) {
                    // Update quantity
                    item.setQuantity(item.getQuantity() + quantity);
                    orderItemsTable.refresh();
                    updateTotalAmount();
                    quantityField.clear();
                    return;
                }
            }

            // Add new item
            OrderItem item = new OrderItem(order, selectedProduct, quantity);
            orderItems.add(item);
            quantityField.clear();
            updateTotalAmount();
        } catch (NumberFormatException e) {
            showError("Error", "Please enter a valid number for quantity");
        }
    }

    @FXML
    private void handleSaveOrder() {
        if (validateInput()) {
            try {
                // Set order properties
                order.setUser(userComboBox.getValue());
                order.setShippingAddress(shippingAddressField.getText().trim());
                order.setPaymentMethod(paymentMethodComboBox.getValue());
                order.setStatus("PENDING");
                order.setPaymentStatus("UNPAID");
                order.setCreatedAt(LocalDateTime.now());
                order.setUpdatedAt(LocalDateTime.now());
                order.setPhoneNumber(phoneNumberField.getText().trim());
                
                order.calculateTotalAmount();

                // Save order
                orderDAO.save(order);

                // Send SMS notification
                sendOrderConfirmationSms(order.getPhoneNumber(), order);

                // Close window
                closeWindow();
            } catch (Exception e) {
                showError("Error", "Could not save the order", e);
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (userComboBox.getValue() == null) {
            errorMessage.append("Please select a user.\n");
        }
        
        if (shippingAddressField.getText().trim().isEmpty()) {
            errorMessage.append("Please enter a shipping address.\n");
        }
        
        if (paymentMethodComboBox.getValue() == null) {
            errorMessage.append("Please select a payment method.\n");
        }
        
        if (orderItems.isEmpty()) {
            errorMessage.append("Please add at least one item to the order.\n");
        }

        if (errorMessage.length() > 0) {
            showError("Validation Error", errorMessage.toString());
            return false;
        }
        
        return true;
    }

    private void updateTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            total = total.add(item.getSubtotal());
        }
        totalAmountLabel.setText(total.toString());
    }

    private void closeWindow() {
        Stage stage = (Stage) userComboBox.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content + "\n" + e.getMessage());
        alert.showAndWait();
    }

    // Twilio SMS sending method
    private static final String ACCOUNT_SID = "AC44cfddddb4a75b849c354653026b999d";
    private static final String AUTH_TOKEN = "c692e7c36cf0ad65d2831bc2f15aa656";
    private static final String FROM_PHONE = "+19086766358";

    private void sendOrderConfirmationSms(String toPhone, Order order) {
        if (toPhone == null || toPhone.isEmpty()) return;
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            String messageBody = "Thank you for placing your order!\n" +
                "Order ID: " + order.getId() + "\n" +
                "Total: $" + order.getTotalAmount() + "\n" +
                "Shipping Address: " + order.getShippingAddress();
            Message.creator(
                new com.twilio.type.PhoneNumber(toPhone),
                new com.twilio.type.PhoneNumber(FROM_PHONE),
                messageBody
            ).create();
        } catch (Exception e) {
            // Optionally log or show error, but don't block order creation
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }
} 