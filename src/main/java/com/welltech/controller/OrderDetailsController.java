package com.welltech.controller;

import com.welltech.model.Order;
import com.welltech.model.OrderItem;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class OrderDetailsController {
    @FXML
    private Label orderIdLabel;
    @FXML
    private Label userLabel;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label paymentStatusLabel;
    @FXML
    private Label shippingAddressLabel;
    @FXML
    private Label paymentMethodLabel;
    @FXML
    private Label createdAtLabel;
    @FXML
    private Label updatedAtLabel;
    @FXML
    private TableView<OrderItem> itemsTable;
    @FXML
    private TableColumn<OrderItem, String> productColumn;
    @FXML
    private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML
    private TableColumn<OrderItem, BigDecimal> unitPriceColumn;
    @FXML
    private TableColumn<OrderItem, BigDecimal> subtotalColumn;

    private Order order;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        // Initialize table columns
        productColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
    }

    public void setOrder(Order order) {
        this.order = order;
        displayOrderDetails();
    }

    private void displayOrderDetails() {
        orderIdLabel.setText("Order #" + order.getId());
        userLabel.setText(order.getUser().getUsername());
        totalAmountLabel.setText("$" + order.getTotalAmount().toString());
        statusLabel.setText(order.getStatus());
        paymentStatusLabel.setText(order.getPaymentStatus());
        shippingAddressLabel.setText(order.getShippingAddress());
        paymentMethodLabel.setText(order.getPaymentMethod());
        createdAtLabel.setText(order.getCreatedAt().format(dateFormatter));
        updatedAtLabel.setText(order.getUpdatedAt().format(dateFormatter));

        // Display order items
        itemsTable.setItems(FXCollections.observableArrayList(order.getOrderItems()));
    }

    @FXML
    private void handleClose() {
        // Get the stage and close it
        javafx.stage.Stage stage = (javafx.stage.Stage) orderIdLabel.getScene().getWindow();
        stage.close();
    }
} 