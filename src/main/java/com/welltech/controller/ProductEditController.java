package com.welltech.controller;

import com.welltech.dao.ProductDAO;
import com.welltech.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductEditController {
    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField imageUrlField;
    @FXML
    private CheckBox activeCheckBox;

    private ProductDAO productDAO;
    private Product product;

    public void setProductDAO(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            nameField.setText(product.getName());
            descriptionField.setText(product.getDescription());
            priceField.setText(product.getPrice().toString());
            stockField.setText(String.valueOf(product.getStockQuantity()));
            imageUrlField.setText(product.getImageUrl());
            activeCheckBox.setSelected(product.isActive());
        }
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            try {
                if (product == null) {
                    product = new Product();
                    product.setCreatedAt(LocalDateTime.now());
                }
                
                product.setName(nameField.getText().trim());
                product.setDescription(descriptionField.getText().trim());
                product.setPrice(new BigDecimal(priceField.getText().trim()));
                product.setStockQuantity(Integer.parseInt(stockField.getText().trim()));
                product.setImageUrl(imageUrlField.getText().trim());
                product.setActive(activeCheckBox.isSelected());
                product.setUpdatedAt(LocalDateTime.now());

                if (product.getId() == 0L) {
                    productDAO.save(product);
                } else {
                    productDAO.update(product);
                }

                closeWindow();
            } catch (NumberFormatException e) {
                showError("Invalid Input", "Please enter valid numbers for price and stock quantity.");
            } catch (Exception e) {
                showError("Error", "Could not save the product", e);
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (nameField.getText().trim().isEmpty()) {
            errorMessage.append("Name is required.\n");
        }
        
        if (priceField.getText().trim().isEmpty()) {
            errorMessage.append("Price is required.\n");
        } else {
            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("Price must be greater than zero.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Price must be a valid number.\n");
            }
        }
        
        if (stockField.getText().trim().isEmpty()) {
            errorMessage.append("Stock quantity is required.\n");
        } else {
            try {
                int stock = Integer.parseInt(stockField.getText().trim());
                if (stock < 0) {
                    errorMessage.append("Stock quantity cannot be negative.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Stock quantity must be a valid number.\n");
            }
        }

        if (errorMessage.length() > 0) {
            showError("Validation Error", errorMessage.toString());
            return false;
        }
        
        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
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