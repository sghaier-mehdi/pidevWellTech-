package com.welltech.controller;

import com.welltech.WellTechApplication; // To navigate back
import com.welltech.dao.ReclamationDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Import Initializable
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip; // Import Tooltip

import java.net.URL; // Import URL
import java.util.Map;
import java.util.ResourceBundle; // Import ResourceBundle

// Implement Initializable for standard JavaFX lifecycle
public class ReclamationStatsController implements Initializable {

    // --- FXML Fields ---
    // Ensure these fx:id values exist in your ReclamationStats.fxml
    @FXML private Label totalCountLabel;
    @FXML private Label unansweredCountLabel;
    @FXML private Label urgentCountLabel;
    @FXML private PieChart typePieChart;
    @FXML private PieChart sentimentPieChart;
    @FXML private Button backButton;

    // --- DAO Instance ---
    // Make final as it's initialized once
    private final ReclamationDAO reclamationDAO = new ReclamationDAO();

    /**
     * Called by the FXMLLoader after the FXML file has been loaded and
     * all @FXML fields have been injected.
     * Standard entry point for controller initialization.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing Reclamation Stats Controller...");
        // Set tooltips for the summary labels for clarity
        Tooltip.install(totalCountLabel, new Tooltip("Total number of all submitted reclamations."));
        Tooltip.install(unansweredCountLabel, new Tooltip("Number of reclamations without an admin response yet."));
        Tooltip.install(urgentCountLabel, new Tooltip("Number of reclamations marked as 'Urgent' by the submitter."));

        // Load the statistical data into the UI elements
        loadStatistics();
        System.out.println("Reclamation Stats Controller Initialized.");
    }

    /**
     * Fetches statistical data from the DAO and updates the corresponding UI elements (Labels and Charts).
     */
    private void loadStatistics() {
        System.out.println("Loading statistics...");
        try {
            // Load basic counts from DAO
            int totalCount = reclamationDAO.getTotalReclamationCount();
            int unansweredCount = reclamationDAO.getUnansweredReclamationCount();
            int urgentCount = reclamationDAO.getUrgentReclamationCount();

            // Update labels - Handle potential -1 error return from DAO methods
            totalCountLabel.setText(totalCount >= 0 ? String.valueOf(totalCount) : "Error");
            unansweredCountLabel.setText(unansweredCount >= 0 ? String.valueOf(unansweredCount) : "Error");
            urgentCountLabel.setText(urgentCount >= 0 ? String.valueOf(urgentCount) : "Error");

            // Load data for Pie Charts using the helper method
            // Ensure DAO methods return a Map<String, Integer> or handle null/empty maps gracefully
            loadPieChartData(typePieChart, reclamationDAO.getReclamationCountByType(), "No Type Data");
            loadPieChartData(sentimentPieChart, reclamationDAO.getReclamationCountBySentiment(), "No Sentiment Data");

            System.out.println("Statistics loaded successfully.");

        } catch (Exception e) {
            // Catch potential exceptions during DAO calls or UI updates
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
            // Display error state in UI
            totalCountLabel.setText("Error");
            unansweredCountLabel.setText("Error");
            urgentCountLabel.setText("Error");
            typePieChart.setTitle("Error loading type data");
            typePieChart.setData(FXCollections.observableArrayList()); // Clear chart data
            sentimentPieChart.setTitle("Error loading sentiment data");
            sentimentPieChart.setData(FXCollections.observableArrayList()); // Clear chart data
        }
    }

