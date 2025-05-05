package com.welltech.controller;

import com.welltech.dao.ProductDAO;
import com.welltech.model.Product;
import com.welltech.util.CloudinaryUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
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
    @FXML
    private Button uploadImageButton;
    @FXML
    private ImageView imagePreview;

    private ProductDAO productDAO;
    private Product product;

    @FXML
    public void initialize() {
        // Initialize the image preview when the imageUrlField changes
        imageUrlField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateImagePreview(newValue);
        });
    }

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
            
            // Initialize image preview with the product's image URL
            updateImagePreview(product.getImageUrl());
        }
    }

    private void updateImagePreview(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            // Clear the image view if URL is empty
            imagePreview.setImage(null);
            
            // Load default placeholder
            try {
                Image placeholderImage = new Image(
                    getClass().getResourceAsStream("/images/product-placeholder.png"),
                    200, 150, true, true
                );
                imagePreview.setImage(placeholderImage);
            } catch (Exception e) {
                System.err.println("Could not load default placeholder: " + e.getMessage());
            }
            return;
        }
        
        // Debug info
        System.out.println("=== IMAGE PREVIEW DEBUG ===");
        System.out.println("Attempting to load image from URL: " + imageUrl);
        
        try {
            // Always start with a placeholder while we load the actual image
            try {
                Image placeholderImage = new Image(
                    getClass().getResourceAsStream("/images/product-placeholder.png"),
                    200, 150, true, true
                );
                imagePreview.setImage(placeholderImage);
            } catch (Exception e) {
                System.err.println("Could not load placeholder: " + e.getMessage());
            }
            
            // Modify the URL if needed
            String processedUrl = imageUrl;
            
            // For Cloudinary URLs, adjust format if needed
            if (processedUrl.contains("cloudinary.com")) {
                System.out.println("Detected Cloudinary URL");
                
                // Convert WEBP to JPG for better compatibility
                if (processedUrl.endsWith(".webp")) {
                    processedUrl = processedUrl.replace(".webp", ".jpg");
                    System.out.println("Converted WEBP to JPG: " + processedUrl);
                }
                
                // Add format transformation if not already present
                if (!processedUrl.contains("/f_")) {
                    // Find the upload part of the URL
                    int uploadIndex = processedUrl.indexOf("/upload/");
                    if (uploadIndex > 0) {
                        String prefix = processedUrl.substring(0, uploadIndex + 8); // after "/upload/"
                        String suffix = processedUrl.substring(uploadIndex + 8);
                        
                        // Insert format transformation
                        processedUrl = prefix + "f_jpg,q_auto/" + suffix;
                        System.out.println("Added format transformation: " + processedUrl);
                    }
                }
            }
            
            // Attempt to load the image with background loading and proper error handling
            Image image = new Image(processedUrl, 200, 150, true, true, true);
            
            // Show a loading indicator while the image loads
            javafx.scene.control.ProgressIndicator progressIndicator = new javafx.scene.control.ProgressIndicator();
            progressIndicator.setMaxSize(50, 50);
            
            // Create container to hold both the image view and progress indicator
            javafx.scene.layout.StackPane container = new javafx.scene.layout.StackPane();
            
            // Get the parent container of the image preview
            javafx.scene.Parent parent = imagePreview.getParent();
            int imageIndex = -1;
            
            if (parent instanceof javafx.scene.layout.Pane) {
                javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) parent;
                imageIndex = pane.getChildren().indexOf(imagePreview);
                
                if (imageIndex >= 0) {
                    // Remove image view from its current parent
                    pane.getChildren().remove(imageIndex);
                    
                    // Add image view and progress indicator to container
                    container.getChildren().addAll(imagePreview, progressIndicator);
                    
                    // Add container to parent at the same position
                    pane.getChildren().add(imageIndex, container);
                }
            }
            
            // Track loading progress
            final int finalImageIndex = imageIndex;
            
            // Listen for progress updates
            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                double progress = newVal.doubleValue();
                progressIndicator.setProgress(progress);
                
                // When loading completes (progress = 1.0)
                if (progress >= 1.0) {
                    progressIndicator.setVisible(false);
                }
            });
            
            // Listen for error state changes
            final String originalUrl = imageUrl;
            final String modifiedUrl = processedUrl;
            
            image.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) { // Error occurred
                    System.err.println("Error loading image from URL: " + modifiedUrl);
                    progressIndicator.setVisible(false);
                    
                    // Try fallback strategies
                    tryFallbackImage(originalUrl, modifiedUrl, parent, container, finalImageIndex);
                } else { // Image loaded successfully
                    System.out.println("Image loaded successfully");
                    imagePreview.setImage(image);
                }
            });
            
            // If the image loads immediately without going through the background thread
            if (!image.isError() && image.getProgress() == 1.0) {
                imagePreview.setImage(image);
                progressIndicator.setVisible(false);
                System.out.println("Image loaded immediately");
            }
            
        } catch (Exception e) {
            System.err.println("Exception while setting up image preview: " + e.getMessage());
            e.printStackTrace();
            
            // Load placeholder on exception
            try {
                Image placeholderImage = new Image(
                    getClass().getResourceAsStream("/images/product-placeholder.png"),
                    200, 150, true, true
                );
                imagePreview.setImage(placeholderImage);
            } catch (Exception ex) {
                System.err.println("Could not load placeholder after error: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Try various fallback strategies when the primary image loading fails
     */
    private void tryFallbackImage(String originalUrl, String failedUrl, javafx.scene.Parent parent, 
                                 javafx.scene.layout.StackPane container, int imageIndex) {
        // Strategy 1: Try HTTP instead of HTTPS if applicable
        if (failedUrl.startsWith("https://")) {
            String httpUrl = failedUrl.replace("https://", "http://");
            System.out.println("Trying HTTP fallback: " + httpUrl);
            
            try {
                Image httpImage = new Image(httpUrl, 200, 150, true, true, true);
                
                httpImage.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        // HTTP version loaded successfully
                        imagePreview.setImage(httpImage);
                        System.out.println("HTTP fallback successful");
                    } else {
                        // Try next fallback strategy
                        tryFormatFallback(originalUrl, parent, container, imageIndex);
                    }
                });
                
                // If loads immediately
                if (!httpImage.isError() && httpImage.getProgress() == 1.0) {
                    imagePreview.setImage(httpImage);
                    System.out.println("HTTP fallback loaded immediately");
                    return;
                }
            } catch (Exception e) {
                System.err.println("HTTP fallback failed: " + e.getMessage());
                // Continue to next fallback
            }
        } else {
            // Skip to next fallback immediately if not HTTPS
            tryFormatFallback(originalUrl, parent, container, imageIndex);
        }
    }
    
    /**
     * Try different image formats as a fallback strategy
     */
    private void tryFormatFallback(String originalUrl, javafx.scene.Parent parent, 
                                  javafx.scene.layout.StackPane container, int imageIndex) {
        // Only for Cloudinary URLs
        if (originalUrl.contains("cloudinary.com")) {
            for (String format : new String[]{"jpg", "png", "gif"}) {
                // Find the upload part
                int uploadIndex = originalUrl.indexOf("/upload/");
                if (uploadIndex > 0) {
                    String prefix = originalUrl.substring(0, uploadIndex + 8); // after "/upload/"
                    
                    // Handle possible transformation path in the URL
                    String suffix = originalUrl.substring(uploadIndex + 8);
                    int slashAfterVersion = suffix.indexOf("/");
                    
                    String formatUrl;
                    if (slashAfterVersion > 0 && suffix.startsWith("v")) {
                        // URL has version number like v1234567890/
                        String version = suffix.substring(0, slashAfterVersion + 1);
                        String filename = suffix.substring(slashAfterVersion + 1);
                        
                        // Replace file extension or add new one
                        if (filename.contains(".")) {
                            int lastDot = filename.lastIndexOf(".");
                            filename = filename.substring(0, lastDot);
                        }
                        
                        formatUrl = prefix + version + "f_" + format + "/" + filename + "." + format;
                    } else {
                        // URL doesn't have version, add format transformation directly
                        formatUrl = prefix + "f_" + format + "/" + suffix;
                        
                        // Ensure file extension matches format
                        if (formatUrl.contains(".")) {
                            int lastDot = formatUrl.lastIndexOf(".");
                            formatUrl = formatUrl.substring(0, lastDot) + "." + format;
                        } else {
                            formatUrl += "." + format;
                        }
                    }
                    
                    System.out.println("Trying format fallback: " + formatUrl);
                    
                    try {
                        final String currentFormat = format;
                        Image formatImage = new Image(formatUrl, 200, 150, true, true, true);
                        
                        formatImage.errorProperty().addListener((obs, oldVal, newVal) -> {
                            if (!newVal) {
                                // Format version loaded successfully
                                imagePreview.setImage(formatImage);
                                System.out.println(currentFormat.toUpperCase() + " format fallback successful");
                                
                                // Restore original image view structure
                                restoreImageView(parent, container, imageIndex);
                            } else if (currentFormat.equals("gif")) {
                                // We've tried all formats, show placeholder and restore view
                                loadPlaceholderAndRestore(parent, container, imageIndex);
                            }
                        });
                        
                        // If loads immediately
                        if (!formatImage.isError() && formatImage.getProgress() == 1.0) {
                            imagePreview.setImage(formatImage);
                            System.out.println(format.toUpperCase() + " format fallback loaded immediately");
                            
                            // Restore original image view structure
                            restoreImageView(parent, container, imageIndex);
                            return;
                        }
                        
                        // Only wait for one format at a time to avoid race conditions
                        break;
                    } catch (Exception e) {
                        System.err.println(format.toUpperCase() + " format fallback failed: " + e.getMessage());
                        // Try next format
                    }
                }
            }
        } else {
            // Not a Cloudinary URL, just use placeholder
            loadPlaceholderAndRestore(parent, container, imageIndex);
        }
    }
    
    /**
     * Load placeholder image and restore the original view structure
     */
    private void loadPlaceholderAndRestore(javafx.scene.Parent parent, 
                                          javafx.scene.layout.StackPane container, 
                                          int imageIndex) {
        try {
            // All fallbacks failed, use placeholder
            Image placeholderImage = new Image(
                getClass().getResourceAsStream("/images/product-placeholder.png"),
                200, 150, true, true
            );
            imagePreview.setImage(placeholderImage);
            System.out.println("Using default placeholder after all fallbacks failed");
            
            // Restore original image view structure
            restoreImageView(parent, container, imageIndex);
        } catch (Exception ex) {
            System.err.println("Failed to load placeholder: " + ex.getMessage());
        }
    }
    
    /**
     * Restore the original view structure by removing container and placing ImageView back
     */
    private void restoreImageView(javafx.scene.Parent parent, 
                                 javafx.scene.layout.StackPane container, 
                                 int imageIndex) {
        if (parent instanceof javafx.scene.layout.Pane && imageIndex >= 0) {
            javafx.application.Platform.runLater(() -> {
                javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) parent;
                
                // Only make changes if container is still a child of the pane
                if (pane.getChildren().contains(container)) {
                    // Remove container from pane
                    pane.getChildren().remove(container);
                    
                    // Remove image view from container
                    container.getChildren().remove(imagePreview);
                    
                    // Add image view back to pane at original position
                    if (imageIndex < pane.getChildren().size()) {
                        pane.getChildren().add(imageIndex, imagePreview);
                    } else {
                        pane.getChildren().add(imagePreview);
                    }
                }
            });
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

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Open the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Show loading indicator with progress bar
                Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
                progressAlert.setTitle("Uploading Image");
                progressAlert.setHeaderText("Please wait while the image is being uploaded...");
                
                // Create a progress indicator
                javafx.scene.control.ProgressIndicator progressIndicator = new javafx.scene.control.ProgressIndicator();
                progressIndicator.setProgress(javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS);
                
                // Create a VBox to hold the progress indicator and message
                javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
                content.setAlignment(javafx.geometry.Pos.CENTER);
                content.getChildren().addAll(
                    new javafx.scene.control.Label("This may take a few moments depending on your internet connection."),
                    progressIndicator
                );
                
                progressAlert.getDialogPane().setContent(content);
                
                // Add a Cancel button to the dialog
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                progressAlert.getButtonTypes().setAll(cancelButton);
                
                // Create a reference to store the alert
                final Alert[] alertRef = {progressAlert};
                
                // Create a new thread for the upload to keep UI responsive
                Thread uploadThread = new Thread(() -> {
                    try {
                        // Validate file size
                        if (selectedFile.length() > 10 * 1024 * 1024) { // 10MB limit
                            javafx.application.Platform.runLater(() -> {
                                if (alertRef[0] != null) {
                                    alertRef[0].close();
                                    alertRef[0] = null;
                                }
                                showError("File Too Large", "The selected image exceeds the maximum size of 10MB. Please select a smaller image.");
                            });
                            return;
                        }
                        
                        System.out.println("Starting upload of file: " + selectedFile.getAbsolutePath());
                        
                        // Upload the image to Cloudinary
                        String uploadedImageUrl = CloudinaryUtil.uploadImage(selectedFile);
                        
                        // Update UI on JavaFX thread
                        javafx.application.Platform.runLater(() -> {
                            // Close the progress alert
                            if (alertRef[0] != null) {
                                alertRef[0].close();
                                alertRef[0] = null;
                            }
                            
                            // Set the uploaded image URL in the "Image URL" field
                            imageUrlField.setText(uploadedImageUrl);
                            
                            // Show success message
                            showSuccess("Upload Successful", "Image uploaded successfully!");
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        
                        // Update UI on JavaFX thread
                        javafx.application.Platform.runLater(() -> {
                            // Close the progress alert
                            if (alertRef[0] != null) {
                                alertRef[0].close();
                                alertRef[0] = null;
                            }
                            
                            // Check for Cloudinary authentication/signature errors
                            if (e.getMessage().contains("Invalid Signature") || 
                                e.getMessage().contains("authentication") ||
                                e.getMessage().contains("unauthorized")) {
                                
                                Alert authAlert = new Alert(Alert.AlertType.ERROR);
                                authAlert.setTitle("Cloudinary Error");
                                authAlert.setHeaderText("Authentication Error with Cloudinary");
                                authAlert.setContentText("There was an authentication issue with Cloudinary. Would you like to use a local file instead?");
                                
                                ButtonType localButton = new ButtonType("Use Local File");
                                ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                                
                                authAlert.getButtonTypes().setAll(localButton, cancelBtn);
                                
                                ButtonType result = authAlert.showAndWait().orElse(cancelBtn);
                                
                                if (result == localButton) {
                                    // Use a local file URL
                                    try {
                                        String localUrl = selectedFile.toURI().toURL().toString();
                                        imageUrlField.setText(localUrl);
                                        showSuccess("Local Image", "Using local image file. Note: This will only work on your computer.");
                                    } catch (Exception ex) {
                                        showError("Error", "Could not use local file: " + ex.getMessage());
                                    }
                                }
                            }
                            // Check if this is a network error
                            else if (e.getMessage().contains("connection") || 
                                e.getMessage().contains("network") ||
                                e.getMessage().contains("timeout")) {
                                
                                Alert retryAlert = new Alert(Alert.AlertType.ERROR);
                                retryAlert.setTitle("Network Error");
                                retryAlert.setHeaderText("Connection Problem");
                                retryAlert.setContentText("Could not connect to Cloudinary. Would you like to try again or use a local file?");
                                
                                ButtonType retryButton = new ButtonType("Retry");
                                ButtonType localButton = new ButtonType("Use Local File");
                                ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                                
                                retryAlert.getButtonTypes().setAll(retryButton, localButton, cancelBtn);
                                
                                ButtonType result = retryAlert.showAndWait().orElse(cancelBtn);
                                
                                if (result == retryButton) {
                                    // Retry the upload
                                    handleUploadImage();
                                } else if (result == localButton) {
                                    // Use a local file URL
                                    try {
                                        String localUrl = selectedFile.toURI().toURL().toString();
                                        imageUrlField.setText(localUrl);
                                        showSuccess("Local Image", "Using local image file. Note: This will only work on your computer.");
                                    } catch (Exception ex) {
                                        showError("Error", "Could not use local file: " + ex.getMessage());
                                    }
                                }
                            } else {
                                // Show regular error message for other types of errors
                                showError("Upload Failed", "Could not upload the image: " + e.getMessage());
                            }
                        });
                    }
                });
                
                // Set the thread as a daemon to ensure it doesn't prevent application exit
                uploadThread.setDaemon(true);
                
                // Start the upload thread
                uploadThread.start();
                
                // When the cancel button is clicked, close the alert and interrupt the thread
                progressAlert.setOnCloseRequest(event -> {
                    System.out.println("Upload cancelled by user");
                    uploadThread.interrupt();
                    alertRef[0] = null;
                });
                
                // Show the alert and wait for it to be closed
                progressAlert.show();
                
            } catch (Exception e) {
                showError("Upload Failed", "Could not upload the image: " + e.getMessage());
            }
        }
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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