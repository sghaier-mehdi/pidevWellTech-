package com.welltech.dao;

import com.welltech.db.DatabaseConnection;
import com.welltech.model.Recompense;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecompenseDAO {
    private Connection connection;

    public RecompenseDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    // Create
    public void createRecompense(Recompense recompense) throws SQLException {
        String sql = "INSERT INTO Recompense (nom, description, points_requis, type, statu) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, recompense.getNom());
            stmt.setString(2, recompense.getDescription());
            stmt.setInt(3, recompense.getPointsRequis());
            stmt.setString(4, recompense.getType());
            stmt.setString(5, recompense.getStatu());
            stmt.executeUpdate();

            // Retrieve generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    recompense.setId(rs.getInt(1));
                }
            }
        }
    }

    // Read
    public List<Recompense> getAllRecompenses() throws SQLException {
        List<Recompense> recompenses = new ArrayList<>();
        String sql = "SELECT * FROM Recompense";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Recompense recompense = new Recompense();
                recompense.setId(rs.getInt("id"));
                recompense.setNom(rs.getString("nom"));
                recompense.setDescription(rs.getString("description"));
                recompense.setPointsRequis(rs.getInt("points_requis"));
                recompense.setType(rs.getString("type"));
                recompense.setStatu(rs.getString("statu"));
                recompenses.add(recompense);
            }
        }
        return recompenses;
    }

    // Update
    public void updateRecompense(Recompense recompense) throws SQLException {
        String sql = "UPDATE Recompense SET nom=?, description=?, points_requis=?, type=?, statu=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, recompense.getNom());
            stmt.setString(2, recompense.getDescription());
            stmt.setInt(3, recompense.getPointsRequis());
            stmt.setString(4, recompense.getType());
            stmt.setString(5, recompense.getStatu());
            stmt.setInt(6, recompense.getId());
            stmt.executeUpdate();
        }
    }

    // Delete
    public void deleteRecompense(int id) throws SQLException {
        String sql = "DELETE FROM Recompense WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}