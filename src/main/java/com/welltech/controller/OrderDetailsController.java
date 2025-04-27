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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

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

    @FXML
    private void handleDownloadPdf() {
        if (order == null) {
            showAlert("Error", "No order selected for PDF generation.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Order Details PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(orderIdLabel.getScene().getWindow());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(50, 750);

                // Debug log
                System.out.println("Generating PDF for Order ID: " + order.getId());

                // Add order details
                contentStream.showText("Order Details");
                contentStream.newLine();
                contentStream.showText("Order ID: " + order.getId());
                contentStream.newLine();
                contentStream.showText("User: " + order.getUser().getUsername());
                contentStream.newLine();
                contentStream.showText("Total Amount: $" + order.getTotalAmount());
                contentStream.newLine();
                contentStream.showText("Status: " + order.getStatus());
                contentStream.newLine();
                contentStream.showText("Payment Status: " + order.getPaymentStatus());
                contentStream.newLine();
                contentStream.showText("Shipping Address: " + order.getShippingAddress());
                contentStream.newLine();
                contentStream.showText("Payment Method: " + order.getPaymentMethod());
                contentStream.newLine();
                contentStream.showText("Created At: " + order.getCreatedAt().format(dateFormatter));
                contentStream.newLine();
                contentStream.showText("Updated At: " + order.getUpdatedAt().format(dateFormatter));
                contentStream.newLine();
                contentStream.newLine();

                // Add order items
                contentStream.showText("Order Items:");
                contentStream.newLine();
                for (OrderItem item : order.getOrderItems()) {
                    contentStream.showText("- Product: " + item.getProduct().getName());
                    contentStream.newLine();
                    contentStream.showText("  Quantity: " + item.getQuantity());
                    contentStream.newLine();
                    contentStream.showText("  Unit Price: $" + item.getUnitPrice());
                    contentStream.newLine();
                    contentStream.showText("  Subtotal: $" + item.getSubtotal());
                    contentStream.newLine();
                    contentStream.newLine();
                }

                contentStream.endText();
                contentStream.close();

                document.save(file);
                showAlert("Success", "PDF downloaded successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to download PDF: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}