package com.welltech.dao;

import com.welltech.model.Reclamation;
import com.welltech.db.DatabaseConnection;


import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReclamationDAO {

    /**
     * Adds a reclamation and returns its generated ID.
     * @param r The Reclamation object to add.
     * @return The generated ID if successful, or -1 if an error occurred.
     */
    public int addReclamation(Reclamation r) {
        String sql = "INSERT INTO reclamation (titre, contenu, date_creation, sentiment, typeReclamation, urgent, followUp, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int generatedId = -1; // Initialize to indicate failure

        // Use try-with-resources - it handles closing Connection and PreparedStatement
        // Connection and PreparedStatement are declared INSIDE the try() parentheses
        try (Connection conn = DatabaseConnection.getConnection(); // Assumes DatabaseConnection is imported correctly
             // Specify Statement.RETURN_GENERATED_KEYS when preparing
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Check if connection was successful
            if (conn == null) {
                System.err.println("Failed to get database connection in addReclamation.");
                return -1;
            }

            stmt.setString(1, r.getTitre());
            stmt.setString(2, r.getContenu());
            // Check if dateCreation is null before converting
            if (r.getDateCreation() != null) {
                stmt.setDate(3, Date.valueOf(r.getDateCreation()));
            } else {
                stmt.setNull(3, Types.DATE); // Or handle as error if date is required
            }
            stmt.setString(4, r.getSentiment());
            stmt.setString(5, r.getTypeReclamation());
            stmt.setBoolean(6, r.isUrgent());
            stmt.setBoolean(7, r.isFollowUp());
            stmt.setInt(8, r.getUserId()); // Ensure this ID exists in users table

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // Retrieve the generated key
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) { // Use try-with-resources for ResultSet
                    if (generatedKeys != null && generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1); // Get the ID
                        r.setId(generatedId); // Optional: Update the object passed in
                        System.out.println("Reclamation inserted with ID: " + generatedId);
                    } else {
                        System.err.println("Creating reclamation succeeded, but no ID obtained.");
                    }
                } // generatedKeys ResultSet is automatically closed here
            } else {
                System.err.println("Creating reclamation failed, no rows affected.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur insertion réclamation : " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            // generatedId remains -1
        }
        // conn and stmt are automatically closed here by try-with-resources
        return generatedId; // Return the generated ID or -1
    }

    public List<Reclamation> getReclamationsByUserId(int userId) {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reclamation r = new Reclamation(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getDate("date_creation").toLocalDate(),
                        rs.getString("sentiment"),
                        rs.getString("typeReclamation"),
                        rs.getBoolean("urgent"),
                        rs.getBoolean("followUp"),
                        rs.getInt("user_id")
                );
                list.add(r);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération réclamations utilisateur : " + e.getMessage());
        }

        return list;
    }

    public List<Reclamation> getAllReclamations() {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Reclamation r = new Reclamation(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getDate("date_creation").toLocalDate(),
                        rs.getString("sentiment"),
                        rs.getString("typeReclamation"),
                        rs.getBoolean("urgent"),
                        rs.getBoolean("followUp"),
                        rs.getInt("user_id")
                );
                list.add(r);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération réclamations : " + e.getMessage());
        }

        return list;
    }

    public void deleteReclamation(int id) {
        String sql_reponse = "DELETE FROM reponse WHERE reclamation_id = ?";
        String sql_notification = "DELETE FROM notifications WHERE related_reclamation_id = ?";
        String sql_reclamation = "DELETE FROM reclamation WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Delete related responses
            try (PreparedStatement stmt_reponse = conn.prepareStatement(sql_reponse)) {
                stmt_reponse.setInt(1, id);
                stmt_reponse.executeUpdate();
            }
            // Delete related notifications
            try (PreparedStatement stmt_notification = conn.prepareStatement(sql_notification)) {
                stmt_notification.setInt(1, id);
                stmt_notification.executeUpdate();
            }

            // Delete the reclamation itself
            try (PreparedStatement stmt_reclamation = conn.prepareStatement(sql_reclamation)) {
                stmt_reclamation.setInt(1, id);
                stmt_reclamation.executeUpdate();
            }

            conn.commit(); // Commit transaction

        } catch (SQLException e) {
            System.err.println("Erreur suppression réclamation et liées : " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback : " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    // Consider closing the connection if not managed elsewhere
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la réinitialisation de l'auto-commit : " + e.getMessage());
                }
            }
        }
    }


    public boolean updateReclamation(Reclamation r) {
        String sql = "UPDATE reclamation SET titre = ?, contenu = ?, date_creation = ?, sentiment = ?, typeReclamation = ?, urgent = ?, followUp = ? WHERE id = ?";
        boolean success = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, r.getTitre());
            stmt.setString(2, r.getContenu());
            stmt.setDate(3, Date.valueOf(r.getDateCreation()));
            stmt.setString(4, r.getSentiment());
            stmt.setString(5, r.getTypeReclamation());
            stmt.setBoolean(6, r.isUrgent());
            stmt.setBoolean(7, r.isFollowUp());
            stmt.setInt(8, r.getId());

            int rows = stmt.executeUpdate();
            success = rows > 0;

        } catch (SQLException e) {
            System.err.println("Erreur mise à jour réclamation : " + e.getMessage());
        }

        return success;
    }

    public List<Reclamation> getReclamationsByUserIdAndDate(int userId, LocalDate date) {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE user_id = ? AND DATE(date_creation) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Reclamation rec = new Reclamation();
                rec.setId(rs.getInt("id"));
                rec.setTitre(rs.getString("titre"));
                rec.setContenu(rs.getString("contenu"));
                rec.setDateCreation(rs.getDate("date_creation").toLocalDate());
                rec.setSentiment(rs.getString("sentiment"));
                rec.setTypeReclamation(rs.getString("typeReclamation"));
                rec.setUrgent(rs.getBoolean("urgent"));
                rec.setFollowUp(rs.getBoolean("followUp"));
                rec.setUserId(rs.getInt("user_id"));

                reclamations.add(rec);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération réclamations par date : " + e.getMessage());
        }

        return reclamations;
    }

    public Reclamation getReclamationById(int id) {
        String sql = "SELECT * FROM reclamation WHERE id = ?";
        Reclamation r = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                r = new Reclamation(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getDate("date_creation").toLocalDate(),
                        rs.getString("sentiment"),
                        rs.getString("typeReclamation"),
                        rs.getBoolean("urgent"),
                        rs.getBoolean("followUp"),
                        rs.getInt("user_id")
                );
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération réclamation par ID : " + e.getMessage());
        }

        return r;
    }
    public List<Reclamation> getReclamationsByDate(LocalDate date) {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE DATE(date_creation) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reclamation rec = new Reclamation();
                rec.setId(rs.getInt("id"));
                rec.setTitre(rs.getString("titre"));
                rec.setContenu(rs.getString("contenu"));
                rec.setDateCreation(rs.getDate("date_creation").toLocalDate());
                rec.setSentiment(rs.getString("sentiment"));
                rec.setTypeReclamation(rs.getString("typeReclamation"));
                rec.setUrgent(rs.getBoolean("urgent"));
                rec.setFollowUp(rs.getBoolean("followUp"));
                rec.setUserId(rs.getInt("user_id"));

                reclamations.add(rec);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération réclamations par date : " + e.getMessage());
        }

        return reclamations;
    }
    public boolean hasResponse(int reclamationId) {
        String sql = "SELECT COUNT(*) FROM reponse WHERE reclamation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reclamationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public java.util.Map<String, Integer> getReclamationCountBySentiment() {
        java.util.Map<String, Integer> sentimentCounts = new java.util.HashMap<>();
        // Ensure sentiment column is not null or empty for meaningful grouping
        String sql = "SELECT sentiment, COUNT(*) as count FROM reclamation WHERE sentiment IS NOT NULL AND sentiment != '' GROUP BY sentiment";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (conn == null) { System.err.println("DB connection null in getReclamationCountBySentiment"); return sentimentCounts; }

            while (rs.next()) {
                String sentiment = rs.getString("sentiment");
                int count = rs.getInt("count");
                sentimentCounts.put(sentiment, count);
            }
        } catch (SQLException e) {
            System.err.println("Error getting reclamation count by sentiment: " + e.getMessage());
            e.printStackTrace();
            // Return potentially empty map on error
        }
        return sentimentCounts;
    }
    public int getUrgentReclamationCount() {
        String sql = "SELECT COUNT(*) FROM reclamation WHERE urgent = TRUE"; // Or urgent = 1 if TINYINT
        int count = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (conn == null) { System.err.println("DB connection null in getUrgentReclamationCount"); return -1; }

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting urgent reclamation count: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }
    public int getTotalReclamationCount() {
        String sql = "SELECT COUNT(*) FROM reclamation";
        int count = -1; // Default to error

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (conn == null) { System.err.println("DB connection null in getTotalReclamationCount"); return -1; }

            if (rs.next()) {
                count = rs.getInt(1); // Get the count from the first column
            }
        } catch (SQLException e) {
            System.err.println("Error getting total reclamation count: " + e.getMessage());
            e.printStackTrace();
            // count remains -1
        }
        return count;
    }

    /**
     * Counts reclamations based on their sentiment.
     * Requires the 'sentiment' column exists and is populated.
     * @return A Map where keys are sentiments (String) and values are counts (Integer). Returns empty map on error.
     */


    /**
     * Counts reclamations based on their type.
     * Requires the 'typeReclamation' column exists and is populated.
     * @return A Map where keys are types (String) and values are counts (Integer). Returns empty map on error.
     */
    public java.util.Map<String, Integer> getReclamationCountByType() {
        java.util.Map<String, Integer> typeCounts = new java.util.HashMap<>();
        // Ensure typeReclamation column is not null or empty
        String sql = "SELECT typeReclamation, COUNT(*) as count FROM reclamation WHERE typeReclamation IS NOT NULL AND typeReclamation != '' GROUP BY typeReclamation";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (conn == null) { System.err.println("DB connection null in getReclamationCountByType"); return typeCounts; }

            while (rs.next()) {
                String type = rs.getString("typeReclamation");
                int count = rs.getInt("count");
                typeCounts.put(type, count);
            }
        } catch (SQLException e) {
            System.err.println("Error getting reclamation count by type: " + e.getMessage());
            e.printStackTrace();
        }
        return typeCounts;
    }

    /**
     * Counts reclamations that are marked as urgent.
     * Requires the 'urgent' column exists (BOOLEAN or TINYINT(1)).
     * @return The count of urgent reclamations, or -1 on error.
     */


    /**
     * Counts reclamations that do NOT have a corresponding response.
     * This requires joining or using a subquery with the 'reponse' table.
     * @return The count of unanswered reclamations, or -1 on error.
     */
    public int getUnansweredReclamationCount() {
        // Using LEFT JOIN and checking where response ID is NULL
        String sql = "SELECT COUNT(r.id) FROM reclamation r LEFT JOIN reponse rep ON r.id = rep.reclamation_id WHERE rep.id IS NULL";
        // Alternative using NOT EXISTS subquery (might perform differently)
        // String sql = "SELECT COUNT(*) FROM reclamation r WHERE NOT EXISTS (SELECT 1 FROM reponse rep WHERE rep.reclamation_id = r.id)";
        int count = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (conn == null) { System.err.println("DB connection null in getUnansweredReclamationCount"); return -1; }

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting unanswered reclamation count: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }

}




