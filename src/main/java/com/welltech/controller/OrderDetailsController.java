package com.welltech.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.welltech.model.Order;
import com.welltech.model.OrderItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    @FXML
    private Label locationNameLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private ImageView qrCodeImageView;

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

        // Display location details
        if (order.getShippingAddress() != null && !order.getShippingAddress().isEmpty()) {
            locationNameLabel.setText("Shipping Address:");
            cityLabel.setText(order.getShippingAddress());
            // Generate and display QR code with shipping address
            String locationUrl = "https://www.google.com/maps/search/" + order.getShippingAddress().replace(" ", "+");
            generateQRCode(locationUrl);
        } else {
            locationNameLabel.setText("Shipping Address: Not specified");
            cityLabel.setText("");
            qrCodeImageView.setImage(null);
        }

        // Display order items
        itemsTable.setItems(FXCollections.observableArrayList(order.getOrderItems()));
    }

    private void generateQRCode(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);

            BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            Image qrCodeImage = SwingFXUtils.toFXImage(bufferedImage, null);
            qrCodeImageView.setImage(qrCodeImage);
        } catch (WriterException e) {
            showAlert("Error", "Could not generate QR code: " + e.getMessage());
        }
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
                
                // Add location details
                if (order.getShippingAddress() != null && !order.getShippingAddress().isEmpty()) {
                    contentStream.showText("Shipping Address: " + order.getShippingAddress());
                    contentStream.newLine();
                }

                contentStream.endText();
                contentStream.close();

                document.save(file);
                showAlert("Success", "PDF generated successfully!");
            } catch (IOException e) {
                showAlert("Error", "Could not generate PDF: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}