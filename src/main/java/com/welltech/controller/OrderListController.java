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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class OrderListController {
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Long> idColumn;
    @FXML
    private TableColumn<Order, String> userColumn;
    @FXML
    private TableColumn<Order, BigDecimal> totalAmountColumn;
    @FXML
    private TableColumn<Order, String> statusColumn;
    @FXML
    private TableColumn<Order, String> paymentStatusColumn;
    @FXML
    private TableColumn<Order, LocalDateTime> createdAtColumn;
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

    @FXML
    public void initialize() {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUser().getUsername()));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdAtColumn.setCellFactory(column -> new TableCell<Order, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });

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

        orderTable.setItems(orders);
    }

    public void setOrderDAO(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
        loadOrders();
    }

    private void loadOrders() {
        orders.clear();
        orders.addAll(orderDAO.findAll());
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
    }

    @FXML
    private void handleViewDetails() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order/OrderDetails.fxml"));
                Parent root = loader.load();
                OrderDetailsController controller = loader.getController();
                controller.setOrder(selectedOrder);
                
                Stage stage = new Stage();
                stage.setTitle("Order Details");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
                
                // Refresh the order list after viewing details
                loadOrders();
            } catch (IOException e) {
                showError("Error", "Could not open order details", e);
            }
        } else {
            showError("Error", "Please select an order to view details");
        }
    }

    @FXML
    private void handleUpdateStatus() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order/OrderStatusUpdate.fxml"));
                Parent root = loader.load();
                OrderStatusUpdateController controller = loader.getController();
                controller.setOrder(selectedOrder);
                controller.setOrderDAO(orderDAO);
                
                Stage stage = new Stage();
                stage.setTitle("Update Order Status");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
                
                // Refresh the order list after status update
                loadOrders();
            } catch (IOException e) {
                showError("Error", "Could not open status update dialog", e);
            }
        } else {
            showError("Error", "Please select an order to update status");
        }
    }

    @FXML
    private void handleDelete() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
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
            
            // Refresh the order list when the create window is closed
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