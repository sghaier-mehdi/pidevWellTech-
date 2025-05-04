package com.welltech.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirebaseNotificationManager implements NotificationManager {
    private static final String FIREBASE_URL = "https://welltech-58ee3-default-rtdb.firebaseio.com";

    @Override
    public boolean sendNotification(String message, String userId) {
        try {
            String target = (userId == null || userId.isEmpty()) ? "global" : userId;
            String notifKey = "notif" + System.currentTimeMillis();
            String url = FIREBASE_URL + "/notifications/" + target + "/" + notifKey + ".json";
            
            String json = "{ \"message\": \"" + message + "\", \"timestamp\": " + (System.currentTimeMillis() / 1000) + " }";
            
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean sendGlobalNotification(String message) {
        return sendNotification(message, null);
    }
} 