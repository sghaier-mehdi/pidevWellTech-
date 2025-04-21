package com.welltech.controller;

import com.welltech.dao.OrderDAO;
import com.welltech.model.Order;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

import java.time.LocalDateTime;

public class OrderStatusUpdateController {
    @FXML
    private Label orderIdLabel;
    @FXML
    private Label currentStatusLabel;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private ComboBox<String> paymentStatusComboBox;

    private Order order;
    private OrderDAO orderDAO;

    @FXML
    public void initialize() {
        // Initialize status options
        statusComboBox.setItems(FXCollections.observableArrayList(
            "PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"
        ));
        
        // Initialize payment status options
        paymentStatusComboBox.setItems(FXCollections.observableArrayList(
            "PENDING", "PAID", "FAILED", "REFUNDED"
        ));
    }

    public void setOrder(Order order) {
        this.order = order;
        displayOrderDetails();
    }

    public void setOrderDAO(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    private void displayOrderDetails() {
        orderIdLabel.setText("Order #" + order.getId());
        currentStatusLabel.setText(order.getStatus());
        statusComboBox.setValue(order.getStatus());
        paymentStatusComboBox.setValue(order.getPaymentStatus());
    }

    @FXML
    private void handleSave() {
        try {
            order.setStatus(statusComboBox.getValue());
            order.setPaymentStatus(paymentStatusComboBox.getValue());
            order.setUpdatedAt(LocalDateTime.now());
            
            orderDAO.update(order);
            closeWindow();
        } catch (Exception e) {
            showError("Error", "Could not update order status", e);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) orderIdLabel.getScene().getWindow();
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
} 