package com.welltech.controller;

import com.welltech.WellTechApplication;
import com.welltech.dao.OrderDAO;
import com.welltech.dao.ProductDAO;
import com.welltech.dao.UserDAO;
import com.welltech.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
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
    private Button chatbotButton;
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
    private Button productsButton;
    
    @FXML
    private Button ordersButton;
    
    private final UserDAO userDAO = new UserDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get current user
            User currentUser = LoginController.getCurrentUser();
            
            if (currentUser != null) {
                // Update welcome label with user's name
                userNameLabel.setText("Welcome, " + currentUser.getFullName());
            } else {
                System.err.println("No user is logged in");
            }
            
            // Set dashboard button as active
            dashboardButton.getStyleClass().add("active");
            
            System.out.println("AdminDashboardController initialized");
        } catch (Exception e) {
            System.err.println("Error initializing AdminDashboardController: " + e.getMessage());
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
    private void navigateToChatbot(ActionEvent event) {
        System.out.println("AdminDashboardController: Navigating to Chatbot.");
        WellTechApplication.loadFXML("chatbotView");
    }
    
    @FXML
    private void handleProducts(ActionEvent event) {
        try {
            URL location = AdminDashboardController.class.getResource("/fxml/product/ProductList.fxml");
            if (location == null) {
                throw new IOException("Could not find ProductList.fxml");
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Product Management");
            stage.setScene(scene);
            
            ProductListController controller = loader.getController();
            controller.setProductDAO(productDAO);
            
            stage.show();
        } catch (Exception e) {
            showError("Error", "Could not open product management window", e);
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleOrders(ActionEvent event) {
        try {
            URL location = AdminDashboardController.class.getResource("/fxml/order/OrderList.fxml");
            if (location == null) {
                throw new IOException("Could not find OrderList.fxml");
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Order Management");
            stage.setScene(scene);
            
            OrderListController controller = loader.getController();
            controller.setOrderDAO(orderDAO);
            
            stage.show();
        } catch (Exception e) {
            showError("Error", "Could not open order management window", e);
            e.printStackTrace();
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showError(String title, String content, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content + "\n" + e.getMessage());
        alert.showAndWait();
    }
} 