package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.DefiDAO;
import com.welltech.dao.ObjectiveDAO;
import com.welltech.dao.ProgressionDAO;
import com.welltech.dao.UserDAO;
import com.welltech.model.Objective;
import com.welltech.model.Progression;
import com.welltech.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the psychiatrist dashboard
 */
public class PsychiatristDashboardController implements Initializable {
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button dashboardButton;
    
    @FXML
    private Button patientsButton;
    
    @FXML
    private Button appointmentsButton;
    
    @FXML
    private Button messagesButton;

    @FXML
    private VBox dashboard;

    @FXML
    private VBox objectives;

    @FXML
    private VBox patients;
    
    @FXML
    private Button profileButton;
    
    @FXML
    private TableView<?> appointmentsTable;
    
    @FXML
    private TableColumn<?, ?> timeColumn;
    
    @FXML
    private TableColumn<?, ?> patientColumn;
    
    @FXML
    private TableColumn<?, ?> purposeColumn;
    
    @FXML
    private TableColumn<?, ?> statusColumn;
    
    @FXML
    private TableColumn<?, ?> actionColumn;


    private final ObjectiveDAO objectiveService = new ObjectiveDAO();
    private final ProgressionDAO progressionService = new ProgressionDAO();
    private final UserDAO userService = new UserDAO();
    private final DefiDAO defiService = new DefiDAO();

    public PsychiatristDashboardController() throws SQLException {
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get current user
            User currentUser = LoginController.getCurrentUser();
            
            if (currentUser != null) {
                // Update welcome label with user's name
                userNameLabel.setText("Welcome, Dr. " + currentUser.getFullName());
            } else {
                System.err.println("No user is logged in");
            }
            
            // Set dashboard button as active
            dashboardButton.getStyleClass().add("active");
            
            // Initialize appointment table (dummy data would be added here)
            // For a real app, we would fetch appointments from database
            
            System.out.println("PsychiatristDashboardController initialized");
        } catch (Exception e) {
            System.err.println("Error initializing PsychiatristDashboardController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle logout button click
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logging out");
        LoginController.logout();
    }
    
    /**
     * Navigate to the profile page
     */
    @FXML
    private void navigateToProfile(ActionEvent event) {
        System.out.println("Navigating to profile");
        WellTechApplication.loadFXML("profile");
    }
    
    /**
     * Navigate to the articles page
     */
    @FXML
    private void navigateToArticles(ActionEvent event) {
        System.out.println("Navigating to articles");
        WellTechApplication.loadFXML("articlesList");
    }

    @FXML
    void goToPatients(ActionEvent event) {
        dashboard.setVisible(false);
        objectives.setVisible(false);
        patients.setVisible(true);
        try {
            loadPatientsProgression(); // Load patient progressions into the patients VBox
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement des progrès des patients.");
        }
    }

    private Optional<ButtonType> showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    private void loadPatientsProgression() throws SQLException {
        // Fetch all patients from the database
        List<User> patient = userService.getAllPatients(); // Assume a method in UserDAO to fetch all patients
        patients.getChildren().clear(); // Clear existing content

        for (User pat : patient) {
            // Fetch progressions for each patient
            List<Progression> progressions = progressionService.getProgressionsByUserId(pat.getId());

            if (!progressions.isEmpty()) {
                VBox patientBox = new VBox(10);
                patientBox.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

                // Add patient details as labels
                Label nameLabel = new Label("Patient : " + pat.getFullName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                Label userIdLabel = new Label("ID : " + pat.getId());

                // Create a container for progressions
                VBox progressionsBox = new VBox(5);
                progressionsBox.setStyle("-fx-padding: 5;");

                for (Progression progression : progressions) {
                    HBox progressionItem = new HBox(10);
                    progressionItem.setStyle("-fx-padding: 5; -fx-border-color: #ddd; -fx-border-width: 1px;");

                    Label defiTitleLabel = new Label("Défi : " + defiService.recupererParId(progression.getDefiId()).getTitre());
                    Label statusLabel = new Label("Statut : " + progression.getStatut());
                    Label progressLabel = new Label("Progrès : " + progression.getProgression() + "%");
                    Label completionDateLabel = new Label("Date de Completion : " + progression.getDateCompletion());

                    progressionItem.getChildren().addAll(defiTitleLabel, statusLabel, progressLabel, completionDateLabel);
                    progressionsBox.getChildren().add(progressionItem);
                }

                // Add patient details and progressions to the main container
                patientBox.getChildren().addAll(nameLabel, userIdLabel, progressionsBox);
                patients.getChildren().add(patientBox);
            }
        }
    }

    @FXML
    void goToObjectives(ActionEvent event) {
        try {
            dashboard.setVisible(false);
            objectives.setVisible(true);
            patients.setVisible(false);
            loadObjectives(); // Load objectives into the objectives VBox
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement des objectifs.");
        }
    }

    private void loadObjectives() throws SQLException {
        List<Objective> objective = objectiveService.getAllObjectives(); // Fetch all objectives
        objectives.getChildren().clear(); // Clear existing content

        for (Objective obj : objective) {
            VBox objectiveBox = new VBox(5);
            objectiveBox.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-width: 1px;");

            Label titleLabel = new Label("Titre : " + obj.getTitle());
            titleLabel.setStyle("-fx-font-weight: bold;");
            Label descriptionLabel = new Label("Description : " + obj.getDescription());
            Label pointsLabel = new Label("Points Requis : " + obj.getPointsRequired());

            Button modifyButton = new Button("Modifier");
            modifyButton.setOnAction(e -> showModifyObjectiveDialog(obj));

            Button deleteButton = new Button("Supprimer");
            deleteButton.setOnAction(e -> {
                showAlert(Alert.AlertType.CONFIRMATION, "Confirmation", "Êtes-vous sûr de vouloir supprimer cet objectif ?")
                        .ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                try {
                                    objectiveService.deleteObjective(obj.getId());
                                    loadObjectives(); // Reload objectives after deletion
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression de l'objectif.");
                                }
                            }
                        });
            });

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(modifyButton, deleteButton);

            objectiveBox.getChildren().addAll(titleLabel, descriptionLabel, pointsLabel, buttonBox);
            objectives.getChildren().add(objectiveBox);
        }
    }

    private void showModifyObjectiveDialog(Objective objective) {
        Stage modifyStage = new Stage();
        modifyStage.setTitle("Modifier l'Objectif");

        TextField titleField = new TextField(objective.getTitle());
        TextArea descriptionArea = new TextArea(objective.getDescription());
        Spinner<Integer> pointsSpinner = new Spinner<>(0, Integer.MAX_VALUE, objective.getPointsRequired());

        Button saveButton = new Button("Enregistrer");
        saveButton.setOnAction(e -> {
            try {
                objective.setTitle(titleField.getText());
                objective.setDescription(descriptionArea.getText());
                objective.setPointsRequired(pointsSpinner.getValue());
                objectiveService.updateObjective(objective);
                loadObjectives(); // Reload objectives after modification
                modifyStage.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour de l'objectif.");
            }
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(e -> modifyStage.close());

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 10;");
        layout.getChildren().addAll(
                new Label("Modifier l'Objectif"),
                new HBox(10, new Label("Titre :"), titleField),
                new HBox(10, new Label("Description :"), descriptionArea),
                new HBox(10, new Label("Points Requis :"), pointsSpinner),
                new HBox(10, saveButton, cancelButton)
        );

        Scene scene = new Scene(layout, 400, 400);
        modifyStage.setScene(scene);
        modifyStage.showAndWait();
    }



} 