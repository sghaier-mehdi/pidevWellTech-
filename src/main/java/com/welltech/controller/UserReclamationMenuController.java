package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.model.User;
import com.welltech.model.UserRole;
import com.welltech.util.SessionManager;
import javafx.fxml.FXML;

public class UserReclamationMenuController {

    @FXML
    private void ouvrirFormulaire() {
        WellTechApplication.loadFXML("ReclamationForm");
    }

    @FXML
    private void ouvrirMesReclamations() {
        WellTechApplication.loadFXML("UserReclamations");
    }

    @FXML
    private void retourDashboard() {
        User user = SessionManager.getCurrentUser();

        if (user == null) {
            System.out.println("Aucun utilisateur connecté.");
            return;
        }

        if (user.getRole() == UserRole.PATIENT) {
            WellTechApplication.loadFXML("patientDashboard");
        } else if (user.getRole() == UserRole.PSYCHIATRIST) {
            WellTechApplication.loadFXML("psychiatristDashboard");
        } else {
            System.out.println("Rôle non reconnu.");
        }
    }
}
