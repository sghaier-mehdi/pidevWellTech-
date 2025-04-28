package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.DefiDAO;
import com.welltech.dao.RecompenseDAO;
import com.welltech.dao.UserDAO;
import com.welltech.model.Defi;
import com.welltech.model.Recompense;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the admin dashboard
 */
public class AdminDashboardController implements Initializable {

    @FXML
    private Label userNameLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button psychiatristsButton;

    @FXML
    private Button patientsButton;

    @FXML
    private Button settingsButton;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> userIdColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, User.UserRole> roleColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;

    private final UserDAO userDAO = new UserDAO();
    private final DefiDAO defiDAO = new DefiDAO();
    private final RecompenseDAO recompenseDAO = new RecompenseDAO();

    @FXML
    private TabPane Users;

    @FXML
    private Button articlesButton11;

    @FXML
    private TabPane Defi;

    @FXML
    private TextField titre;

    @FXML
    private TabPane UsersTab;

    @FXML
    private TextField pointsRequis;

    @FXML
    private TextField type;

    @FXML
    private TextField nom;

    @FXML
    private TextField points;

    @FXML
    private VBox RecompenseContainer;

    @FXML
    private Button articlesButton1;

    @FXML
    private TabPane Recompense;

    @FXML
    private TextField DateFin;

    @FXML
    private VBox DefiContainer;

    @FXML
    private TextField type1;

    @FXML
    private TextArea desc1;

    @FXML
    private Button articlesButton;

    @FXML
    private TextArea condition;

    @FXML
    private TextField dateDeb;


    @FXML
    private Button profileButton;

    @FXML
    private TextArea desc;

    @FXML
    private TextField status;

    public AdminDashboardController() throws SQLException {
    }

    @FXML
    void navigateToDefi(ActionEvent event) {
        UsersTab.setVisible(false);
        Recompense.setVisible(false);
        Defi.setVisible(true);
        Defi.getSelectionModel().select(0); // Select the first tab in the Defi TabPane
    }

    @FXML
    void navigateToRecompense(ActionEvent event) {
        UsersTab.setVisible(false);
        Defi.setVisible(false);
        Recompense.setVisible(true);
        Recompense.getSelectionModel().select(0); // Select the first tab in the Recompense TabPane
    }

