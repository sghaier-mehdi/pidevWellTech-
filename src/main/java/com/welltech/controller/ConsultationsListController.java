package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.ConsultationDAO;
import com.welltech.model.Consultation;
import com.welltech.model.Consultation.ConsultationStatus;
import com.welltech.model.User;
import com.welltech.model.User.UserRole;
import com.welltech.util.ExcelExporter; // Import the Excel Exporter utility

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList; // Although FlowPane doesn't use it directly
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets; // Needed for Insets
import javafx.geometry.Pos;    // Needed for Pos.CENTER, Pos.TOP_LEFT, etc.
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.FlowPane; // Needed for Card View
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser; // Needed for FileChooser
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File; // Needed for File
import java.io.IOException; // Needed for FileChooser and ExcelExporter
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays; // Needed for Arrays.stream
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors; // Needed for Collectors.toList
import java.util.stream.Stream; // Needed for Stream API

public class ConsultationsListController implements Initializable {

    // === FXML Injections for Header/Sidebar ===
    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    @FXML private Button dashboardButton;
    @FXML private Button articlesButton;
    @FXML private Button consultationsButton;
    @FXML private Button profileButton;
    // Add injections for other sidebar buttons if applicable (e.g., patientsButton, messagesButton)
    // @FXML private Button patientsButton;
    // @FXML private Button messagesButton;
    // ==========================================


    @FXML private Button scheduleButton;
    // === FXML Injection for Export Button ===
    @FXML private Button exportButton; // Add Export Button Injection
    // ========================================
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private Label statusLabel;

    @FXML private ScrollPane scrollPane;
    @FXML private FlowPane consultationContainer;

    private ConsultationDAO consultationDAO;
    private User currentUser;
    private ObservableList<Consultation> masterList = FXCollections.observableArrayList();

    private Consultation selectedConsultation = null;
    private Node selectedConsultationCard = null;

    private static final DateTimeFormatter CARD_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        consultationDAO = new ConsultationDAO();
        currentUser = LoginController.getCurrentUser();

        if (currentUser == null) {
            System.err.println("ConsultationsListController: No user logged in."); // <-- Debug Print
            WellTechApplication.loadFXML("login");
            return;
        }
        System.out.println("ConsultationsListController: Initializing for user: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")"); // <-- Debug Print


        if (userNameLabel != null) {
            userNameLabel.setText("Welcome, " + currentUser.getFullName());
        }

        // === Sidebar Button Styling ===
        removeActiveStyle(dashboardButton);
        removeActiveStyle(articlesButton);
        removeActiveStyle(consultationsButton);
        removeActiveStyle(profileButton);
        // removeActiveStyle(patientsButton);
        // removeActiveStyle(messagesButton);

        if (consultationsButton != null) {
            consultationsButton.getStyleClass().add("active");
        }
        // ==============================


        setupFilters();
        setupSearchAndFilterListeners(); // Setup listeners *before* loading data

