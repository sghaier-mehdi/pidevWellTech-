package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.ConsultationDAO;
import com.welltech.model.Consultation;
import com.welltech.model.User;
import com.welltech.model.User.UserRole;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class PatientConsultationsCalendarController implements Initializable {

    // === FXML Injections for Header/Sidebar ===
    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    @FXML private Button dashboardButton;
    @FXML private Button articlesButton;
    @FXML private Button consultationsButton; // Appointments List button
    @FXML private Button chatbotButton; // Add Chatbot button injection if it exists in FXML
    @FXML private Button calendarButton; // This view's button
    @FXML private Button profileButton;
    // Add injections for other sidebar buttons if applicable
    // @FXML private Button historyButton; // If exists
    // @FXML private Button messagesButton; // If exists


    @FXML private Text pageTitle;
    @FXML private Button prevMonthButton;
    @FXML private Label currentMonthLabel; // Label showing Month Year
    @FXML private Button nextMonthButton;
    @FXML private GridPane dayOfWeekGrid; // Grid for Mon, Tue... headers
    @FXML private GridPane calendarGrid; // Main grid for day cells
    @FXML private Label statusLabel;

    private ConsultationDAO consultationDAO;
    private User currentUser;
    private ObservableList<Consultation> patientConsultations = FXCollections.observableArrayList();
    private YearMonth currentMonth;

    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        consultationDAO = new ConsultationDAO();
        currentUser = LoginController.getCurrentUser();

        if (currentUser == null || currentUser.getRole() != UserRole.PATIENT) {
            System.err.println("PatientConsultationsCalendarController: No patient user is logged in. Redirecting."); // Debug
            WellTechApplication.loadFXML("login");
            return;
        }
        System.out.println("PatientConsultationsCalendarController: Initializing for patient: " + currentUser.getUsername()); // Debug


        if (userNameLabel != null) {
            userNameLabel.setText("Welcome, " + currentUser.getFullName());
        }

        // === Sidebar Button Styling ===
        if (dashboardButton != null) removeActiveStyle(dashboardButton);
        if (articlesButton != null) removeActiveStyle(articlesButton);
        if (consultationsButton != null) removeActiveStyle(consultationsButton); // Refers to Appointments List button
        if (chatbotButton != null) removeActiveStyle(chatbotButton); // Refers to Chatbot button
        if (calendarButton != null) removeActiveStyle(calendarButton); // Refers to THIS view's button
        if (profileButton != null) removeActiveStyle(profileButton);
        // ... other buttons ...


        if (calendarButton != null) { // Highlight THIS view's button
            calendarButton.getStyleClass().add("active");
        }
        // ==============================


        setupDayOfWeekHeaders(); // Setup headers *before* populating the grid
        currentMonth = YearMonth.now();
        loadAllPatientConsultations(); // Load all data initially
        // updateCalendarDisplay(); // <<< REMOVED: This is now called AFTER data is loaded
    }

    private void removeActiveStyle(Button button) {
        if (button != null) {
            button.getStyleClass().remove("active");
        }
    }

    // --- Setup Day of Week Headers ---
    private void setupDayOfWeekHeaders() {
        if (dayOfWeekGrid == null) { System.err.println("PatientConsultationsCalendarController: dayOfWeekGrid is null!"); return; }
        dayOfWeekGrid.getChildren().clear();
        dayOfWeekGrid.setAlignment(Pos.CENTER);
        dayOfWeekGrid.getColumnConstraints().clear();

        DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY; // Start week on Monday
        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / 7); // Each column takes 1/7th width
            colConst.setHgrow(Priority.ALWAYS);
            colConst.setFillWidth(true);
            dayOfWeekGrid.getColumnConstraints().add(colConst);

            DayOfWeek day = firstDayOfWeek.plus(i); // Get the current day of week
            Label dayLabel = new Label(day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.getStyleClass().add("calendar-day-header"); // Base header style

            // === Add Weekend Header Style ===
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                dayLabel.getStyleClass().add("weekend-header"); // Add weekend specific style
            }
            // ==============================

            dayOfWeekGrid.add(dayLabel, i, 0); // Add to row 0, column i
            GridPane.setHalignment(dayLabel, javafx.geometry.HPos.CENTER);
        }
        dayOfWeekGrid.getStyleClass().add("day-of-week-headers"); // Add container style class (ensure this is in your FXML HBox/GridPane)
    }

    private void loadAllPatientConsultations() {
        setStatus("Loading appointments...", Color.BLUE);
        Runnable loadTask = () -> {
            try {
                System.out.println("PatientConsultationsCalendarController: Loading all consultations for patient ID: " + currentUser.getId()); // Debug
                List<Consultation> consultationsFromDB = consultationDAO.getConsultationsByPatient(currentUser.getId());
                System.out.println("PatientConsultationsCalendarController: Found " + consultationsFromDB.size() + " consultations in DB."); // Debug

                Platform.runLater(() -> {
                    patientConsultations.setAll(consultationsFromDB);
                    System.out.println("PatientConsultationsCalendarController: patientConsultations list updated with " + patientConsultations.size() + " items."); // Debug
                    updateCalendarDisplay(); // Now display the calendar using the loaded data
                    String msg = patientConsultations.isEmpty() ? "No appointments found." : patientConsultations.size() + " appointments loaded.";
                    setStatus(msg, Color.DARKSLATEGRAY);
                });
            } catch (Exception e) {
                System.err.println("PatientConsultationsCalendarController: Failed to load appointments: " + e.getMessage()); // Debug
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Failed to load appointments.", Color.RED));
            }
        };
        new Thread(loadTask).start();
        // Removed updateCalendarDisplay() from here - it's called in Platform.runLater
    }

    // --- Update Calendar Display ---
    private void updateCalendarDisplay() {
        if (currentMonthLabel == null) { System.err.println("PatientConsultationsCalendarController: currentMonthLabel is null!"); return; }
        currentMonthLabel.setText(currentMonth.format(MONTH_YEAR_FORMATTER));
        populateCalendarGrid(currentMonth); // Populate the grid with days and appointments
    }

    // --- Populate Calendar Grid ---
    private void populateCalendarGrid(YearMonth yearMonth) {
        if (calendarGrid == null) { System.err.println("PatientConsultationsCalendarController: calendarGrid is null!"); return; }
        calendarGrid.getChildren().clear(); // Clear existing day cells
        calendarGrid.setAlignment(Pos.TOP_LEFT);
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear(); // Clear row constraints before adding new ones

        // Column constraints (same as day headers)
        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / 7); // Each column takes 1/7th width
            colConst.setHgrow(Priority.ALWAYS);
            colConst.setFillWidth(true);
            calendarGrid.getColumnConstraints().add(colConst);
        }
        // Row constraints (allow rows to grow vertically)
        for (int i = 0; i < 6; i++) { // Standard 6 weeks view
            RowConstraints rowConst = new RowConstraints();
            rowConst.setVgrow(Priority.ALWAYS);
            rowConst.setFillHeight(true);
            calendarGrid.getRowConstraints().add(rowConst);
        }

        // Calculate the date of the first day to display in the grid (might be in the previous month)
        LocalDate firstOfMonth = yearMonth.atDay(1);
        // Calculate the day of the week (0=Mon, 6=Sun) for the 1st of the month
        int dayOfWeekOfFirst = firstOfMonth.getDayOfWeek().getValue() - 1; // Adjust to 0-based index for Monday=0
        // Calculate the date of the first day cell in the grid (the Monday of the week the 1st falls into)
        LocalDate currentDate = firstOfMonth.minusDays(dayOfWeekOfFirst);


        System.out.println("PatientConsultationsCalendarController: Populating grid starting from: " + currentDate + " for month: " + yearMonth);

        for (int row = 0; row < 6; row++) { // 6 rows (weeks)
            for (int col = 0; col < 7; col++) { // 7 columns (days)
                VBox dayCell = createDayCell(currentDate, yearMonth);
                calendarGrid.add(dayCell, col, row); // Add cell to grid at column, row
                // Ensure the cell grows within its grid pane constraints
                GridPane.setVgrow(dayCell, Priority.ALWAYS);
                GridPane.setHgrow(dayCell, Priority.ALWAYS);

                currentDate = currentDate.plusDays(1); // Move to the next day
            }
        }
        System.out.println("PatientConsultationsCalendarController: Calendar grid populated.");
    }


    // --- Create Day Cell VBox ---
    // Creates the VBox for a single day in the calendar grid, adds day number and appointments
    private VBox createDayCell(LocalDate date, YearMonth monthBeingDisplayed) {
        // Debug Print: Called for each cell (42 times)
        // System.out.println("PatientConsultationsCalendarController: Creating cell for date: " + date + " (Month Being Displayed: " + monthBeingDisplayed + ")");

        VBox cell = new VBox();
        cell.getStyleClass().add("day-cell"); // Base style for the day cell VBox
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Allow cell to grow
        cell.setPadding(new Insets(5));
        cell.setSpacing(3); // Space between day number and entries

        // === Add Weekend Cell Style ===
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            cell.getStyleClass().add("calendar-weekend"); // Add weekend specific style
        }
        // ==============================

        // Add click handler for the cell itself (optional - e.g., to add a new appointment)
        // cell.setOnMouseClicked(event -> handleDayClick(date)); // Implement handleDayClick if needed

        // --- Add the day number label ---
        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        dayLabel.getStyleClass().add("day-label"); // Style for the number
        dayLabel.setAlignment(Pos.TOP_LEFT); // Align day number top-left within its own label

        // Add styles for days outside the current month and today
        if (!YearMonth.from(date).equals(monthBeingDisplayed)) {
            cell.getStyleClass().add("calendar-other-month"); // CSS for other month cell
        } else if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("calendar-today"); // CSS for today's cell
        }
        cell.getChildren().add(dayLabel); // Add the day number label first (index 0)


        // --- Add appointment entries to the cell ---
        // Filter the list of ALL patient consultations for appointments on 'date'
        // Debug Print: Check data and filter logic
        // System.out.println("PatientConsultationsCalendarController: Processing appointments for date: " + date + ". Total consultations loaded: " + patientConsultations.size());
        patientConsultations.stream()
                .filter(c -> {
                    // Check if appointment date matches the cell date
                    boolean matches = c.getConsultationTime() != null && c.getConsultationTime().toLocalDate().equals(date);
                    // Debug Print (Very verbose):
                    // System.out.println("  - Checking appointment " + c.getId() + " on " + (c.getConsultationTime() != null ? c.getConsultationTime().toLocalDate() : "null") + " against " + date + ": " + matches);
                    return matches;
                })
                .sorted((c1, c2) -> c1.getConsultationTime().compareTo(c2.getConsultationTime())) // Sort by time
                .forEach(c -> {
                    // Debug Print: If this print appears, appointments ARE being found for the cell
                    // System.out.println("PatientConsultationsCalendarController: Found appointment " + c.getId() + " for date " + date + ". Adding entry label.");
                    // Create a Label for each appointment entry
                    Label entryLabel = new Label(
                            c.getConsultationTime().toLocalTime().format(TIME_FORMATTER) +
                                    " - " +
                                    c.getPsychiatristName() // Patient sees Psychiatrist name
                    );
                    entryLabel.getStyleClass().add("calendar-appointment-entry"); // CSS style for appointment entry
                    entryLabel.setFont(Font.font("System", 10));
                    entryLabel.setWrapText(true); // Allow text wrapping in the entry
                    entryLabel.setMaxWidth(Double.MAX_VALUE); // Allow entry label to take available width

                    entryLabel.setOnMouseClicked(event -> handleViewConsultation(c)); // Add click handler

                    cell.getChildren().add(entryLabel); // Add the appointment entry label to the day cell
                });

        // Debug Print: Total children should be 1 (dayLabel) + number of appointments
        // System.out.println("PatientConsultationsCalendarController: Finished creating cell for " + date + ". Total children in cell: " + cell.getChildren().size());

        return cell;
    }

    @FXML private void handlePrevMonth(ActionEvent event) {
        currentMonth = currentMonth.minusMonths(1);
        updateCalendarDisplay(); // Update display for the new month (uses existing data)
    }

    @FXML private void handleNextMonth(ActionEvent event) {
        currentMonth = currentMonth.plusMonths(1);
        updateCalendarDisplay(); // Update display for the new month (uses existing data)
    }

    private void handleViewConsultation(Consultation consultation) {
        if (consultation == null) return;
        System.out.println("PatientConsultationsCalendarController: *** Clicked on appointment ID: " + consultation.getId() + ". Attempting to view details. ***"); // <-- Debug Print

        try {
            System.out.println("PatientConsultationsCalendarController: Loading consultationDetailView.fxml..."); // <-- Debug Print
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/consultationDetailView.fxml"));
            Parent detailRoot = loader.load(); // Load the FXML
            System.out.println("PatientConsultationsCalendarController: FXML loaded."); // <-- Debug Print


            ConsultationDetailController detailController = loader.getController();
            if (detailController != null) {
                System.out.println("PatientConsultationsCalendarController: Detail controller obtained. Setting consultation data."); // <-- Debug Print
                detailController.setConsultation(consultation); // Pass the consultation data
            } else {
                System.err.println("PatientConsultationsCalendarController: Error: ConsultationDetailController not found after loading FXML."); // <-- Ensure this print is here
                // Optionally show an error to the user
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Internal Error");
                errorAlert.setHeaderText("Could not initialize details view.");
                errorAlert.setContentText("The controller for the detail view was not found.");
                errorAlert.showAndWait();
                return;
            }

            // Create and configure the new stage (dialog)
            Stage detailStage = new Stage();
            detailStage.setTitle("Consultation Details");
            detailStage.initModality(Modality.APPLICATION_MODAL); // Block interactions with main app
            // Get the window from any node that is part of this controller's scene
            // Use a node guaranteed to be in the scene, like calendarGrid or statusLabel
            if (calendarGrid != null && calendarGrid.getScene() != null) {
                detailStage.initOwner(calendarGrid.getScene().getWindow()); // Set parent window from current view
                System.out.println("PatientConsultationsCalendarController: Detail stage owner set from calendar grid scene."); // <-- Debug print
            } else {
                // Fallback if calendarGrid is not yet in a scene (less likely for event handler)
                detailStage.initOwner(WellTechApplication.getPrimaryStage()); // Use the primary stage
                System.out.println("PatientConsultationsCalendarController: Detail stage owner set from primary stage."); // <-- Debug print
            }

            detailStage.setScene(new Scene(detailRoot));
            detailStage.setMinWidth(600);
            detailStage.setMinHeight(500);

            System.out.println("PatientConsultationsCalendarController: Showing detail dialog..."); // <-- Add this print
            detailStage.showAndWait(); // Show the dialog and wait for it to be closed
            System.out.println("PatientConsultationsCalendarController: Detail dialog closed."); // <-- Add this print


        } catch (IOException e) {
            System.err.println("PatientConsultationsCalendarController: Error loading Consultation Detail View FXML: " + e.getMessage());
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Loading Error");
            errorAlert.setHeaderText("Could not open consultation details.");
            errorAlert.setContentText("Failed to load the required layout resources. Check logs for details.");
            errorAlert.showAndWait();
        } catch (Exception e) { // Catch any other exceptions during dialog setup/showing
            System.err.println("PatientConsultationsCalendarController: An unexpected error occurred while showing consultation detail dialog: " + e.getMessage());
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Could not display consultation details.");
            errorAlert.setContentText("An unexpected error occurred: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
    private void setStatus(String message, Color color) { /* ... */ } // Keep existing status logic


    // === Navigation Handlers ===
    @FXML private void handleLogout(ActionEvent event) { System.out.println("PatientConsultationsCalendarController: Logging out."); LoginController.logout(); }

    @FXML private void navigateToDashboard(ActionEvent event) { System.out.println("PatientConsultationsCalendarController: Navigating to Dashboard."); if (currentUser != null) { switch (currentUser.getRole()) { case ADMIN: WellTechApplication.loadFXML("adminDashboard"); break; case PSYCHIATRIST: WellTechApplication.loadFXML("psychiatristDashboard"); break; case PATIENT: WellTechApplication.loadFXML("patientDashboard"); break; default: WellTechApplication.loadFXML("login"); } } else { WellTechApplication.loadFXML("login"); }}

    @FXML private void navigateToArticles(ActionEvent event) { System.out.println("PatientConsultationsCalendarController: Navigating to Articles."); WellTechApplication.loadFXML("articlesList"); }

    // === Handler for Appointments LIST button ===
    // This method is required by the Appointments button in patientConsultationsCalendarView.fxml
    @FXML private void navigateToConsultationsList(ActionEvent event) { // Corrected method name
        System.out.println("PatientConsultationsCalendarController: Navigating to Appointments List.");
        WellTechApplication.loadFXML("consultationsList"); // Load the Consultations LIST view
    }
    // ==========================================


    // This controller is for the Calendar, so clicking Calendar would reload THIS view
    @FXML private void navigateToCalendar(ActionEvent event) {
        System.out.println("PatientConsultationsCalendarController: Already on Calendar page (reloading).");
        WellTechApplication.loadFXML("patientConsultationsCalendarView"); // Reloads the current view
    }

    @FXML private void navigateToProfile(ActionEvent event) { System.out.println("PatientConsultationsCalendarController: Navigating to Profile."); WellTechApplication.loadFXML("profile"); }

    // Add other navigation methods if applicable
    // @FXML private void navigateToHistory(ActionEvent event) { ... }
    // @FXML private void navigateToMessages(ActionEvent event) { ... }
    // ==================================================================
}