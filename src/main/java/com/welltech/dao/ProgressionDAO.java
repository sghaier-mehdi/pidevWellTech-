package com.welltech.dao;

import com.welltech.db.DatabaseConnection;
import com.welltech.model.Progression;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgressionDAO {
    private Connection connection;

    public ProgressionDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    // Create
    public void createProgression(Progression progression) throws SQLException {
        String sql = "INSERT INTO Progression (user_id, defi_id, statut, progression) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, progression.getUserId());
            stmt.setInt(2, progression.getDefiId());
            stmt.setString(3, progression.getStatut());
            stmt.setDouble(4, progression.getProgression());
            stmt.executeUpdate();

            // Retrieve generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    progression.setId(rs.getInt(1));
                }
            }
        }
    }

    // Read
    public List<Progression> getAllProgressions() throws SQLException {
        List<Progression> progressions = new ArrayList<>();
        String sql = "SELECT * FROM Progression";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Progression progression = new Progression();
                progression.setId(rs.getInt("id"));
                progression.setUserId(rs.getInt("user_id"));
                progression.setDefiId(rs.getInt("defi_id"));
                progression.setStatut(rs.getString("statut"));
                progression.setProgression(rs.getDouble("progression"));
                progressions.add(progression);
            }
        }
        return progressions;
    }


    public List<Progression> getProgressionsByUserId(int userId) throws SQLException {
        List<Progression> progressions = new ArrayList<>();
        String sql = "SELECT * FROM Progression WHERE user_id = ?"; // Fixed SQL query

        try (PreparedStatement stmt = connection.prepareStatement(sql)) { // Prepare the statement
            stmt.setInt(1, userId); // Set the user ID parameter
            ResultSet rs = stmt.executeQuery(); // Execute the query without passing the SQL string again

            while (rs.next()) { // Iterate through the result set
                Progression progression = new Progression(); // Create a new Progression object
                progression.setId(rs.getInt("id")); // Set the ID
                progression.setUserId(rs.getInt("user_id")); // Set the user ID
                progression.setDefiId(rs.getInt("defi_id")); // Set the defi ID
                progression.setStatut(rs.getString("statut")); // Set the status
                progression.setProgression(rs.getDouble("progression")); // Set the progression value
                progressions.add(progression); // Add the progression to the list
            }
        }
        return progressions; // Return the list of progressions
    }


    // Update
    public void updateProgression(Progression progression) throws SQLException {
        String sql = "UPDATE Progression SET statut=?, progression=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, progression.getStatut());
            stmt.setDouble(2, progression.getProgression());
            stmt.setInt(3, progression.getId());
            stmt.executeUpdate();
        }
    }

    // Delete
    public void deleteProgression(int id) throws SQLException {
        String sql = "DELETE FROM Progression WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}