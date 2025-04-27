package com.welltech.controller;

import com.welltech.dao.OrderDAO;
import com.welltech.model.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class OrderListController {
    @FXML
    private FlowPane orderFlowPane;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private Button createOrderButton;

    private OrderDAO orderDAO;
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Order selectedOrder; // To track the selected order

    @FXML
    public void initialize() {
        // Set the FlowPane width to fit 4 cards per row
        double cardWidth = 220; // Card width
        double hgap = 15; // Horizontal gap from FlowPane
        int cardsPerRow = 4;
        double totalWidth = (cardWidth * cardsPerRow) + (hgap * (cardsPerRow - 1));
        orderFlowPane.setPrefWidth(totalWidth);

        // Initialize status filter
        statusFilter.setItems(FXCollections.observableArrayList(
            "All", "PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"
        ));
        statusFilter.setValue("All");

        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });

        // Set up status filter listener
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });

        // Set up date filter listeners
        startDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });
        endDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });
    }

    public void setOrderDAO(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
        loadOrders();
    }

    private void loadOrders() {
        orders.clear();
        orders.addAll(orderDAO.findAll());
        orderFlowPane.getChildren().clear();
        orders.forEach(this::addOrderCard);
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedStatus = statusFilter.getValue();
        LocalDateTime startDateTime = startDate.getValue() != null ? 
            startDate.getValue().atStartOfDay() : null;
        LocalDateTime endDateTime = endDate.getValue() != null ? 
            endDate.getValue().atTime(23, 59, 59) : null;

        orders.clear();
        orders.addAll(orderDAO.findAll().stream()
            .filter(order -> {
                boolean matchesSearch = searchText.isEmpty() ||
                    order.getUser().getUsername().toLowerCase().contains(searchText) ||
                    String.valueOf(order.getId()).contains(searchText);
                
                boolean matchesStatus = selectedStatus.equals("All") ||
                    order.getStatus().equals(selectedStatus);
                
                boolean matchesDateRange = (startDateTime == null || 
                    !order.getCreatedAt().isBefore(startDateTime)) &&
                    (endDateTime == null || !order.getCreatedAt().isAfter(endDateTime));
                
                return matchesSearch && matchesStatus && matchesDateRange;
            })
            .collect(Collectors.toList()));

        orderFlowPane.getChildren().clear();
        orders.forEach(this::addOrderCard);
    }

    private void addOrderCard(Order order) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #ffffff; " +
                      "-fx-border-color: #e0e0e0; " +
                      "-fx-border-radius: 10; " +
                      "-fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2); " +
                      "-fx-padding: 15;");
        card.setPrefWidth(220);
        card.setPrefHeight(250); // Fixed height for uniformity

        // Hover effect
        card.setOnMouseEntered(event -> card.setStyle("-fx-background-color: #f5f5f5; " +
                                                     "-fx-border-color: #e0e0e0; " +
                                                     "-fx-border-radius: 10; " +
                                                     "-fx-background-radius: 10; " +
                                                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 3); " +
                                                     "-fx-padding: 15;"));
        card.setOnMouseExited(event -> {
            if (order != selectedOrder) {
                card.setStyle("-fx-background-color: #ffffff; " +
                              "-fx-border-color: #e0e0e0; " +
                              "-fx-border-radius: 10; " +
                              "-fx-background-radius: 10; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2); " +
                              "-fx-padding: 15;");
            }
        });

        
        Text user = new Text("User: " + order.getUser().getUsername());
        user.setStyle("-fx-font-size: 12; -fx-fill: #666666;");
        
        Text totalAmount = new Text("Total: $" + order.getTotalAmount());
        totalAmount.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-fill: #2e7d32;");
        
        Text status = new Text("Status: " + order.getStatus());
        status.setStyle("-fx-font-size: 12; -fx-fill: #0288d1;");
        
        Text paymentStatus = new Text("Payment: " + order.getPaymentStatus());
        paymentStatus.setStyle("-fx-font-size: 12; -fx-fill: " + (order.getPaymentStatus().equals("PAID") ? "#2e7d32;" : "#d32f2f;"));
        
        Text createdAt = new Text("Created: " + (order.getCreatedAt() != null ? dateFormatter.format(order.getCreatedAt()) : "N/A"));
        createdAt.setStyle("-fx-font-size: 11; -fx-fill: #999999;");

        Button payButton = new Button(order.getPaymentStatus().equals("PAID") ? "Paid" : "Pay Now");
        payButton.setDisable(order.getPaymentStatus().equals("PAID"));
        payButton.setStyle("-fx-background-color: " + (order.getPaymentStatus().equals("PAID") ? "#cccccc;" : "#1976d2;") +
                           "-fx-text-fill: white; " +
                           "-fx-font-weight: bold; " +
                           "-fx-background-radius: 5;");
        payButton.setOnAction(event -> handlePay(order));
        if (!order.getPaymentStatus().equals("PAID")) {
            payButton.setOnMouseEntered(event -> payButton.setStyle("-fx-background-color: #1565c0; " +
                                                                    "-fx-text-fill: white; " +
                                                                    "-fx-font-weight: bold; " +
                                                                    "-fx-background-radius: 5;"));
            payButton.setOnMouseExited(event -> payButton.setStyle("-fx-background-color: #1976d2; " +
                                                                   "-fx-text-fill: white; " +
                                                                   "-fx-font-weight: bold; " +
                                                                   "-fx-background-radius: 5;"));
        }

        card.getChildren().addAll(  user, totalAmount, status, paymentStatus, createdAt, payButton);

        // Highlight selected card
        card.setOnMouseClicked(event -> {
            selectedOrder = order;
            orderFlowPane.getChildren().forEach(node -> node.setStyle("-fx-background-color: #ffffff; " +
                                                                      "-fx-border-color: #e0e0e0; " +
                                                                      "-fx-border-radius: 10; " +
                                                                      "-fx-background-radius: 10; " +
                                                                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2); " +
                                                                      "-fx-padding: 15;"));
            card.setStyle("-fx-background-color: #e3f2fd; " +
                          "-fx-border-color: #1976d2; " +
                          "-fx-border-radius: 10; " +
                          "-fx-background-radius: 10; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 3); " +
                          "-fx-padding: 15;");
        });

        orderFlowPane.getChildren().add(card);
    }

    private void handlePay(Order order) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment");
        alert.setHeaderText(null);
        alert.setContentText("Proceeding to payment for Order ID: " + order.getId() + "\nTotal: $" + order.getTotalAmount());
        alert.showAndWait();
        // Simulate payment success
        order.setPaymentStatus("PAID");
        orderDAO.update(order);
        loadOrders();
        // Add actual payment logic here (e.g., integrate with a payment API)
    }

    @FXML
    private void handleViewDetails() {
        if (selectedOrder != null) {
            try {
                URL location = getClass().getResource("/fxml/order/OrderDetails.fxml");
                if (location == null) {
                    throw new IOException("Could not find OrderDetails.fxml");
                }
                FXMLLoader loader = new FXMLLoader(location);
                Parent root = loader.load();
                OrderDetailsController controller = loader.getController();
                controller.setOrder(selectedOrder);
                
                Stage stage = new Stage();
                stage.setTitle("Order Details");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
                
                loadOrders();
            } catch (IOException e) {
                showError("Error", "Could not open order details", e);
                e.printStackTrace();
            }
        } else {
            showError("Error", "Please select an order to view details");
        }
    }

    @FXML
    private void handleUpdateStatus() {
        if (selectedOrder != null) {
            try {
                URL location = getClass().getResource("/fxml/order/OrderStatusUpdate.fxml");
                if (location == null) {
                    throw new IOException("Could not find OrderStatusUpdate.fxml");
                }
                FXMLLoader loader = new FXMLLoader(location);
                Parent root = loader.load();
                OrderStatusUpdateController controller = loader.getController();
                controller.setOrder(selectedOrder);
                controller.setOrderDAO(orderDAO);
                
                Stage stage = new Stage();
                stage.setTitle("Update Order Status");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
                
                loadOrders();
            } catch (IOException e) {
                showError("Error", "Could not open status update dialog", e);
                e.printStackTrace();
            }
        } else {
            showError("Error", "Please select an order to update status");
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedOrder != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this order?");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    orderDAO.delete(selectedOrder);
                    loadOrders();
                } catch (Exception e) {
                    showError("Error", "Could not delete the order", e);
                }
            }
        } else {
            showError("Selection Required", "Please select an order to delete.");
        }
    }

    @FXML
    private void handleCreateOrder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order/OrderCreate.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Create New Order");
            stage.setScene(new Scene(root));
            stage.show();
            
            stage.setOnCloseRequest(event -> loadOrders());
        } catch (IOException e) {
            showError("Error", "Could not open the create order window", e);
        }
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
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