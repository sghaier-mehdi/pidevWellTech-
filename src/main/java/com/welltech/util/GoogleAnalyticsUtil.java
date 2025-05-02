package com.welltech.util;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GoogleAnalyticsUtil {
    private static final String MEASUREMENT_ID = "G-W2BBWH4S30";
    private static final String API_SECRET = "7EieLrFFS3y471lPbccynQ";

    public static void sendProductCreatedEvent(String productName, String productId) {
        try {
            String endpoint = String.format(
                "https://www.google-analytics.com/mp/collect?measurement_id=%s&api_secret=%s",
                MEASUREMENT_ID, API_SECRET
            );

            String payload = "{" +
                "\"client_id\": \"product-creation-job-001\"," +
                "\"events\": [{" +
                "  \"name\": \"product_created\"," +
                "  \"params\": {" +
                "    \"product_name\": \"" + productName + "\"," +
                "    \"product_id\": \"" + productId + "\"" +
                "  }" +
                "}]" +
                "}";

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 204) {
                System.err.println("GA4 event failed: HTTP " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 