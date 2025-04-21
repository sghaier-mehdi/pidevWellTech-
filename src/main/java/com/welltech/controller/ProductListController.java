package com.welltech.controller;

import com.welltech.dao.ProductDAO;
import com.welltech.model.Product;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
    private FlowPane productFlowPane;

    private ProductDAO productDAO;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Product selectedProduct; // To track the selected product

    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51RGS2eIVlAFxxphB2AVnhV5xia7POVEqgmCk8J6UDUqZ9xdYCU3zMR5vCtcHTQu2HzBafMLmC0mt0j1PYaDENZ2g00POTN56ew";
    private static final String STRIPE_SECRET_KEY = "sk_test_51RGS2eIVlAFxxphB6rdKqy1KsraPWwsl4ApwIQwjY3pilDxIjVkQXFhB3nPoFb5P9vWCLt1RHNwqHLcufNOX4XK700BmH4iJTP";

    public void setProductDAO(ProductDAO productDAO) {
        this.productDAO = productDAO;
        loadProducts();
    }

    @FXML
    public void initialize() {
        // Set the FlowPane width to fit 4 cards per row
        double cardWidth = 220; // Card width
        double hgap = 15; // Horizontal gap from FlowPane
        int cardsPerRow = 4;
        double totalWidth = (cardWidth * cardsPerRow) + (hgap * (cardsPerRow - 1));
        productFlowPane.setPrefWidth(totalWidth);
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadProducts();
        } else {
            productFlowPane.getChildren().clear();
            productDAO.findByName(searchText).forEach(this::addProductCard);
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
            productFlowPane.getChildren().clear();
            productDAO.findAll().forEach(this::addProductCard);
        } catch (Exception e) {
            showError("Error", "Could not load products", e);
        }
    }

    private void addProductCard(Product product) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #ffffff; " +
                      "-fx-border-color: #e0e0e0; " +
                      "-fx-border-radius: 10; " +
                      "-fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2); " +
                      "-fx-padding: 15;");
        card.setPrefWidth(220);
        card.setPrefHeight(300); // Fixed height for uniformity

        // Hover effect
        card.setOnMouseEntered(event -> card.setStyle("-fx-background-color: #f5f5f5; " +
                                                     "-fx-border-color: #e0e0e0; " +
                                                     "-fx-border-radius: 10; " +
                                                     "-fx-background-radius: 10; " +
                                                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 3); " +
                                                     "-fx-padding: 15;"));
        card.setOnMouseExited(event -> {
            if (product != selectedProduct) {
                card.setStyle("-fx-background-color: #ffffff; " +
                              "-fx-border-color: #e0e0e0; " +
                              "-fx-border-radius: 10; " +
                              "-fx-background-radius: 10; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2); " +
                              "-fx-padding: 15;");
            }
        });

        Text name = new Text("Name: " + product.getName());
        name.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-fill: #333333;");
        
        Text description = new Text("Description: " + product.getDescription());
        description.setStyle("-fx-font-size: 12; -fx-fill: #666666;");
        description.setWrappingWidth(190);
        
        Text price = new Text("Price: $" + product.getPrice());
        price.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-fill: #2e7d32;");
        
        Text stock = new Text("Stock: " + product.getStockQuantity());
        stock.setStyle("-fx-font-size: 12; -fx-fill: #666666;");
        
        Text status = new Text("Status: " + (product.isActive() ? "Active" : "Inactive"));
        status.setStyle("-fx-font-size: 12; -fx-fill: " + (product.isActive() ? "#2e7d32;" : "#d32f2f;"));
        
        Text createdAt = new Text("Created: " + (product.getCreatedAt() != null ? dateFormatter.format(product.getCreatedAt()) : "N/A"));
        createdAt.setStyle("-fx-font-size: 11; -fx-fill: #999999;");
        
        Text updatedAt = new Text("Updated: " + (product.getUpdatedAt() != null ? dateFormatter.format(product.getUpdatedAt()) : "N/A"));
        updatedAt.setStyle("-fx-font-size: 11; -fx-fill: #999999;");

        Button payNowButton = new Button("Pay Now");
        payNowButton.setStyle("-fx-background-color: #1976d2; " +
                              "-fx-text-fill: white; " +
                              "-fx-font-weight: bold; " +
                              "-fx-background-radius: 5;");
        payNowButton.setOnAction(event -> handlePayNow(product));
        payNowButton.setOnMouseEntered(event -> payNowButton.setStyle("-fx-background-color: #1565c0; " +
                                                                      "-fx-text-fill: white; " +
                                                                      "-fx-font-weight: bold; " +
                                                                      "-fx-background-radius: 5;"));
        payNowButton.setOnMouseExited(event -> payNowButton.setStyle("-fx-background-color: #1976d2; " +
                                                                     "-fx-text-fill: white; " +
                                                                     "-fx-font-weight: bold; " +
                                                                     "-fx-background-radius: 5;"));

        card.getChildren().addAll(name, description, price, stock, status, createdAt, updatedAt, payNowButton);

        // Highlight selected card
        card.setOnMouseClicked(event -> {
            selectedProduct = product;
            productFlowPane.getChildren().forEach(node -> node.setStyle("-fx-background-color: #ffffff; " +
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

        productFlowPane.getChildren().add(card);
    }

    private void handlePayNow(Product product) {
        Stripe.apiKey = STRIPE_SECRET_KEY;

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/success") // Replace with your success URL
                .setCancelUrl("http://localhost:8080/cancel")   // Replace with your cancel URL
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(product.getPrice().multiply(new BigDecimal(100)).longValue()) // Convert to cents
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(product.getName())
                                        .setDescription(product.getDescription())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();

            Session session = Session.create(params);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Stripe Payment");
            alert.setHeaderText("Redirecting to Stripe Checkout");
            alert.setContentText("Please complete the payment in the browser.");
            alert.showAndWait();

            // Open the Stripe Checkout URL in the default browser
            javafx.application.HostServices hostServices = new javafx.application.Application() {
                @Override
                public void start(Stage primaryStage) {
                    // No implementation needed
                }
            }.getHostServices();
            hostServices.showDocument(session.getUrl());
        } catch (StripeException e) {
            showError("Payment Error", "Failed to initiate payment: " + e.getMessage(), e);
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