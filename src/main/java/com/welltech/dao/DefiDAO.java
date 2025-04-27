package com.welltech.dao;

import com.welltech.db.DatabaseConnection;
import com.welltech.model.Defi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DefiDAO {
    private Connection connection;

    public DefiDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    // Create
    public void createDefi(Defi defi) throws SQLException {
        String sql = "INSERT INTO Defi (titre, description, points, statut, date_debut, date_fin, conditions, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, defi.getTitre());
            stmt.setString(2, defi.getDescription());
            stmt.setInt(3, defi.getPoints());
            stmt.setString(4, defi.getStatut());
            stmt.setDate(5, Date.valueOf(defi.getDateDebut()));
            stmt.setDate(6, Date.valueOf(defi.getDateFin()));
            stmt.setString(7, defi.getConditions());
            stmt.setString(8, defi.getType());
            stmt.executeUpdate();

            // Retrieve generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    defi.setId(rs.getInt(1));
                }
            }
        }
    }

    // Read
    public List<Defi> getAllDefis() throws SQLException {
        List<Defi> defis = new ArrayList<>();
        String sql = "SELECT * FROM Defi";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Defi defi = new Defi();
                defi.setId(rs.getInt("id"));
                defi.setTitre(rs.getString("titre"));
                defi.setDescription(rs.getString("description"));
                defi.setPoints(rs.getInt("points"));
                defi.setStatut(rs.getString("statut"));
                defi.setDateDebut(rs.getDate("date_debut").toLocalDate());
                defi.setDateFin(rs.getDate("date_fin").toLocalDate());
                defi.setConditions(rs.getString("conditions"));
                defi.setType(rs.getString("type"));
                defis.add(defi);
            }
        }
        return defis;
    }

    // Update
    public void updateDefi(Defi defi) throws SQLException {
        String sql = "UPDATE Defi SET titre=?, description=?, points=?, statut=?, date_debut=?, date_fin=?, conditions=?, type=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, defi.getTitre());
            stmt.setString(2, defi.getDescription());
            stmt.setInt(3, defi.getPoints());
            stmt.setString(4, defi.getStatut());
            stmt.setDate(5, Date.valueOf(defi.getDateDebut()));
            stmt.setDate(6, Date.valueOf(defi.getDateFin()));
            stmt.setString(7, defi.getConditions());
            stmt.setString(8, defi.getType());
            stmt.setInt(9, defi.getId());
            stmt.executeUpdate();
        }
    }

    // Delete
    public void deleteDefi(int id) throws SQLException {
        String sql = "DELETE FROM Defi WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}