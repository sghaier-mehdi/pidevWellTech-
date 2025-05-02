package com.welltech.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.welltech.dao.ProductDAO;
import com.welltech.dao.OrderDAO;
import com.welltech.model.Product;
import com.welltech.model.Order;
import com.welltech.model.OrderItem;
import com.welltech.model.User;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductListController {
    @FXML
    private TextField searchField;
    @FXML
    private FlowPane productFlowPane;
    
    // Add filter components
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private CheckBox showActiveOnly;
    @FXML
    private Slider priceRangeSlider;
    @FXML 
    private Label priceRangeLabel;

    private ProductDAO productDAO;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Product selectedProduct; // To track the selected product
    
    // For search debouncing
    private javafx.animation.PauseTransition searchDebouncer = new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
    private double maxPrice = 1000.0; // Default max price, will be updated when products load

    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51RGS2eIVlAFxxphB2AVnhV5xia7POVEqgmCk8J6UDUqZ9xdYCU3zMR5vCtcHTQu2HzBafMLmC0mt0j1PYaDENZ2g00POTN56ew";
    private static final String STRIPE_SECRET_KEY = "sk_test_51RGS2eIVlAFxxphB6rdKqy1KsraPWwsl4ApwIQwjY3pilDxIjVkQXFhB3nPoFb5P9vWCLt1RHNwqHLcufNOX4XK700BmH4iJTP";

    public void setProductDAO(ProductDAO productDAO) {
        this.productDAO = productDAO;
        loadProducts();
        
        // Check if we need to update the max price for the slider
        if (priceRangeSlider != null) {
            try {
                // Calculate max price from all products
                List<Product> allProducts = productDAO.findAll();
                if (allProducts.size() > 0) {
                    Optional<BigDecimal> highestPrice = allProducts.stream()
                        .map(Product::getPrice)
                        .max(BigDecimal::compareTo);
                    
                    if (highestPrice.isPresent()) {
                        maxPrice = highestPrice.get().doubleValue() * 1.2; // Add 20% buffer
                        priceRangeSlider.setMax(maxPrice);
                        priceRangeSlider.setValue(maxPrice);
                        if (priceRangeLabel != null) {
                            priceRangeLabel.setText(String.format("Max Price: $%.2f", maxPrice));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error calculating max price: " + e.getMessage());
            }
        }
    }

    @FXML
    public void initialize() {
        // Set the FlowPane width to fit 4 cards per row
        double cardWidth = 220; // Card width
        double hgap = 15; // Horizontal gap from FlowPane
        int cardsPerRow = 4;
        double totalWidth = (cardWidth * cardsPerRow) + (hgap * (cardsPerRow - 1));
        productFlowPane.setPrefWidth(totalWidth);
        
        // Initialize search debouncer
        searchDebouncer.setOnFinished(e -> performSearch());
        
        // Set up dynamic search as user types
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchDebouncer.stop();
            searchDebouncer.playFromStart();
        });
        
        // Initialize filters if they exist in the FXML
        initializeFilters();
    }
    
    private void initializeFilters() {
        try {
            // Initialize category filter if it exists
            if (categoryFilter != null) {
                categoryFilter.getItems().add("All Categories");
                categoryFilter.setValue("All Categories");
                categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> performSearch());
            }
            
            // Initialize active-only checkbox if it exists
            if (showActiveOnly != null) {
                showActiveOnly.setSelected(true);
                showActiveOnly.selectedProperty().addListener((obs, oldVal, newVal) -> performSearch());
            }
            
            // Initialize price range slider if it exists
            if (priceRangeSlider != null) {
                priceRangeSlider.setMin(0);
                priceRangeSlider.setMax(maxPrice);
                priceRangeSlider.setValue(maxPrice);
                
                // Update label as slider changes
                priceRangeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (priceRangeLabel != null) {
                        priceRangeLabel.setText(String.format("Max Price: $%.2f", newVal.doubleValue()));
                    }
                    // Don't search on every small change, only when user releases the slider
                });
                
                // Only perform search when user releases the slider
                priceRangeSlider.setOnMouseReleased(e -> performSearch());
            }
        } catch (Exception e) {
            System.err.println("Error initializing filters: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void performSearch() {
        try {
            String searchText = searchField.getText().trim().toLowerCase();
            boolean showOnlyActive = showActiveOnly != null && showActiveOnly.isSelected();
            String category = categoryFilter != null ? categoryFilter.getValue() : "All Categories";
            double maxPriceValue = priceRangeSlider != null ? priceRangeSlider.getValue() : maxPrice;
            
            // Get all products first
            List<Product> allProducts = productDAO.findAll();
            
            // Calculate actual max price for initialization if needed
            if (allProducts.size() > 0 && priceRangeSlider != null && priceRangeSlider.getValue() == maxPrice) {
                Optional<BigDecimal> highestPrice = allProducts.stream()
                    .map(Product::getPrice)
                    .max(BigDecimal::compareTo);
                
                if (highestPrice.isPresent()) {
                    double newMaxPrice = highestPrice.get().doubleValue() * 1.2; // Add 20% buffer
                    if (newMaxPrice > maxPrice) {
                        maxPrice = newMaxPrice;
                        priceRangeSlider.setMax(maxPrice);
                        priceRangeSlider.setValue(maxPrice);
                        if (priceRangeLabel != null) {
                            priceRangeLabel.setText(String.format("Max Price: $%.2f", maxPrice));
                        }
                    }
                }
            }
            
            // Filter products based on search and filters
            List<Product> filteredProducts = allProducts.stream()
                .filter(product -> 
                    // Text search
                    (searchText.isEmpty() || 
                     product.getName().toLowerCase().contains(searchText) ||
                     (product.getDescription() != null && 
                      product.getDescription().toLowerCase().contains(searchText)))
                    // Active filter
                    && (!showOnlyActive || product.isActive())
                    // Price filter
                    && (product.getPrice().doubleValue() <= maxPriceValue)
                    // Category filter (if implemented)
                    && (category.equals("All Categories") /* || add category check when implemented */)
                )
                .collect(Collectors.toList());
                
            // Update UI with filtered products
            productFlowPane.getChildren().clear();
            filteredProducts.forEach(this::addProductCard);
            
            // Display count of results
            System.out.println("Found " + filteredProducts.size() + " products matching criteria");
        } catch (Exception e) {
            System.err.println("Error performing search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        // This will be triggered by the search button
        performSearch();
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
                    System.out.println("Attempting to delete product: " + selectedProduct.getName());
                    productDAO.delete(selectedProduct);
                    System.out.println("Product deleted successfully: " + selectedProduct.getName());
                    loadProducts();
                } catch (IllegalStateException e) {
                    System.err.println("Product is in use: " + e.getMessage());
                    
                    // Show a dialog offering to deactivate the product instead
                    Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                    errorAlert.setTitle("Product In Use");
                    errorAlert.setHeaderText("Cannot Delete Product");
                    errorAlert.setContentText(e.getMessage() + "\n\nWould you like to deactivate this product instead?");
                    
                    ButtonType deactivateButton = new ButtonType("Deactivate");
                    ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    errorAlert.getButtonTypes().setAll(deactivateButton, cancelButton);
                    
                    if (errorAlert.showAndWait().get() == deactivateButton) {
                        // Deactivate the product
                        selectedProduct.setActive(false);
                        selectedProduct.setUpdatedAt(LocalDateTime.now());
                        productDAO.update(selectedProduct);
                        showSuccess("Product Deactivated", "The product has been deactivated successfully.");
                        loadProducts();
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Validation error while deleting product: " + e.getMessage());
                    showError("Validation Error", "The product cannot be deleted due to invalid data: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Unexpected error while deleting product: " + e.getMessage());
                    showError("Error", "Could not delete the product: " + e.getMessage());
                }
            }
        } else {
            System.out.println("No product selected for deletion.");
            showError("Selection Required", "Please select a product to delete.");
        }
    }

    private void loadProducts() {
        try {
            // Reset filters to default values
            if (searchField != null) {
                searchField.setText("");
            }
            
            if (categoryFilter != null) {
                categoryFilter.setValue("All Categories");
            }
            
            if (showActiveOnly != null) {
                showActiveOnly.setSelected(true);
            }
            
            if (priceRangeSlider != null) {
                priceRangeSlider.setValue(priceRangeSlider.getMax());
            }
            
            // Use the search method with empty criteria to load all products
            performSearch();
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
        card.setPrefHeight(350); // Increased height for image

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

        // Product image
        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        imageView.setFitWidth(190);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        
        // Use a better product placeholder image
        try {
            javafx.scene.image.Image placeholderImage = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/product-placeholder.png"),
                190, 120, true, true
            );
            imageView.setImage(placeholderImage);
        } catch (Exception e) {
            System.err.println("Could not load placeholder image: " + e.getMessage());
        }
        
        // Debug product image URL
        System.out.println("Product: " + product.getName());
        System.out.println("Image URL: " + (product.getImageUrl() != null ? product.getImageUrl() : "null"));
        
        // Check if product has an image URL
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                final String originalImageUrl = product.getImageUrl();
                System.out.println("Attempting to load image from URL: " + originalImageUrl);
                
                // Initialize with the original URL
                String imageUrl = originalImageUrl;
                
                // Check if the URL is a local file URL
                if (imageUrl.startsWith("file:")) {
                    System.out.println("Loading local file image");
                    
                    // Extract the file path and check if the file exists
                    try {
                        java.io.File imageFile = new java.io.File(new java.net.URI(imageUrl));
                        if (!imageFile.exists()) {
                            System.err.println("Local image file does not exist: " + imageFile.getAbsolutePath());
                            // Keep using the placeholder image
                            return;
                        }
                    } catch (Exception e) {
                        System.err.println("Error checking local file: " + e.getMessage());
                    }
                } 
                // Check if it's a Cloudinary URL
                else if (imageUrl.contains("cloudinary.com")) {
                    System.out.println("Detected Cloudinary URL");
                    
                    // Try to convert WEBP to JPG if needed
                    if (imageUrl.endsWith(".webp")) {
                        imageUrl = imageUrl.replace(".webp", ".jpg");
                        System.out.println("Converted to JPG URL: " + imageUrl);
                    }
                }
                
                // Load the image with the potentially modified URL
                final String finalImageUrl = imageUrl;
                javafx.scene.image.Image image = new javafx.scene.image.Image(
                    finalImageUrl, 
                    190, // requested width
                    120, // requested height
                    true, // preserve ratio
                    true, // smooth
                    true  // background loading
                );
                
                // Only set the image if it loads successfully
                image.errorProperty().addListener((obs, oldValue, newValue) -> {
                    if (newValue) {
                        System.err.println("Error loading image for product: " + product.getName());
                        
                        // Try to convert HTTPS to HTTP as a fallback
                        if (finalImageUrl.startsWith("https://")) {
                            try {
                                final String httpUrl = finalImageUrl.replace("https://", "http://");
                                System.out.println("Trying HTTP fallback: " + httpUrl);
                                
                                javafx.scene.image.Image fallbackImage = new javafx.scene.image.Image(
                                    httpUrl, 
                                    190, 120, true, true, true
                                );
                                
                                fallbackImage.errorProperty().addListener((obs2, oldVal2, newVal2) -> {
                                    if (!newVal2) {
                                        imageView.setImage(fallbackImage);
                                    }
                                });
                            } catch (Exception e) {
                                System.err.println("HTTP fallback also failed: " + e.getMessage());
                            }
                        }
                    } else {
                        // Image loaded successfully
                        imageView.setImage(image);
                    }
                });
                
                // If the image loads immediately without error, set it
                if (!image.isError() && image.getProgress() == 1.0) {
                    imageView.setImage(image);
                    System.out.println("Image loaded successfully");
                }
                
                // Center the image
                imageView.setStyle("-fx-alignment: center;");
            } catch (Exception e) {
                System.err.println("Failed to load image: " + e.getMessage());
                e.printStackTrace();
                // Keep the placeholder image
            }
        } else {
            System.out.println("No image URL provided for product: " + product.getName());
        }
        
        // Add a border to the image container
        VBox imageContainer = new VBox(imageView);
        imageContainer.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-padding: 5; -fx-alignment: center;");
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
        imageContainer.setMinHeight(130);
        
        // Product info
        Text name = new Text(product.getName());
        name.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-fill: #333333;");
        
        Text description = new Text(product.getDescription() != null ? product.getDescription() : "");
        description.setStyle("-fx-font-size: 12; -fx-fill: #666666;");
        description.setWrappingWidth(190);
        
        Text price = new Text("$" + product.getPrice());
        price.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-fill: #2e7d32;");
        
        Text stock = new Text("Stock: " + product.getStockQuantity());
        stock.setStyle("-fx-font-size: 12; -fx-fill: #666666;");
        
        Text status = new Text("Status: " + (product.isActive() ? "Active" : "Inactive"));
        status.setStyle("-fx-font-size: 12; -fx-fill: " + (product.isActive() ? "#2e7d32;" : "#d32f2f;"));
        
        // Display updated date only
        Text updatedAt = new Text("Updated: " + (product.getUpdatedAt() != null ? dateFormatter.format(product.getUpdatedAt()) : "N/A"));
        updatedAt.setStyle("-fx-font-size: 11; -fx-fill: #999999;");

        card.getChildren().addAll(imageContainer, name, description, price, stock, status, updatedAt);

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
            // Create or get the current user's order
            User currentUser = LoginController.getCurrentUser();
            if (currentUser == null) {
                showError("Authentication Required", "Please log in to make a purchase.");
                return;
            }
            
            // Create a new order
            Order order = new Order();
            order.setUser(currentUser);
            order.setStatus("PENDING");
            order.setPaymentStatus("PAID"); // Set payment status to PAID immediately
            order.setPaymentMethod("Stripe");
            order.setTotalAmount(product.getPrice());
            order.setUpdatedAt(LocalDateTime.now());
            
            // Create order item for this product
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(1);
            orderItem.setUnitPrice(product.getPrice());
            
            // Add item to order
            order.getOrderItems().add(orderItem);
            
            // Save order to database
            OrderDAO orderDAO = new OrderDAO();
            orderDAO.save(order);
            
            System.out.println("Order created with ID: " + order.getId() + " and status: " + order.getPaymentStatus());

            // Continue with Stripe session creation
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

            // Open the Stripe Checkout URL in the user's default browser
            java.awt.Desktop.getDesktop().browse(new java.net.URI(session.getUrl()));

            // Show a success message after creating the order
            showSuccess("Order Created", "Order has been created and marked as PAID. Redirecting to payment gateway.");

        } catch (StripeException e) {
            showError("Payment Error", "Failed to initiate payment: " + e.getMessage(), e);
        } catch (Exception e) {
            showError("Error", "Could not process order: " + e.getMessage(), e);
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

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}