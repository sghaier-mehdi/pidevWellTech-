package com.welltech.dao;

import com.welltech.db.DatabaseConnection;
import com.welltech.model.Objective;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ObjectiveDAO {
    private Connection connection;

    public ObjectiveDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    // Create
    public void createObjective(Objective objective) throws SQLException {
        String sql = "INSERT INTO Objective (title, description, points_required, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, objective.getTitle());
            stmt.setString(2, objective.getDescription());
            stmt.setInt(3, objective.getPointsRequired());
            stmt.setInt(4, objective.getUserId());
            stmt.executeUpdate();

            // Retrieve generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    objective.setId(rs.getInt(1));
                }
            }
        }
    }

    // Read
    public List<Objective> getAllObjectives() throws SQLException {
        List<Objective> objectives = new ArrayList<>();
        String sql = "SELECT * FROM Objective";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Objective objective = new Objective();
                objective.setId(rs.getInt("id"));
                objective.setTitle(rs.getString("title"));
                objective.setDescription(rs.getString("description"));
                objective.setPointsRequired(rs.getInt("points_required"));
                objective.setUserId(rs.getInt("user_id"));
                objectives.add(objective);
            }
        }
        return objectives;
    }

    // Update
    public void updateObjective(Objective objective) throws SQLException {
        String sql = "UPDATE Objective SET title=?, description=?, points_required=?, user_id=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, objective.getTitle());
            stmt.setString(2, objective.getDescription());
            stmt.setInt(3, objective.getPointsRequired());
            stmt.setInt(4, objective.getUserId());
            stmt.setInt(5, objective.getId());
            stmt.executeUpdate();
        }
    }

    // Delete
    public void deleteObjective(int id) throws SQLException {
        String sql = "DELETE FROM Objective WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}