    @FXML
    void AddDefi(ActionEvent event) {
        if (verifyDefiForm()) {
            try {
                LocalDate startDate = LocalDate.parse(dateDeb.getText());
                LocalDate endDate = LocalDate.parse(DateFin.getText());

                Defi defi = new Defi(
                        titre.getText(),
                        desc.getText(),
                        Integer.parseInt(points.getText()),
                        "status",
                        startDate,
                        endDate,
                        condition.getText(),
                        type.getText()
                );

                defiDAO.createDefi(defi);
                loadDefis(); // Refresh the Defi list
                clearDefiFields(); // Clear the form fields
                showAlert(Alert.AlertType.INFORMATION, "Success", "Defi added successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add Defi: " + e.getMessage());
            }
        }
    }
    private void loadDefis() throws SQLException {
        List<Defi> defis = defiDAO.getAllDefis();
        DefiContainer.getChildren().clear();

        for (Defi defi : defis) {
            // Create a styled VBox for each Defi
            VBox defiBox = new VBox(5);
            defiBox.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

            // Add Defi details as styled labels
            Label titleLabel = new Label("Titre : " + defi.getTitre());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
            Label descriptionLabel = new Label("Description : " + defi.getDescription());
            Label pointsLabel = new Label("Points : " + defi.getPoints());
            Label statusLabel = new Label("Statut : " + defi.getStatut());
            Label dateDebutLabel = new Label("Date de Début : " + defi.getDateDebut());
            Label dateFinLabel = new Label("Date de Fin : " + defi.getDateFin());
            Label conditionsLabel = new Label("Conditions : " + defi.getConditions());
            Label typeLabel = new Label("Type : " + defi.getType());

            // Add Modify and Delete buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            Button modifyButton = new Button("Modifier");
            modifyButton.setOnAction(event -> {
                try {
                    showModifyDefiDialog(defi); // Show the modify dialog
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement du défi pour modification.");
                }
            });

            Button deleteButton = new Button("Supprimer");
            deleteButton.setOnAction(event -> {
                showAlert(Alert.AlertType.CONFIRMATION, "Confirmation", "Êtes-vous sûr de vouloir supprimer ce défi ?")
                        .ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                try {
                                    defiDAO.deleteDefi(defi.getId()); // Delete the Defi from the database
                                    loadDefis(); // Reload the Defis list
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression du défi.");
                                }
                            }
                        });
            });

            buttonBox.getChildren().addAll(modifyButton, deleteButton);

            // Add all components to the Defi box
            defiBox.getChildren().addAll(
                    titleLabel,
                    descriptionLabel,
                    pointsLabel,
                    statusLabel,
                    dateDebutLabel,
                    dateFinLabel,
                    conditionsLabel,
                    typeLabel,
                    buttonBox
            );

            DefiContainer.getChildren().add(defiBox);
        }
    }

    @FXML
    void AddRecompense(ActionEvent event) {
        if (verifyRecompenseForm()) {
            try {
                com.welltech.model.Recompense recompense = new Recompense(
                        nom.getText(),
                        desc1.getText(),
                        Integer.parseInt(pointsRequis.getText()),
                        type1.getText(),
                        "active" // Default status
                );

                recompenseDAO.createRecompense(recompense);
                loadRecompenses(); // Refresh the Recompense list
                clearRecompenseFields(); // Clear the form fields
                showAlert(Alert.AlertType.INFORMATION, "Success", "Recompense added successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add Recompense: " + e.getMessage());
            }
        }
    }

    private boolean verifyDefiForm() {
        if (titre.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez saisir un titre valide.");
            return false;
        }
        if (desc.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez saisir une description valide.");
            return false;
        }
        if (points.getText().trim().isEmpty() || !points.getText().matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Les points doivent être un nombre valide.");
            return false;
        }
        if (dateDeb.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez saisir une date de début valide.");
            return false;
        }
        if (DateFin.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez saisir une date de fin valide.");
            return false;
        }
        if (condition.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez saisir des conditions valides.");
            return false;
        }
        if (type.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez saisir un type valide.");
            return false;
        }
        return true;
    }

    private boolean verifyRecompenseForm() {
        if (nom.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez saisir un nom valide.");
            return false;
        }
        if (desc1.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez saisir une description valide.");
            return false;
        }
        if (pointsRequis.getText().trim().isEmpty() || !pointsRequis.getText().matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Les points requis doivent être un nombre valide.");
            return false;
        }
        if (type1.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez saisir un type valide.");
            return false;
        }
        return true;
    }


    private void loadRecompenses() throws SQLException {
        List<Recompense> recompenses = recompenseDAO.getAllRecompenses();
        RecompenseContainer.getChildren().clear();

        for (Recompense recompense : recompenses) {
            // Create a styled VBox for each Recompense
            VBox recompenseBox = new VBox(5);
            recompenseBox.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

            // Add Recompense details as styled labels
            Label nameLabel = new Label("Nom : " + recompense.getNom());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
            Label descriptionLabel = new Label("Description : " + recompense.getDescription());
            Label pointsLabel = new Label("Points Requis : " + recompense.getPointsRequis());
            Label typeLabel = new Label("Type : " + recompense.getType());
            Label statuLabel = new Label("Statut : " + recompense.getStatu());

            // Add Modify and Delete buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            Button modifyButton = new Button("Modifier");
            modifyButton.setOnAction(event -> {
                try {
                    showModifyRecompenseDialog(recompense); // Show the modify dialog
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement de la récompense pour modification.");
                }
            });

            Button deleteButton = new Button("Supprimer");
            deleteButton.setOnAction(event -> {
                showAlert(Alert.AlertType.CONFIRMATION, "Confirmation", "Êtes-vous sûr de vouloir supprimer cette récompense ?")
                        .ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                try {
                                    recompenseDAO.deleteRecompense(recompense.getId()); // Delete the Recompense from the database
                                    loadRecompenses(); // Reload the Recompenses list
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression de la récompense.");
                                }
                            }
                        });
            });

            buttonBox.getChildren().addAll(modifyButton, deleteButton);

            // Add all components to the Recompense box
            recompenseBox.getChildren().addAll(
                    nameLabel,
                    descriptionLabel,
                    pointsLabel,
                    typeLabel,
                    statuLabel,
                    buttonBox
            );

            RecompenseContainer.getChildren().add(recompenseBox);
        }
    }

    private void showModifyDefiDialog(Defi defi) throws SQLException {
        Stage modifyStage = new Stage();
        modifyStage.setTitle("Modifier le Défi");

        TextField titleField = new TextField(defi.getTitre());
        TextArea descriptionArea = new TextArea(defi.getDescription());
        Spinner<Integer> pointsSpinner = new Spinner<>(0, Integer.MAX_VALUE, defi.getPoints());
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Actif", "Inactif");
        statusComboBox.setValue(defi.getStatut());
        DatePicker startDatePicker = new DatePicker(defi.getDateDebut());
        DatePicker endDatePicker = new DatePicker(defi.getDateFin());
        TextArea conditionsArea = new TextArea(defi.getConditions());
        TextField typeField = new TextField(defi.getType());

        Button saveButton = new Button("Enregistrer");
        Button cancelButton = new Button("Annuler");

        saveButton.setOnAction(event -> {
            try {
                defi.setTitre(titleField.getText());
                defi.setDescription(descriptionArea.getText());
                defi.setPoints(pointsSpinner.getValue());
                defi.setStatut(statusComboBox.getValue());
                defi.setDateDebut(startDatePicker.getValue());
                defi.setDateFin(endDatePicker.getValue());
                defi.setConditions(conditionsArea.getText());
                defi.setType(typeField.getText());

                defiDAO.updateDefi(defi); // Update the Defi in the database
                loadDefis(); // Reload the Defis list
                modifyStage.close(); // Close the dialog
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour du défi.");
            }
        });

        cancelButton.setOnAction(event -> modifyStage.close());

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 10;");
        layout.getChildren().addAll(
                new Label("Modifier le Défi"),
                new HBox(10, new Label("Titre :"), titleField),
                new HBox(10, new Label("Description :"), descriptionArea),
                new HBox(10, new Label("Points :"), pointsSpinner),
                new HBox(10, new Label("Statut :"), statusComboBox),
                new HBox(10, new Label("Date de Début :"), startDatePicker),
                new HBox(10, new Label("Date de Fin :"), endDatePicker),
                new HBox(10, new Label("Conditions :"), conditionsArea),
                new HBox(10, new Label("Type :"), typeField),
                new HBox(10, saveButton, cancelButton)
        );

        Scene scene = new Scene(layout, 400, 400);
        modifyStage.setScene(scene);
        modifyStage.showAndWait();
    }

    private void showModifyRecompenseDialog(Recompense recompense) throws SQLException {
        Stage modifyStage = new Stage();
        modifyStage.setTitle("Modifier la Récompense");

        TextField nameField = new TextField(recompense.getNom());
        TextArea descriptionArea = new TextArea(recompense.getDescription());
        Spinner<Integer> pointsSpinner = new Spinner<>(0, Integer.MAX_VALUE, recompense.getPointsRequis());
        TextField typeField = new TextField(recompense.getType());
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Actif", "Inactif");
        statusComboBox.setValue(recompense.getStatu());

        Button saveButton = new Button("Enregistrer");
        Button cancelButton = new Button("Annuler");

        saveButton.setOnAction(event -> {
            try {
                recompense.setNom(nameField.getText());
                recompense.setDescription(descriptionArea.getText());
                recompense.setPointsRequis(pointsSpinner.getValue());
                recompense.setType(typeField.getText());
                recompense.setStatu(statusComboBox.getValue());

                recompenseDAO.updateRecompense(recompense); // Update the Recompense in the database
                loadRecompenses(); // Reload the Recompenses list
                modifyStage.close(); // Close the dialog
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour de la récompense.");
            }
        });

        cancelButton.setOnAction(event -> modifyStage.close());

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 10;");
        layout.getChildren().addAll(
                new Label("Modifier la Récompense"),
                new HBox(10, new Label("Nom :"), nameField),
                new HBox(10, new Label("Description :"), descriptionArea),
                new HBox(10, new Label("Points Requis :"), pointsSpinner),
                new HBox(10, new Label("Type :"), typeField),
                new HBox(10, new Label("Statut :"), statusComboBox),
                new HBox(10, saveButton, cancelButton)
        );

        Scene scene = new Scene(layout, 400, 400);
        modifyStage.setScene(scene);
        modifyStage.showAndWait();
    }

    /**
     * Clear Defi form fields
     */
    private void clearDefiFields() {
        titre.clear();
        desc.clear();
        points.clear();
        status.clear();
        dateDeb.clear();
        DateFin.clear();
        condition.clear();
        type.clear();
    }

    /**
     * Clear Recompense form fields
     */
    private void clearRecompenseFields() {
        nom.clear();
        desc1.clear();
        pointsRequis.clear();
        type1.clear();
    }

    /**
     * Show an alert dialog
     */
    private Optional<ButtonType> showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
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
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get current user
            try {
                // Load Defis and Recompenses on initialization
                loadDefis();
                loadRecompenses();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            User currentUser = LoginController.getCurrentUser();

            if (currentUser != null) {
                // Update welcome label with user's name
                userNameLabel.setText("Welcome, " + currentUser.getFullName());
            } else {
                System.err.println("No user is logged in");
            }

            // Set dashboard button as active
            dashboardButton.getStyleClass().add("active");

            // Initialize users table (in a real app, we would set cell value factories and populate data)
            // For now, we're just setting up the structure

            System.out.println("AdminDashboardController initialized");
        } catch (Exception e) {
            System.err.println("Error initializing AdminDashboardController: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 