    /**
     * Helper method to populate a PieChart from a Map of data,
     * displaying count and percentage in slice labels and adding tooltips.
     *
     * @param chart The PieChart UI element to populate.
     * @param dataMap Map where Key is the slice category label (String) and Value is the count (Integer).
     * @param emptyMessage Message to display in the chart title if dataMap is null or empty.
     */
    private void loadPieChartData(PieChart chart, Map<String, Integer> dataMap, String emptyMessage) {
        // Create an ObservableList to hold the data slices for the PieChart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        if (dataMap != null && !dataMap.isEmpty()) {
            // Calculate the total sum of values for percentage calculation
            // Using long stream prevents potential integer overflow if counts are very large
            long total = dataMap.values().stream().mapToLong(Integer::longValue).sum();
            System.out.println("Total items for chart [" + (chart.getId() != null ? chart.getId() : "N/A") + "]: " + total); // Debug log

            // Iterate through the data map (e.g., {"Positive": 10, "Negative": 5})
            dataMap.forEach((key, value) -> {
                if (value != null && value > 0) { // Only add slices with a positive value
                    // Calculate percentage for this slice
                    double percentage = (total > 0) ? (100.0 * value / total) : 0.0;

                    // Create the formatted label string for the slice (e.g., "Positive\n10 (33.3%)")
                    String sliceLabel = String.format("%s\n%d (%.1f%%)", key, value, percentage);

                    // Create the PieChart.Data object with the label and value
                    PieChart.Data slice = new PieChart.Data(sliceLabel, value);
                    pieChartData.add(slice);
                } else {
                    System.out.println("Skipping slice with key '" + key + "' due to zero or null value.");
                }
            });

            // Check if any valid data was added before setting it
            if (!pieChartData.isEmpty()) {
                // Set the prepared data onto the PieChart
                chart.setData(pieChartData);
                chart.setTitle(""); // Clear any default/previous title

                // Make sure the labels are set to be visible
                chart.setLabelsVisible(true);
                // chart.setLabelLineLength(20); // Optional: Adjust label line length

                // Apply tooltips and hover effects AFTER setting data so nodes exist
                chart.getData().forEach(data -> {
                    // Extract original key and value for the tooltip
                    String dataLabel = data.getName(); // Gets the full "Key\nCount (Percent%)" label
                    String currentKeyOnly = dataLabel.contains("\n") ? dataLabel.substring(0, dataLabel.indexOf('\n')) : dataLabel; // Extract just the key part
                    long currentVal = (long) data.getPieValue(); // Get the numerical value

                    // Create a simpler tooltip showing just Key: Value
                    String tooltipMsg = String.format("%s: %d", currentKeyOnly, currentVal);
                    Tooltip tt = new Tooltip(tooltipMsg);
                    tt.setStyle("-fx-font-size: 12px;"); // Style tooltip font

                    // Install the tooltip on the visual node of the pie slice
                    Tooltip.install(data.getNode(), tt);

                    // Add hover effect to slightly enlarge the slice
                    data.getNode().setOnMouseEntered(event -> data.getNode().setStyle("-fx-scale-x: 1.05; -fx-scale-y: 1.05; -fx-cursor: hand;"));
                    data.getNode().setOnMouseExited(event -> data.getNode().setStyle("")); // Reset style on exit
                });
            } else {
                // If after filtering, no data remains (e.g., all values were 0)
                handleEmptyChartData(chart, emptyMessage + " (Zero Values)");
            }

        } else {
            // Handle the case where the input dataMap itself was null or empty
            handleEmptyChartData(chart, emptyMessage);
        }
    }

    /**
     * Helper method to configure a PieChart when there is no data to display.
     * @param chart The PieChart to configure.
     * @param message The message to display as the title.
     */
    private void handleEmptyChartData(PieChart chart, String message) {
        ObservableList<PieChart.Data> emptyData = FXCollections.observableArrayList();
        emptyData.add(new PieChart.Data(message, 1)); // Add a single placeholder slice
        chart.setData(emptyData);
        chart.setTitle(message); // Show the message as the chart title
        chart.setLabelsVisible(false); // Hide labels for the placeholder
        System.out.println("No data found for chart: " + (chart.getId() != null ? chart.getId() : "N/A") + " - Displaying placeholder.");
        // Ensure tooltip/hover is cleared if reusing chart node
        chart.getData().get(0).getNode().setOnMouseEntered(null);
        chart.getData().get(0).getNode().setOnMouseExited(null);
        Tooltip.uninstall(chart.getData().get(0).getNode(), null); // Attempt to remove existing tooltip if any
    }


    /**
     * Handles the action event when the "Back to Dashboard" button is clicked.
     * Navigates the user back to the main admin dashboard view.
     * @param event The action event trigger by the button click.
     */
    @FXML
    private void goBackToDashboard(ActionEvent event) {
        // Navigate back to the admin dashboard using the application's navigation method
        System.out.println("Navigating back to Admin Dashboard...");
        WellTechApplication.loadFXML("adminDashboard");
    }
}