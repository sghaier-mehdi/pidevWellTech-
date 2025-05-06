package com.welltech.service;

import com.welltech.model.User;
import com.welltech.controller.LoginController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class NotificationService {
    private static final String FIREBASE_URL = "https://welltech-920e5-default-rtdb.firebaseio.com";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public static void showNotifications() {
        try {
            User currentUser = LoginController.getCurrentUser();
            if (currentUser == null) {
                showError("No user logged in");
                return;
            }

            StringBuilder allNotifications = new StringBuilder();
            
            // Get global notifications
            String globalNotifications = fetchNotifications("global");
            if (!globalNotifications.isEmpty()) {
                allNotifications.append("Global Notifications:\n\n").append(globalNotifications).append("\n");
            }

            // Get user-specific notifications
            String userNotifications = fetchNotifications(String.valueOf(currentUser.getId()));
            if (!userNotifications.isEmpty()) {
                allNotifications.append("Personal Notifications:\n\n").append(userNotifications);
            }

            if (allNotifications.length() == 0) {
                showNoNotifications();
            } else {
                showNotificationsDialog(allNotifications.toString());
            }
        } catch (Exception e) {
            showError("Error fetching notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String fetchNotifications(String path) throws Exception {
        String url = FIREBASE_URL + "/notifications/" + path + ".json";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String jsonResponse = response.toString();
            if (!jsonResponse.isEmpty() && !jsonResponse.equals("null")) {
                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                StringBuilder notificationsText = new StringBuilder();
                
                if (rootNode.isObject()) {
                    rootNode.fields().forEachRemaining(entry -> {
                        JsonNode notification = entry.getValue();
                        String message = notification.get("message").asText();
                        long timestamp = notification.get("timestamp").asLong();
                        
                        LocalDateTime dateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(timestamp), 
                            ZoneId.systemDefault()
                        );
                        String formattedTime = dateTime.format(formatter);
                        
                        notificationsText.append("• ").append(message)
                            .append("\n   Posted on: ").append(formattedTime)
                            .append("\n\n");
                    });
                }
                return notificationsText.toString();
            }
        }
        return "";
    }

    private static void showNotificationsDialog(String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Notifications");
            alert.setHeaderText("Your Notifications");
            alert.setContentText(content);
            alert.show();
        });
    }

    private static void showNoNotifications() {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Notifications");
            alert.setHeaderText("No Notifications");
            alert.setContentText("There are no notifications at this time.");
            alert.show();
        });
    }

    private static void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Load Notifications");
            alert.setContentText(message);
            alert.show();
        });
    }
} 