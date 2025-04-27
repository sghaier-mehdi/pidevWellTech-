package com.welltech.controller;

import com.welltech.model.Consultation;
import com.welltech.model.Consultation.ConsultationStatus;
import com.welltech.model.User; // Assuming User model is needed (not directly in this controller's logic, but good practice)

// === JavaFX Imports ===
import javafx.application.Platform;
import javafx.event.ActionEvent; // *** ADD THIS IMPORT ***
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox; // Might be needed for styling or structure? (Remove if not used)
import javafx.scene.layout.StackPane; // If using StackPane for image
import javafx.scene.layout.VBox; // Might be needed for styling or structure? (Remove if not used)
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap; // For ZXing hints (Use HashMap instead of Hashtable)
import java.util.Map; // For ZXing hints
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

// === ZXing Imports ===
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter; // Or QRCodeWriter
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
 // *** ADD THIS IMPORT ***
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter; // Alternative writer
// === End ZXing Imports ===

// === Image Conversion Imports (from java.desktop module) ===
import java.awt.image.BufferedImage; // *** ADD THIS IMPORT ***
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException; // Needed for ImageIO streams
import javax.imageio.ImageIO; // *** ADD THIS IMPORT ***
// === End Imports ===
import javafx.scene.Node; // Add this import

public class ConsultationDetailController implements Initializable {

    // === FXML Injections ===
    @FXML private StackPane qrCodeContainer; // Container for QR code image
    @FXML private ImageView qrCodeImageView; // The ImageView for the QR code

    @FXML private Label statusLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label patientLabel;
    @FXML private Label psychiatristLabel;
    @FXML private Label purposeLabel;
    @FXML private Label durationLabel;
    @FXML private TextFlow notesFlow;

    // Optional: Navigation buttons if integrated into main layout (add injections if they exist in FXML)
    /*
    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    @FXML private Button dashboardButton;
    @FXML private Button articlesButton;
    @FXML private Button consultationsButton;
    @FXML private Button profileButton;
    */

