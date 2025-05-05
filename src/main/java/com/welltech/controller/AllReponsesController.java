package com.welltech.controller;

import com.welltech.dao.ReponseDAO;
import com.welltech.model.Reponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class AllReponsesController {

    @FXML
    private VBox reponsesList;

    private final ReponseDAO dao = new ReponseDAO();

    @FXML
    public void initialize() {
        loadAllReponses();
    }

    private void loadAllReponses() {
        try {
            List<Reponse> reponses = dao.getAllReponses();
            reponsesList.getChildren().clear();

            for (Reponse r : reponses) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CardReponse.fxml"));
                Parent card = loader.load();

                CardReponseController controller = loader.getController();
                controller.setData(r);

                reponsesList.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void retourDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) reponsesList.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