        // Restrict scheduling/exporting based on role
        if (currentUser.getRole() == User.UserRole.PATIENT) {
            // Hide Schedule button for Patients
            if (scheduleButton != null) { // Check if FXML element is initialized (it won't be for Patient)
                scheduleButton.setVisible(false);
                scheduleButton.setManaged(false);
            }
            // === Hide Export Button for Patients ===
            if (exportButton != null) { // Check if FXML element is initialized (it won't be for Patient)
                exportButton.setVisible(false);
                exportButton.setManaged(false);
            }
            // =====================================

            // === Load Patient Calendar View ===
            System.out.println("ConsultationsListController: Loading Patient Calendar View.");
            WellTechApplication.loadFXML("patientConsultationsCalendarView");
            // Return immediately as this controller's FXML elements (Card View specific) won't be initialized
            return;
            // ==================================

        } else {
            // If Admin or Psychiatrist, proceed with the Card View initialization
            System.out.println("ConsultationsListController: Loading Admin/Psychiatrist Card View.");

            // Show Schedule button (it's hidden by default in Patient path above)
            if (scheduleButton != null) {
                scheduleButton.setVisible(true);
                scheduleButton.setManaged(true);
            }

            // === Hide Export Button for Psychiatrists if needed (Optional) ===
            // Example: if (exportButton != null && currentUser.getRole() == UserRole.PSYCHIATRIST) {
            //     exportButton.setVisible(false);
            //     exportButton.setManaged(false);
            // } else if (exportButton != null && currentUser.getRole() == UserRole.ADMIN) {
            //     exportButton.setVisible(true); // Show for Admin
            //     exportButton.setManaged(true);
            // }
            // If you always show Export for Admin/Psychiatrist, no need for this block.
            // Let's assume for now it's shown for Admin/Psychiatrist.

            loadConsultations(); // Load data and display for the Card View
            // === End Standard Card View Initialization ===
        }
        // No code should be here after the initial role dispatch
    }

    // Helper to remove 'active' style safely (for sidebar buttons)
    private void removeActiveStyle(Button button) {
        if (button != null) {
            button.getStyleClass().remove("active");
        }
    }

    // === Card View Specific Methods (Only called if user is Admin/Psychiatrist) ===
    // Check for null FXML elements before using them, as they won't be initialized for Patient view.
    private void setupFilters() {
        List<String> statusDisplayValues = Arrays.stream(ConsultationStatus.values()).map(ConsultationStatus::getDisplayValue).collect(Collectors.toList());
        if (statusFilterComboBox != null) { // Check if FXML element is initialized
            statusFilterComboBox.getItems().addAll("All", "My Consultations", "Upcoming", "Past");
            statusFilterComboBox.getItems().addAll(statusDisplayValues);
            statusFilterComboBox.setValue("All");
        }
    }

    private void setupSearchAndFilterListeners() {
        if (searchField != null) searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayConsultations());
        if (statusFilterComboBox != null) statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayConsultations());
    }

    private void loadConsultations() {
        // This load logic is ONLY for Admin/Psychiatrist now
        setStatus("Loading consultations...", Color.BLUE); // statusLabel check is inside setStatus

        Runnable loadTask = () -> {
            List<Consultation> consultationsFromDB;
            try {
                System.out.println("ConsultationsListController (Card View): Starting background task to load consultations for user role: " + currentUser.getRole());
                if (currentUser.getRole() == User.UserRole.ADMIN) {
                    consultationsFromDB = consultationDAO.getAllConsultations();
                } else { // Must be PSYCHIATRIST based on the check in initialize
                    System.out.println("ConsultationsListController (Card View): Fetching consultations for Psychiatrist ID: " + currentUser.getId());
                    consultationsFromDB = consultationDAO.getConsultationsByPsychiatrist(currentUser.getId());
                }
                System.out.println("ConsultationsListController (Card View): DAO returned " + consultationsFromDB.size() + " consultations from DB.");

                consultationsFromDB.sort((c1, c2) -> {
                    if (c1.getConsultationTime() == null && c2.getConsultationTime() == null) return 0;
                    if (c1.getConsultationTime() == null) return 1; // Nulls last
                    if (c2.getConsultationTime() == null) return -1; // Nulls last
                    return c2.getConsultationTime().compareTo(c1.getConsultationTime()); // Descending
                });

                Platform.runLater(() -> {
                    masterList.setAll(consultationsFromDB);
                    System.out.println("ConsultationsListController (Card View): Master list updated on FX thread with " + masterList.size() + " items.");
                    filterAndDisplayConsultations();
                    String msg = masterList.isEmpty() ? "No consultations found." : masterList.size() + " consultations loaded.";
                    setStatus(msg, Color.DARKSLATEGRAY);
                });

            } catch (Exception e) {
                System.err.println("ConsultationsListController (Card View): Error loading consultations: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Failed to load consultations.", Color.RED));
            }
        };
        new Thread(loadTask).start();
    }

    private void filterAndDisplayConsultations() {
        // This filter logic is ONLY for Admin/Psychiatrist now
        // Check if FXML elements are initialized before using them
        if (searchField == null || statusFilterComboBox == null) {
            System.out.println("ConsultationsListController (Card View): Filter/Search FXML elements are null, skipping filtering logic."); // <-- Debug Print
            // If this happens, it means this method was called for a Patient, which shouldn't happen after dispatch.
            return;
        }

        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String statusFilterValue = statusFilterComboBox.getValue();
        System.out.println("ConsultationsListController (Card View): Applying filters. Search: '" + searchText + "', Filter: '" + statusFilterValue + "'. Master list size: " + masterList.size());

        Stream<Consultation> consultationStream = masterList.stream();

        if (statusFilterValue != null && !statusFilterValue.equals("All")) {
            consultationStream = consultationStream.filter(consultation -> {
                if (statusFilterValue.equals("My Consultations")) {
                    return currentUser.getRole() == User.UserRole.PSYCHIATRIST && consultation.getPsychiatrist() != null && consultation.getPsychiatrist().getId() == currentUser.getId();
                } else if (statusFilterValue.equals("Upcoming")) {
                    return consultation.getConsultationTime() != null && consultation.getConsultationTime().isAfter(LocalDateTime.now());
                } else if (statusFilterValue.equals("Past")) {
                    return consultation.getConsultationTime() != null && consultation.getConsultationTime().isBefore(LocalDateTime.now());
                }
                else {
                    return consultation.getStatus() != null && consultation.getStatus().getDisplayValue().equals(statusFilterValue);
                }
            });
        }

        if (!searchText.isEmpty()) {
            consultationStream = consultationStream.filter(consultation ->
                    (consultation.getPurpose() != null && consultation.getPurpose().toLowerCase().contains(searchText)) ||
                            (consultation.getNotes() != null && consultation.getNotes().toLowerCase().contains(searchText)) ||
                            (consultation.getPatient() != null && consultation.getPatient().getFullName().toLowerCase().contains(searchText)) ||
                            (consultation.getPsychiatrist() != null && consultation.getPsychiatrist().getFullName().toLowerCase().contains(searchText))
            );
        }

        List<Consultation> filteredListResult = consultationStream.collect(Collectors.toList());
        System.out.println("ConsultationsListController (Card View): Found " + filteredListResult.size() + " consultations after filtering.");
        displayConsultationsAsCards(filteredListResult); // This method populates the FlowPane

        if (selectedConsultation != null && filteredListResult.stream().noneMatch(c -> c.getId() == selectedConsultation.getId())) {
            clearSelection();
        }
        if (selectedConsultation != null) {
            if (consultationContainer != null) { // Check if FXML element is initialized
                consultationContainer.getChildren().stream()
                        .filter(node -> node.getUserData() instanceof Consultation && ((Consultation)node.getUserData()).getId() == selectedConsultation.getId())
                        .findFirst()
                        .ifPresent(node -> handleCardSelection(node, selectedConsultation));
            }
        }
    }

    private void displayConsultationsAsCards(List<Consultation> consultationsToDisplay) {
        if (consultationContainer == null) { // Check if FXML element is initialized
            System.out.println("ConsultationsListController (Card View): consultationContainer is null, skipping display."); // <-- Debug Print
            return; // Exit if not in Admin/Psy view
        }
        System.out.println("ConsultationsListController (Card View): Displaying " + consultationsToDisplay.size() + " consultations as cards.");
        consultationContainer.getChildren().clear();
        if (consultationsToDisplay.isEmpty()) {
            Label emptyLabel = new Label("No consultations found matching your criteria.");
            emptyLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
            consultationContainer.getChildren().add(emptyLabel);
            System.out.println("ConsultationsListController (Card View): Added 'No consultations found' label.");
        } else {
            for (Consultation consultation : consultationsToDisplay) {
                Node consultationCard = createConsultationCard(consultation);
                consultationContainer.getChildren().add(consultationCard);
            }
            System.out.println("ConsultationsListController (Card View): Finished adding cards to FlowPane.");
        }
        String msg = consultationsToDisplay.size() + " consultations shown.";
        setStatus(msg, Color.DARKSLATEGRAY); // statusLabel check is inside setStatus
        // Selection re-application is handled in filterAndDisplayConsultations
    }

    private Node createConsultationCard(Consultation consultation) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("consultation-card");
        card.setUserData(consultation);
        card.setPrefWidth(280);
        card.setMinWidth(250);
        card.setAlignment(Pos.TOP_LEFT);

        Label dateTimeLabel = new Label(consultation.getConsultationTime() != null ? consultation.getConsultationTime().format(CARD_DATE_FORMATTER) : "Date N/A");
        dateTimeLabel.getStyleClass().add("card-title");
        dateTimeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));

        Label metaLabel = new Label("Patient: " + consultation.getPatientName() + " | Psy: " + consultation.getPsychiatristName());
        metaLabel.getStyleClass().add("card-meta");

        Label purposeLabel = new Label("Purpose: " + (consultation.getPurpose() != null && consultation.getPurpose().length() > 50 ? consultation.getPurpose().substring(0, 50) + "..." : consultation.getPurpose()));
        purposeLabel.setWrapText(true);
        purposeLabel.getStyleClass().add("card-meta");


        Label statusDisplayLabel = new Label(consultation.getStatusDisplayValue());
        if (consultation.getStatus() != null) {
            switch (consultation.getStatus()) {
                case SCHEDULED: statusDisplayLabel.getStyleClass().setAll("card-status-info"); break;
                case COMPLETED: statusDisplayLabel.getStyleClass().setAll("card-status-success"); break;
                case CANCELLED: statusDisplayLabel.getStyleClass().setAll("card-status-danger"); break;
                case RESCHEDULED: statusDisplayLabel.getStyleClass().setAll("card-status-warning"); break;
                default: statusDisplayLabel.getStyleClass().setAll("card-status-draft"); break;
            }
        } else {
            statusDisplayLabel.getStyleClass().setAll("card-status-draft");
        }
        statusDisplayLabel.setPrefWidth(-1);


        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.getStyleClass().add("card-button-box");

        Button viewBtn = new Button("View");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");

        viewBtn.getStyleClass().addAll("button-outline", "button-sm");
        editBtn.getStyleClass().addAll("button-secondary", "button-sm");
        deleteBtn.getStyleClass().addAll("button-danger", "button-sm");

        viewBtn.setOnAction(event -> handleViewConsultation(consultation));
        editBtn.setOnAction(event -> handleEditConsultation(consultation));
        deleteBtn.setOnAction(event -> handleDeleteConsultation(consultation));

        boolean canModify = currentUser.getRole() == User.UserRole.ADMIN;
        if (currentUser.getRole() == User.UserRole.PSYCHIATRIST && consultation.getPsychiatrist() != null && consultation.getPsychiatrist().getId() == currentUser.getId()) {
            canModify = true;
        }
        // Patients cannot modify (implicitly false)

        buttonBox.getChildren().add(viewBtn); // View is available for all roles? Decide this.
        if (canModify) {
            buttonBox.getChildren().addAll(editBtn, deleteBtn);
        }

        card.getChildren().addAll(dateTimeLabel, metaLabel, purposeLabel, statusDisplayLabel, spacer, buttonBox);

        card.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Node target = (Node) event.getTarget();
            boolean isButtonClick = false;
            while(target != null && target != card) {
                if (target instanceof Button) {
                    isButtonClick = true;
                    break;
                }
                target = target.getParent();
            }

            if (!isButtonClick) {
                handleCardSelection(card, consultation);
            }
        });

        return card;
    }

    private void handleCardSelection(Node cardNode, Consultation consultation) {
        clearSelectionStyle();
        cardNode.getStyleClass().add("consultation-card-selected");
        selectedConsultationCard = cardNode;
        selectedConsultation = consultation;
        System.out.println("ConsultationsListController (Card View): Selected Consultation ID: " + consultation.getId());
    }

    private void clearSelectionStyle() {
        if (selectedConsultationCard != null) {
            selectedConsultationCard.getStyleClass().remove("consultation-card-selected");
        }
    }

    private void clearSelection() {
        clearSelectionStyle();
        selectedConsultation = null;
        System.out.println("ConsultationsListController (Card View): Selection cleared.");
    }

    private void handleViewConsultation(Consultation consultation) {
        // This view logic is ONLY for Admin/Psychiatrist now
        if (consultation == null) return;
        System.out.println("ConsultationsListController (Card View): Viewing Consultation ID: " + consultation.getId());
        clearSelection();

        try {
            // Need a node from the Admin/Psy view to get the owner window.
            // Use the consultationContainer (FlowPane) as an example node.
            if (consultationContainer == null) { // Safeguard if called unexpectedly
                System.err.println("ConsultationsListController (Card View): Cannot open detail dialog, consultationContainer is null.");
                // Maybe show an error message to the user?
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/consultationDetailView.fxml"));
            Parent detailRoot = loader.load();

            ConsultationDetailController detailController = loader.getController();
            if (detailController != null) {
                detailController.setConsultation(consultation);
            } else {
                System.err.println("ConsultationsListController (Card View): Error: ConsultationDetailController not found after loading FXML.");
                return;
            }

            Stage detailStage = new Stage();
            detailStage.setTitle("Consultation Details");
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.initOwner(consultationContainer.getScene().getWindow()); // Use a node from *this* view
            detailStage.setScene(new Scene(detailRoot));
            detailStage.setMinWidth(600);
            detailStage.setMinHeight(500);

            detailStage.showAndWait();

        } catch (IOException e) {
            System.err.println("ConsultationsListController (Card View): Error loading Consultation Detail View FXML: " + e.getMessage());
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Loading Error");
            errorAlert.setHeaderText("Could not open consultation details.");
            errorAlert.setContentText("Failed to load the required layout resources. Check logs for details.");
            errorAlert.showAndWait();
        } catch (Exception e) {
            System.err.println("ConsultationsListController (Card View): An unexpected error occurred while showing consultation detail: " + e.getMessage());
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Could not display consultation details.");
            errorAlert.setContentText("An unexpected error occurred: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }


    @FXML
    private void handleScheduleNew(ActionEvent event) {
        System.out.println("ConsultationsListController (Card View): Scheduling new consultation.");
        WellTechApplication.setCurrentConsultation(null);
        WellTechApplication.loadFXML("consultationEdit");
    }

    private void handleEditConsultation(Consultation consultation) {
        System.out.println("ConsultationsListController (Card View): Editing Consultation ID: " + consultation.getId());
        clearSelection();
        WellTechApplication.setCurrentConsultation(consultation);
        WellTechApplication.loadFXML("consultationEdit");
    }

    private void handleDeleteConsultation(Consultation consultation) {
        System.out.println("ConsultationsListController (Card View): Deleting Consultation ID: " + consultation.getId());
        clearSelection();

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Consultation");
        confirmAlert.setContentText("Are you sure you want to permanently delete the consultation with Patient '" + consultation.getPatientName() + "' on " + (consultation.getConsultationTime() != null ? consultation.getConsultationTime().format(CARD_DATE_FORMATTER) : "N/A Date") + "?");
        ButtonType okButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(okButton, cancelButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == okButton) {
            setStatus("Deleting consultation...", Color.ORANGE);
            Runnable deleteTask = () -> {
                boolean deleted = consultationDAO.deleteConsultation(consultation.getId());
                Platform.runLater(() -> {
                    if (deleted) {
                        setStatus("Consultation deleted.", Color.RED);
                        masterList.remove(consultation);
                        filterAndDisplayConsultations();
                    } else {
                        setStatus("Failed to delete consultation.", Color.RED);
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Deletion Failed");
                        errorAlert.setContentText("Could not delete the consultation from the database.");
                        errorAlert.showAndWait();
                    }
                });
            };
            new Thread(deleteTask).start();
        }
    }

    private void setStatus(String message, Color color) {
        Platform.runLater(() -> {
            // Check if statusLabel is initialized before using it (won't be for Patient)
            if (statusLabel != null) {
                statusLabel.setText(message);
                statusLabel.setTextFill(color);
                statusLabel.getStyleClass().removeAll("success-message", "error-message", "info-message");
                if (color == Color.GREEN) statusLabel.getStyleClass().add("success-message");
                else if (color == Color.RED || color == Color.ORANGE) statusLabel.getStyleClass().add("error-message");
                else statusLabel.getStyleClass().add("info-message");
            } else {
                System.out.println("ConsultationsListController: Status update (statusLabel is null): " + message); // Log for Patient view
            }
        });
    }

    // === Navigation Handlers ===
    @FXML private void handleLogout(ActionEvent event) { LoginController.logout(); }

    @FXML private void navigateToDashboard(ActionEvent event) {
        System.out.println("ConsultationsListController: Navigating to Dashboard."); // <-- Debug Print
        if (currentUser != null) {
            switch (currentUser.getRole()) {
                case ADMIN: WellTechApplication.loadFXML("adminDashboard"); break;
                case PSYCHIATRIST: WellTechApplication.loadFXML("psychiatristDashboard"); break;
                case PATIENT: WellTechApplication.loadFXML("patientDashboard"); break;
                default: WellTechApplication.loadFXML("login");
            }
        } else {
            WellTechApplication.loadFXML("login");
        }
    }

    @FXML private void navigateToArticles(ActionEvent event) {
        System.out.println("ConsultationsListController: Navigating to Articles."); // <-- Debug Print
        WellTechApplication.loadFXML("articlesList");
    }

    // This controller is for the Consultation List, clicking Consultations reloads THIS view or dispatches again
    @FXML private void navigateToConsultations(ActionEvent event) {
        System.out.println("ConsultationsListController: Navigating to Consultations (Dispatching again).");
        // When clicking the button, we dispatch again based on role
        if (currentUser != null) {
            if (currentUser.getRole() == UserRole.PATIENT) {
                WellTechApplication.loadFXML("patientConsultationsCalendarView");
            } else {
                WellTechApplication.loadFXML("consultationsList"); // Reloads the card view
            }
        } else {
            WellTechApplication.loadFXML("login");
        }
    }
    @FXML // <-- Make sure this annotation is present
    private void handleExportToExcel(ActionEvent event) { // <-- Make sure the name and signature match
        // Get the currently filtered list of consultations
        // Note: The FlowPane doesn't hold the list, the filter process generates it.
        // Re-run the filter logic to get the current displayed list.

        // Corrected: Declare consultationsToExport as a Stream
        Stream<Consultation> consultationsToExportStream = masterList.stream(); // Start with master list as a Stream

        // --- Apply the same filters as display ---
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String statusFilterValue = statusFilterComboBox.getValue();

        if (statusFilterValue != null && !statusFilterValue.equals("All")) {
            // Apply filter on the Stream
            consultationsToExportStream = consultationsToExportStream.filter(consultation -> { // Call filter() on the Stream
                if (statusFilterValue.equals("My Consultations")) {
                    return (currentUser.getRole() == User.UserRole.PSYCHIATRIST && consultation.getPsychiatrist() != null && consultation.getPsychiatrist().getId() == currentUser.getId()) ||
                            (currentUser.getRole() == User.UserRole.PATIENT && consultation.getPatient() != null && consultation.getPatient().getId() == currentUser.getId());
                } else if (statusFilterValue.equals("Upcoming")) {
                    return consultation.getConsultationTime() != null && consultation.getConsultationTime().isAfter(LocalDateTime.now());
                } else if (statusFilterValue.equals("Past")) {
                    return consultation.getConsultationTime() != null && consultation.getConsultationTime().isBefore(LocalDateTime.now());
                }
                else {
                    return consultation.getStatus() != null && consultation.getStatus().getDisplayValue().equals(statusFilterValue);
                }
            });
        }

        if (!searchText.isEmpty()) {
            // Apply filter on the Stream
            consultationsToExportStream = consultationsToExportStream.filter(consultation -> // Call filter() on the Stream
                    (consultation.getPurpose() != null && consultation.getPurpose().toLowerCase().contains(searchText)) ||
                            (consultation.getNotes() != null && consultation.getNotes().toLowerCase().contains(searchText)) ||
                            (consultation.getPatient() != null && consultation.getPatient().getFullName().toLowerCase().contains(searchText)) ||
                            (consultation.getPsychiatrist() != null && consultation.getPsychiatrist().getFullName().toLowerCase().contains(searchText))
            );
        }
        // --- End Filters ---

        // Collect the Stream back into a List at the end
        List<Consultation> finalFilteredList = consultationsToExportStream.collect(Collectors.toList()); // Collect the Stream into a List


        if (finalFilteredList.isEmpty()) {
            setStatus("No consultations to export based on current filters.", Color.ORANGE);
            return;
        }

        // Show FileChooser dialog to get save location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Consultations to Excel");
        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        // Set default file name
        fileChooser.setInitialFileName("ConsultationsExport_" + java.time.LocalDate.now() + ".xlsx");

        // Show save dialog
        File file = fileChooser.showSaveDialog(consultationContainer.getScene().getWindow()); // Get window from any node

        if (file != null) {
            setStatus("Exporting to Excel...", Color.BLUE);
            exportButton.setDisable(true); // Disable button during export

            // Perform export in a background thread
            Runnable exportTask = () -> {
                try {
                    ExcelExporter.exportConsultationsToExcel(finalFilteredList, file.getAbsolutePath()); // Pass the final List
                    Platform.runLater(() -> {
                        setStatus("Export successful: " + file.getName(), Color.GREEN);
                        exportButton.setDisable(false); // Re-enable button
                    });
                } catch (IOException e) {
                    System.err.println("Error exporting consultations to Excel: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        setStatus("Export failed: " + e.getMessage(), Color.RED);
                        exportButton.setDisable(false); // Re-enable button
                        // Optionally show a more detailed error dialog
                    });
                } catch (Exception e) {
                    System.err.println("An unexpected error occurred during export: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        setStatus("Export failed due to unexpected error.", Color.RED);
                        exportButton.setDisable(false);
                    });
                }
            };
            new Thread(exportTask).start(); // Run the task
        } else {
            // User cancelled the dialog
            setStatus("Export cancelled.", Color.DARKSLATEGRAY);
        }
    }
    @FXML private void navigateToProfile(ActionEvent event) {
        System.out.println("ConsultationsListController: Navigating to Profile.");
        WellTechApplication.loadFXML("profile");
    }

    // ... other navigation methods ...
}