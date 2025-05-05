package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.ConsultationDAO;
import com.welltech.dao.UserDAO;
import com.welltech.model.Consultation;
import com.welltech.model.Consultation.ConsultationStatus;
import com.welltech.model.User;
import com.welltech.model.User.UserRole;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
// === REMOVE THIS INCORRECT IMPORT ===
// import javafx.scene.layout.ButtonBox; // This class does not exist in javafx.scene.layout
// ====================================
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

// Needed for layout classes like HBox, VBox
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class ConsultationEditController implements Initializable {

    // === FXML Injections for Header/Sidebar (Add these) ===
    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    @FXML private Button dashboardButton;
    @FXML private Button articlesButton; // Add injection for Articles button
    @FXML private Button consultationsButton; // Injection for THIS view's button
    @FXML private Button profileButton; // Assuming this exists in the sidebar
    // Add injections for other sidebar buttons if applicable (e.g., patientsButton, messagesButton)
    // @FXML private Button patientsButton;
    // @FXML private Button messagesButton;
    // ======================================================

    // --- FXML Injections for Form Fields ---
    @FXML private Text pageTitle;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField durationField;
    @FXML private ComboBox<User> patientComboBox;
    @FXML private ComboBox<User> psychiatristComboBox;
    @FXML private TextField purposeField;
    @FXML private TextArea notesArea;
    @FXML private ComboBox<ConsultationStatus> statusComboBox;

    // --- Error Labels ---
    @FXML private Label dateTimeError;
    @FXML private Label durationError;
    @FXML private Label patientError;
    @FXML private Label psychiatristError;
    @FXML private Label purposeError;
    @FXML private Label notesError;
    @FXML private Label statusError;
    @FXML private Label statusMessage; // Main feedback label

    // --- Buttons ---
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // --- DAOs and Data ---
    private ConsultationDAO consultationDAO;
    private UserDAO userDAO;
    private User currentUser;
    private Consultation currentConsultation;
    private boolean isEditMode = false;

    // Observable lists for ComboBoxes
    private ObservableList<User> patientsList = FXCollections.observableArrayList();
    private ObservableList<User> psychiatristsList = FXCollections.observableArrayList();
    private ObservableList<ConsultationStatus> statusList = FXCollections.observableArrayList(ConsultationStatus.values());


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        consultationDAO = new ConsultationDAO();
        userDAO = new UserDAO();
        currentUser = LoginController.getCurrentUser();

        if (currentUser == null) {
            System.err.println("No user logged in for ConsultationEditController.");
            WellTechApplication.loadFXML("login");
            return;
        }

        if (userNameLabel != null) {
            userNameLabel.setText("Welcome, " + currentUser.getFullName());
        }

        // === Sidebar Button Styling ===
        // Remove 'active' style from ALL sidebar buttons initially
        removeActiveStyle(dashboardButton);
        removeActiveStyle(articlesButton);
        removeActiveStyle(consultationsButton); // Ensure this button exists in FXML
        removeActiveStyle(profileButton);
        // removeActiveStyle(patientsButton); // If applicable
        // removeActiveStyle(messagesButton); // If applicable

        // Add 'active' style ONLY to the button for THIS view
        if (consultationsButton != null) {
            consultationsButton.getStyleClass().add("active");
        }
        // ==============================

        setupComboBoxes(); // Configure ComboBox display and load data
        setupErrorClearListeners();

        // Get the consultation passed from the list view (or null)
        currentConsultation = WellTechApplication.getCurrentConsultation();

        if (currentConsultation != null) {
            isEditMode = true;
            pageTitle.setText("Edit Consultation");
            populateConsultationFields();
        } else {
            isEditMode = false;
            pageTitle.setText("Schedule New Consultation");
            statusComboBox.setValue(ConsultationStatus.SCHEDULED);
        }

        // Role-based restrictions (Keep as is)
        if (currentUser.getRole() == User.UserRole.PATIENT) {
            setFeedback("You don't have permission to schedule or edit consultations.", Color.RED);
            saveButton.setDisable(true);
            setFieldsEditable(false);
        } else if (currentUser.getRole() == User.UserRole.PSYCHIATRIST) {
            if (!isEditMode) {
                psychiatristComboBox.setValue(currentUser);
                // psychiatristComboBox.setDisable(true);
            } else {
                if (currentConsultation.getPsychiatrist() == null || currentConsultation.getPsychiatrist().getId() != currentUser.getId() && currentUser.getRole() != User.UserRole.ADMIN) {
                    setFeedback("You can only edit consultations you are assigned to.", Color.RED);
                    saveButton.setDisable(true);
                    setFieldsEditable(false);
                }
            }
            // Permissions for changing patient/psychiatrist on edit
            patientComboBox.setDisable(currentUser.getRole() != User.UserRole.ADMIN && isEditMode);
            psychiatristComboBox.setDisable(currentUser.getRole() != User.UserRole.ADMIN && isEditMode);
        }
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            setFieldsEditable(true);
        }
    }

    // Helper to remove 'active' style safely (Add this)
    private void removeActiveStyle(Button button) {
        if (button != null) {
            button.getStyleClass().remove("active");
        }
    }


    private void setFieldsEditable(boolean editable) {
        datePicker.setDisable(!editable);
        timeField.setDisable(!editable);
        durationField.setDisable(!editable);
        patientComboBox.setDisable(!editable);
        psychiatristComboBox.setDisable(!editable);
        purposeField.setDisable(!editable);
        notesArea.setDisable(!editable);
        statusComboBox.setDisable(!editable);
        saveButton.setDisable(!editable);
    }


    private void setupComboBoxes() {
        statusComboBox.setItems(statusList);

        StringConverter<User> userConverter = new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user != null ? user.getFullName() : "";
            }
            @Override
            public User fromString(String string) { return null; }
        };
        patientComboBox.setConverter(userConverter);
        psychiatristComboBox.setConverter(userConverter);

        setFeedback("Loading users...", Color.BLUE);
        Runnable loadUsersTask = () -> {
            try {
                List<User> patients = userDAO.getUsersByRole(UserRole.PATIENT);
                List<User> psychiatrists = userDAO.getUsersByRole(UserRole.PSYCHIATRIST);

                Platform.runLater(() -> {
                    patientsList.setAll(patients);
                    psychiatristsList.setAll(psychiatrists);
                    patientComboBox.setItems(patientsList);
                    psychiatristComboBox.setItems(psychiatristsList);

                    if (isEditMode && currentConsultation != null) {
                        selectComboBoxValue(patientComboBox, currentConsultation.getPatient());
                        selectComboBoxValue(psychiatristComboBox, currentConsultation.getPsychiatrist());
                        statusComboBox.setValue(currentConsultation.getStatus());
                    }
                    setFeedback("Users loaded.", Color.GREEN);
                    if (currentUser.getRole() == User.UserRole.PATIENT) {
                        setFieldsEditable(false);
                        saveButton.setDisable(true);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error loading users for ComboBoxes: " + e.getMessage());
                Platform.runLater(() -> setFeedback("Failed to load user lists.", Color.RED));
            }
        };
        new Thread(loadUsersTask).start();
    }

    private void selectComboBoxValue(ComboBox<User> comboBox, User userToSelect) {
        if (userToSelect != null && comboBox.getItems() != null) {
            comboBox.getItems().stream()
                    .filter(u -> u != null && u.getId() == userToSelect.getId())
                    .findFirst()
                    .ifPresent(comboBox::setValue);
        }
    }

    private void setupErrorClearListeners() {
        datePicker.valueProperty().addListener((obs, old, newVal) -> clearError(dateTimeError));
        timeField.textProperty().addListener((obs, old, newVal) -> clearError(dateTimeError));
        durationField.textProperty().addListener((obs, old, newVal) -> clearError(durationError));
        patientComboBox.valueProperty().addListener((obs, old, newVal) -> clearError(patientError));
        psychiatristComboBox.valueProperty().addListener((obs, old, newVal) -> clearError(psychiatristError));
        purposeField.textProperty().addListener((obs, old, newVal) -> clearError(purposeError));
        notesArea.textProperty().addListener((obs, old, newVal) -> clearError(notesError));
        statusComboBox.valueProperty().addListener((obs, old, newVal) -> clearError(statusError));

        datePicker.valueProperty().addListener((obs, old, newVal) -> clearStatus());
        timeField.textProperty().addListener((obs, old, newVal) -> clearStatus());
        durationField.textProperty().addListener((obs, old, newVal) -> clearStatus());
        patientComboBox.valueProperty().addListener((obs, old, newVal) -> clearStatus());
        psychiatristComboBox.valueProperty().addListener((obs, old, newVal) -> clearStatus());
        purposeField.textProperty().addListener((obs, old, newVal) -> clearStatus());
        notesArea.textProperty().addListener((obs, old, newVal) -> clearStatus());
        statusComboBox.valueProperty().addListener((obs, old, newVal) -> clearStatus());
    }

    private void clearError(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        clearStatus();
    }
    private void clearStatus() {
        statusMessage.setText("");
    }

    /**
     * Populate form fields with current consultation data in edit mode.
     */
    private void populateConsultationFields() {
        if (currentConsultation == null) return;

        datePicker.setValue(currentConsultation.getConsultationTime().toLocalDate());
        timeField.setText(currentConsultation.getConsultationTime().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        durationField.setText(String.valueOf(currentConsultation.getDurationMinutes()));
        // Patient/Psychiatrist/Status ComboBoxes are set in setupComboBoxes()
        purposeField.setText(currentConsultation.getPurpose());
        notesArea.setText(currentConsultation.getNotes());
    }


    /**
     * Handle save button click. Performs input control (validation) and saves/updates.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        clearStatus();
        if (!validateForm()) {
            setFeedback("Please fix the errors above.", Color.RED);
            return;
        }

        LocalDate date = datePicker.getValue();
        LocalTime time = LocalTime.parse(timeField.getText());
        LocalDateTime consultationDateTime = LocalDateTime.of(date, time);
        int duration = Integer.parseInt(durationField.getText().trim());
        User patient = patientComboBox.getValue();
        User psychiatrist = psychiatristComboBox.getValue();
        String purpose = purposeField.getText().trim();
        String notes = notesArea.getText().trim();
        ConsultationStatus status = statusComboBox.getValue();

        setFeedback("Saving consultation...", Color.BLUE);
        saveButton.setDisable(true);

        Consultation consultationToSave;
        if (isEditMode) {
            consultationToSave = currentConsultation;
        } else {
            consultationToSave = new Consultation();
        }

        consultationToSave.setPatient(patient);
        consultationToSave.setPsychiatrist(psychiatrist);
        consultationToSave.setConsultationTime(consultationDateTime);
        consultationToSave.setDurationMinutes(duration);
        consultationToSave.setPurpose(purpose);
        consultationToSave.setNotes(notes);
        consultationToSave.setStatus(status);

        Runnable saveTask = () -> {
            boolean success = false;
            try {
                if (isEditMode) {
                    success = consultationDAO.updateConsultation(consultationToSave);
                } else {
                    int newId = consultationDAO.insertConsultation(consultationToSave);
                    success = newId > 0;
                    if(success) {
                        consultationToSave.setId(newId);
                    }
                }

                boolean finalSuccess = success;
                Platform.runLater(() -> {
                    if (finalSuccess) {
                        String action = isEditMode ? "updated" : "scheduled";
                        setFeedback("Consultation " + action + " successfully!", Color.GREEN);
                        navigateToConsultations(null);
                    } else {
                        String action = isEditMode ? "update" : "schedule";
                        setFeedback("Failed to " + action + " consultation.", Color.RED);
                        saveButton.setDisable(false);
                    }
                });

            } catch (Exception e) {
                System.err.println("Error saving consultation to DB: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    setFeedback("Database error while saving: " + e.getMessage(), Color.RED);
                    saveButton.setDisable(false);
                });
            }
        };
        new Thread(saveTask).start();
    }

    /**
     * Performs input control (validation) for the form fields.
     */
    private boolean validateForm() {
        boolean isValid = true;
        clearError(dateTimeError); clearError(durationError); clearError(patientError);
        clearError(psychiatristError); clearError(purposeError); clearError(notesError);
        clearError(statusError);

        // --- Validate Date & Time ---
        LocalDate date = datePicker.getValue();
        String timeText = timeField.getText().trim();
        if (date == null) {
            dateTimeError.setText("Date is required.");
            isValid = false;
        }
        LocalTime time = null;
        if (timeText.isEmpty()) {
            if (date == null) {
                dateTimeError.setText("Date and Time are required.");
            } else {
                dateTimeError.setText("Time is required (HH:mm).");
            }
            isValid = false;
        } else {
            try {
                time = LocalTime.parse(timeText, java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e) {
                dateTimeError.setText("Invalid time format (use HH:mm, e.g., 14:30).");
                isValid = false;
            }
        }

        if (date != null && time != null) {
            LocalDateTime consultationDateTime = LocalDateTime.of(date, time);
            if (!isEditMode && consultationDateTime.isBefore(LocalDateTime.now())) {
                dateTimeError.setText("Cannot schedule a consultation in the past.");
                isValid = false;
            }
        }

        // --- Validate Duration ---
        String durationText = durationField.getText().trim();
        if (durationText.isEmpty()) {
            durationError.setText("Duration is required.");
            isValid = false;
        } else {
            try {
                int duration = Integer.parseInt(durationText);
                if (duration <= 0) {
                    durationError.setText("Duration must be a positive number.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                durationError.setText("Duration must be a number.");
                isValid = false;
            }
        }

        // --- Validate Patient ---
        if (patientComboBox.getValue() == null) {
            patientError.setText("Patient is required.");
            isValid = false;
        } else if (patientComboBox.getValue().getRole() != User.UserRole.PATIENT) {
            patientError.setText("Selected user is not a Patient.");
            isValid = false;
        }

        // --- Validate Psychiatrist ---
        if (psychiatristComboBox.getValue() == null) {
            psychiatristError.setText("Psychiatrist is required.");
            isValid = false;
        } else if (psychiatristComboBox.getValue().getRole() != User.UserRole.PSYCHIATRIST && psychiatristComboBox.getValue().getRole() != User.UserRole.ADMIN) {
            psychiatristError.setText("Selected user is not a Psychiatrist or Admin.");
            isValid = false;
        }

        // --- Validate Purpose ---
        String purpose = purposeField.getText().trim();
        if (purpose.isEmpty()) {
            purposeError.setText("Purpose is required.");
            isValid = false;
        }

        // --- Validate Notes --- (Optional as before)

        // --- Validate Status ---
        if (statusComboBox.getValue() == null) {
            statusError.setText("Status is required.");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Sets the feedback message and color. Ensures UI update is on FX thread.
     */
    private void setFeedback(String message, Color color) {
        Platform.runLater(() -> {
            statusMessage.setText(message);
            statusMessage.setTextFill(color);
            statusMessage.getStyleClass().removeAll("success-message", "error-message", "info-message");
            if (color == Color.GREEN) statusMessage.getStyleClass().add("success-message");
            else if (color == Color.RED || color == Color.ORANGE) statusMessage.getStyleClass().add("error-message");
            else statusMessage.getStyleClass().add("info-message");
        });
    }

    @FXML private void handleCancel(ActionEvent event) {
        System.out.println("Cancel button clicked");
        navigateToConsultations(null);
    }

    // === Navigation Methods (Add these) ===
    // These methods handle navigation when sidebar/header buttons are clicked
    @FXML private void handleLogout(ActionEvent event) { LoginController.logout(); }

    @FXML private void navigateToDashboard(ActionEvent event) {
        if (currentUser != null) {
            switch (currentUser.getRole()) {
                case ADMIN: WellTechApplication.loadFXML("adminDashboard"); break;
                case PSYCHIATRIST: WellTechApplication.loadFXML("psychiatristDashboard"); break;
                case PATIENT: WellTechApplication.loadFXML("patientDashboard"); break;
                default: WellTechApplication.loadFXML("login"); // Should not happen if user is logged in
            }
        } else {
            WellTechApplication.loadFXML("login"); // Redirect to login if no user found
        }
    }

    @FXML private void navigateToArticles(ActionEvent event) { WellTechApplication.loadFXML("articlesList"); }

    @FXML private void navigateToConsultations(ActionEvent event) { WellTechApplication.loadFXML("consultationsList"); }

    @FXML private void navigateToProfile(ActionEvent event) { WellTechApplication.loadFXML("profile"); }

    // Add other navigation methods if applicable (e.g., navigateToPatients, navigateToMessages)
    // @FXML private void navigateToPatients(ActionEvent event) { ... }
    // @FXML private void navigateToMessages(ActionEvent event) { ... }
    // ======================================
}