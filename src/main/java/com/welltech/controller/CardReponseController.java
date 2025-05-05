package com.welltech.controller;

import com.welltech.dao.ReponseDAO;
import com.welltech.model.Reponse;
import com.welltech.util.SessionManager;
import com.welltech.model.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class CardReponseController {

    @FXML
    private Label contenuLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private HBox adminButtons;

    private final ReponseDAO dao = new ReponseDAO();
    private Reponse currentReponse;

    public void setData(Reponse reponse) {
        this.currentReponse = reponse;

        contenuLabel.setText("💬 " + reponse.getContenu());
        dateLabel.setText("📅 Le " + reponse.getDateReponse().toLocalDate().toString());

        // Afficher les boutons seulement si admin connecté
        boolean isAdmin = SessionManager.getCurrentUser().getRole() == UserRole.ADMIN;
        adminButtons.setVisible(isAdmin);
    }

    @FXML
    private void modifierReponse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditReponse.fxml"));
            Parent root = loader.load();

            EditReponseController controller = loader.getController();
            controller.setReponse(currentReponse);

            Stage stage = new Stage();
            stage.setTitle("Modifier Réponse");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerReponse() {
        boolean deleted = dao.deleteReponse(currentReponse.getId());
        if (deleted) {
            showAlert("Réponse supprimée !");
            // Optionnel : rafraîchir la liste dans le contrôleur parent
        } else {
            showAlert("Échec de la suppression !");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