    // --- Data ---
    private Consultation consultation;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize components or bindings if needed
        if (qrCodeContainer != null) {
            qrCodeContainer.managedProperty().bind(qrCodeContainer.visibleProperty());
            qrCodeContainer.setVisible(false); // Hide until QR is generated
        }
        // Initialize header/sidebar labels/buttons if they exist in the FXML
         /*
         User currentUser = LoginController.getCurrentUser(); // Assuming LoginController is accessible
         if (currentUser != null && userNameLabel != null) {
             userNameLabel.setText("Welcome, " + currentUser.getFullName());
         }
         // Add active style to the correct sidebar button (likely Consultations)
         // removeActiveStyle(dashboardButton); // Helper needed
         // removeActiveStyle(articlesButton);
         // ...
         // if (consultationsButton != null) consultationsButton.getStyleClass().add("active");
         */
    }

    /**
     * Called by the loading controller (ConsultationsListController) to pass data.
     */
    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
        if (consultation != null) {
            displayConsultationDetails();
            generateAndDisplayQrCode(consultation); // Trigger QR code generation
        } else {
            System.err.println("ConsultationDetailController received a null consultation.");
            // Optionally display an error message on the view
            // e.g., notesFlow.getChildren().setAll(new Text("Error loading consultation details."));
        }
    }

    /**
     * Populates the UI elements with the consultation data.
     */
    private void displayConsultationDetails() {
        if (consultation == null) return;

        // Populate Labels
        dateTimeLabel.setText(consultation.getConsultationTime() != null ? consultation.getConsultationTime().format(DATE_FORMATTER) : "Date/Time N/A");
        patientLabel.setText("Patient: " + consultation.getPatientName());
        psychiatristLabel.setText("Psychiatrist: " + consultation.getPsychiatristName());
        purposeLabel.setText("Purpose: " + (consultation.getPurpose() != null ? consultation.getPurpose() : "N/A"));
        durationLabel.setText("Duration: " + (consultation.getDurationMinutes() > 0 ? consultation.getDurationMinutes() + " minutes" : "N/A"));

        // Populate Status Label (reuse styling logic from Card View)
        statusLabel.setText(consultation.getStatusDisplayValue());
        statusLabel.getStyleClass().removeAll("card-status-info", "card-status-success", "card-status-danger", "card-status-warning", "card-status-draft"); // Remove all before adding
        if (consultation.getStatus() != null) {
            switch (consultation.getStatus()) {
                case SCHEDULED: statusLabel.getStyleClass().add("card-status-info"); break;
                case COMPLETED: statusLabel.getStyleClass().add("card-status-success"); break;
                case CANCELLED: statusLabel.getStyleClass().add("card-status-danger"); break;
                case RESCHEDULED: statusLabel.getStyleClass().add("card-status-warning"); break;
                default: statusLabel.getStyleClass().add("card-status-draft"); break;
            }
        } else {
            statusLabel.getStyleClass().add("card-status-draft");
        }

        // Populate Notes TextFlow
        notesFlow.getChildren().clear();
        String notes = consultation.getNotes();
        if (notes != null && !notes.trim().isEmpty()) {
            String[] paragraphs = notes.split("\\n");
            for (String para : paragraphs) {
                if (!para.trim().isEmpty()) {
                    notesFlow.getChildren().add(new Text(para.trim() + "\n"));
                }
            }
        } else {
            notesFlow.getChildren().add(new Text("No notes available."));
        }
    }

    /**
     * Generates the QR code in a background task and displays it.
     */
    private void generateAndDisplayQrCode(Consultation consultation) {
        if (consultation == null) return;

        // --- 1. Prepare Data to Encode ---
        String qrData = formatConsultationDataForQr(consultation);
        if (qrData == null || qrData.trim().isEmpty()) { // Check for null or empty
            System.err.println("QR Code data is null or empty, skipping generation.");
            if (qrCodeContainer != null) qrCodeContainer.setVisible(false); // Hide container if no data
            return;
        }
        System.out.println("Encoding QR Code with data: " + qrData);

        // --- 2. Generate QR Code in Background ---
        CompletableFuture.supplyAsync(() -> {
                    try {
                        int size = 200;
                        String fileType = "png";

                        Map<EncodeHintType, Object> hints = new HashMap<>(); // Use HashMap instead of Hashtable
                        // Add hints if needed, e.g., hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

                        QRCodeWriter qrCodeWriter = new QRCodeWriter();
                        BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, size, size, hints);

                        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, fileType, outputStream);
                        byte[] imageBytes = outputStream.toByteArray();

                        return new Image(new ByteArrayInputStream(imageBytes)); // Return JavaFX Image

                    } catch (WriterException e) {
                        System.err.println("Error generating QR Code BitMatrix: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    } catch (IOException e) { // Catch ImageIO exceptions explicitly
                        System.err.println("Error converting QR Code image to bytes: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    } catch (Exception e) { // Catch any other unexpected errors
                        System.err.println("Unexpected error during QR code generation: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                })
                .thenAcceptAsync(javafxImage -> {
                    // --- 3. Display QR Code on FX Thread ---
                    if (javafxImage != null && !javafxImage.isError()) { // Check for load errors
                        qrCodeImageView.setImage(javafxImage);
                        if (qrCodeContainer != null) qrCodeContainer.setVisible(true); // Show the container
                        System.out.println("QR Code image displayed.");
                    } else {
                        qrCodeImageView.setImage(null);
                        if (qrCodeContainer != null) qrCodeContainer.setVisible(false); // Hide container on failure
                        System.err.println("QR Code generation failed or image had error, image not displayed.");
                    }
                }, Platform::runLater)
                .exceptionally(ex -> {
                    System.err.println("Unexpected error after QR code task: " + ex.getMessage());
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        qrCodeImageView.setImage(null);
                        if (qrCodeContainer != null) qrCodeContainer.setVisible(false);
                        // Optionally show an error message on the UI
                    });
                    return null;
                });
    }

    /**
     * Formats consultation data into a string for the QR code.
     */
    private String formatConsultationDataForQr(Consultation consultation) {
        if (consultation == null) return null; // Return null if consultation is null

        StringBuilder sb = new StringBuilder();
        sb.append("Consultation ID: ").append(consultation.getId()).append("\n");
        sb.append("Time: ").append(consultation.getConsultationTime() != null ? consultation.getConsultationTime().format(DATE_FORMATTER) : "N/A").append("\n");
        sb.append("Patient: ").append(consultation.getPatientName()).append("\n");
        sb.append("Psychiatrist: ").append(consultation.getPsychiatristName()).append("\n");
        sb.append("Purpose: ").append(consultation.getPurpose() != null ? consultation.getPurpose() : "N/A").append("\n");
        sb.append("Status: ").append(consultation.getStatusDisplayValue());

        return sb.toString();
    }


    /**
     * Handles the Close button action.
     * MODIFIED to only close the stage for the dialog scenario.
     */
    @FXML
    private void handleClose() {
        // Get the Stage this scene belongs to (assuming it's in a dialog/separate window)
        Stage stage = (Stage) ((Node)dateTimeLabel).getScene().getWindow();
        if (stage != null) {
            stage.close(); // Close the window
        } else {
            // If integrated into main layout, navigate back to list
            // This block should ideally not be reached if the view is always a dialog.
            // Removed the incorrect navigateToConsultations call from here.
            System.err.println("Consultation Detail View is not in a Stage? Cannot close or navigate.");
            // Optionally add logic here if this view can be displayed directly in the main layout.
        }
    }


    // === Navigation Handlers (Add these if integrated into main layout) ===
    // These methods handle navigation when sidebar/header buttons are clicked
    /*
    @FXML private void handleLogout(ActionEvent event) { LoginController.logout(); }

    @FXML private void navigateToDashboard(ActionEvent event) {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            switch (currentUser.getRole()) {
                case ADMIN: WellTechApplication.loadFXML("adminDashboard"); break;
                case PSYCHIATRIST: WellTechApplication.loadFXML("psychiatristDashboard"); break;
                case PATIENT: WellTechApplication.loadFXML("patientDashboard"); break;
                default: WellTechApplication.loadFXML("login");
            }
        } else { WellTechApplication.loadFXML("login"); }
    }

    @FXML private void navigateToArticles(ActionEvent event) { WellTechApplication.loadFXML("articlesList"); }

    // This controller is for the Detail view, so clicking Consultations in sidebar might just close the dialog or reload list
    @FXML private void navigateToConsultations(ActionEvent event) {
        // If this view is in a dialog, close the dialog first:
        Stage stage = (Stage) ((Node)dateTimeLabel).getScene().getWindow();
        if (stage != null) {
             stage.close();
        }
        WellTechApplication.loadFXML("consultationsList"); // Then navigate to the list
    }

    @FXML private void navigateToProfile(ActionEvent event) { WellTechApplication.loadFXML("profile"); }
    */
    // Add other navigation methods if applicable
}