package com.welltech.controller;

import com.welltech.dao.ProductDAO;
import com.welltech.model.Product;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProductListController {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> descriptionColumn;
    @FXML
    private TableColumn<Product, BigDecimal> priceColumn;
    @FXML
    private TableColumn<Product, Integer> stockColumn;
    @FXML
    private TableColumn<Product, Boolean> activeColumn;
    @FXML
    private TableColumn<Product, LocalDateTime> createdAtColumn;
    @FXML
    private TableColumn<Product, LocalDateTime> updatedAtColumn;

    private ProductDAO productDAO;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void setProductDAO(ProductDAO productDAO) {
        this.productDAO = productDAO;
        loadProducts();
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdAtColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(item));
                }
            }
        });
        
        updatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
        updatedAtColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(item));
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadProducts();
        } else {
            productTable.getItems().setAll(productDAO.findByName(searchText));
        }
    }

    @FXML
    private void handleAddNew() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product/ProductEdit.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Add New Product");
            stage.setScene(scene);
            
            ProductEditController controller = loader.getController();
            controller.setProductDAO(productDAO);
            controller.setProduct(null);
            
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            loadProducts();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not open the product edit window. Error: " + e.getMessage(), e);
        }
    }

    @FXML
    private void handleEdit() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product/ProductEdit.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = new Stage();
                stage.setTitle("Edit Product");
                stage.setScene(scene);
                
                ProductEditController controller = loader.getController();
                controller.setProductDAO(productDAO);
                controller.setProduct(selectedProduct);
                
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
                
                loadProducts();
            } catch (IOException e) {
                showError("Error", "Could not open the product edit window", e);
            }
        } else {
            showError("Selection Required", "Please select a product to edit.");
        }
    }

    @FXML
    private void handleDelete() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this product?");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    productDAO.delete(selectedProduct);
                    loadProducts();
                } catch (Exception e) {
                    showError("Error", "Could not delete the product", e);
                }
            }
        } else {
            showError("Selection Required", "Please select a product to delete.");
        }
    }

    private void loadProducts() {
        try {
            productTable.getItems().setAll(productDAO.findAll());
        } catch (Exception e) {
            showError("Error", "Could not load products", e);
        }
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