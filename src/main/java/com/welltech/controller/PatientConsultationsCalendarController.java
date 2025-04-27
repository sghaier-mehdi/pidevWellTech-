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

    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    @FXML private Button dashboardButton;
    @FXML private Button articlesButton;
    @FXML private Button consultationsButton;
    @FXML private Button profileButton;

    @FXML private Text pageTitle;
    @FXML private Button prevMonthButton;
    @FXML private Label currentMonthLabel;
    @FXML private Button nextMonthButton;
    @FXML private GridPane dayOfWeekGrid;
    @FXML private GridPane calendarGrid;
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
            WellTechApplication.loadFXML("login");
            return;
        }

        if (userNameLabel != null) {
            userNameLabel.setText("Welcome, " + currentUser.getFullName());
        }

        removeActiveStyle(dashboardButton);
        removeActiveStyle(articlesButton);
        removeActiveStyle(consultationsButton);
        removeActiveStyle(profileButton);

        if (consultationsButton != null) {
            consultationsButton.getStyleClass().add("active");
        }

        setupDayOfWeekHeaders();
        currentMonth = YearMonth.now();
        loadAllPatientConsultations();
    }

    private void removeActiveStyle(Button button) {
        if (button != null) {
            button.getStyleClass().remove("active");
        }
    }

    private void setupDayOfWeekHeaders() {
        dayOfWeekGrid.getChildren().clear();
        dayOfWeekGrid.setAlignment(Pos.CENTER);
        dayOfWeekGrid.getColumnConstraints().clear();
        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / 7);
            colConst.setHgrow(Priority.ALWAYS);
            colConst.setFillWidth(true);
            dayOfWeekGrid.getColumnConstraints().add(colConst);
        }

        DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;
        for (int i = 0; i < 7; i++) {
            DayOfWeek day = firstDayOfWeek.plus(i);
            Label dayLabel = new Label(day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayOfWeekGrid.add(dayLabel, i, 0);
            GridPane.setHalignment(dayLabel, javafx.geometry.HPos.CENTER);
        }
    }

    private void loadAllPatientConsultations() {
        setStatus("Loading appointments...", Color.BLUE);
        Runnable loadTask = () -> {
            try {
                List<Consultation> consultationsFromDB = consultationDAO.getConsultationsByPatient(currentUser.getId());
                Platform.runLater(() -> {
                    patientConsultations.setAll(consultationsFromDB);
                    updateCalendarDisplay();
                    String msg = patientConsultations.isEmpty() ? "No appointments found." : patientConsultations.size() + " appointments loaded.";
                    setStatus(msg, Color.DARKSLATEGRAY);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Failed to load appointments.", Color.RED));
            }
        };
        new Thread(loadTask).start();
    }

    private void updateCalendarDisplay() {
        currentMonthLabel.setText(currentMonth.format(MONTH_YEAR_FORMATTER));
        populateCalendarGrid(currentMonth);
    }

    private void populateCalendarGrid(YearMonth yearMonth) {
        calendarGrid.getChildren().clear();
        calendarGrid.setAlignment(Pos.TOP_LEFT);
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / 7);
            colConst.setHgrow(Priority.ALWAYS);
            colConst.setFillWidth(true);
            calendarGrid.getColumnConstraints().add(colConst);
        }
        for (int i = 0; i < 6; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setVgrow(Priority.ALWAYS);
            rowConst.setFillHeight(true);
            calendarGrid.getRowConstraints().add(rowConst);
        }

        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeekOfFirst = firstOfMonth.getDayOfWeek().getValue() - 1;
        LocalDate currentDate = firstOfMonth.minusDays(dayOfWeekOfFirst);

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                VBox dayCell = createDayCell(currentDate, yearMonth);
                calendarGrid.add(dayCell, col, row);
                dayCell.setPadding(new Insets(5));
                dayCell.setSpacing(3);
                currentDate = currentDate.plusDays(1);
            }
        }
    }

    private VBox createDayCell(LocalDate date, YearMonth monthBeingDisplayed) {
        VBox cell = new VBox();
        cell.getStyleClass().add("day-cell");
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        dayLabel.getStyleClass().add("day-label");

        if (!YearMonth.from(date).equals(monthBeingDisplayed)) {
            dayLabel.setTextFill(Color.GRAY);
        } else if (date.equals(LocalDate.now())) {
            dayLabel.getStyleClass().add("current-day-label");
        }
        cell.getChildren().add(dayLabel);

        patientConsultations.stream()
                .filter(c -> c.getConsultationTime() != null && c.getConsultationTime().toLocalDate().equals(date))
                .sorted((c1, c2) -> c1.getConsultationTime().compareTo(c2.getConsultationTime()))
                .forEach(c -> {
                    Label entryLabel = new Label(c.getConsultationTime().toLocalTime().format(TIME_FORMATTER) + " - " + c.getPsychiatristName());
                    entryLabel.getStyleClass().add("consultation-entry");
                    entryLabel.setFont(Font.font("System", 10));
                    entryLabel.setWrapText(true);
                    entryLabel.setOnMouseClicked(event -> handleViewConsultation(c));
                    cell.getChildren().add(entryLabel);
                });

        return cell;
    }

    @FXML
    private void handlePrevMonth(ActionEvent event) {
        currentMonth = currentMonth.minusMonths(1);
        updateCalendarDisplay();
    }

    @FXML
    private void handleNextMonth(ActionEvent event) {
        currentMonth = currentMonth.plusMonths(1);
        updateCalendarDisplay();
    }

    private void handleViewConsultation(Consultation consultation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/consultationDetailView.fxml"));
            Parent detailRoot = loader.load();

            ConsultationDetailController detailController = loader.getController();
            if (detailController != null) {
                detailController.setConsultation(consultation);
            } else {
                return;
            }

            Stage detailStage = new Stage();
            detailStage.setTitle("Consultation Details");
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.initOwner(statusLabel.getScene().getWindow());
            detailStage.setScene(new Scene(detailRoot));
            detailStage.setMinWidth(600);
            detailStage.setMinHeight(500);

            detailStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Loading Error");
            errorAlert.setHeaderText("Could not open consultation details.");
            errorAlert.setContentText("Failed to load the required layout resources. Check logs for details.");
            errorAlert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Could not display consultation details.");
            errorAlert.setContentText("An unexpected error occurred: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void setStatus(String message, Color color) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setTextFill(color);
            statusLabel.getStyleClass().removeAll("success-message", "error-message", "info-message");
            if (color == Color.GREEN) statusLabel.getStyleClass().add("success-message");
            else if (color == Color.RED || color == Color.ORANGE) statusLabel.getStyleClass().add("error-message");
            else statusLabel.getStyleClass().add("info-message");
        });
    }

    @FXML private void handleLogout(ActionEvent event) { LoginController.logout(); }

    @FXML private void navigateToDashboard(ActionEvent event) {
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
        WellTechApplication.loadFXML("articlesList");
    }

    @FXML private void navigateToConsultations(ActionEvent event) {
        WellTechApplication.loadFXML("patientConsultationsCalendarView");
    }

    @FXML private void navigateToProfile(ActionEvent event) {
        WellTechApplication.loadFXML("profile");
    }
}
