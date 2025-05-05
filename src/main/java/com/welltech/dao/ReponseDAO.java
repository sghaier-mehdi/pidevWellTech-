package com.welltech.dao;

import com.welltech.model.Reponse;
import com.welltech.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseDAO {

    public boolean addReponse(Reponse reponse) {
        String sql = "INSERT INTO reponse (contenu, created_at, reclamation_id) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reponse.getContenu());
            stmt.setTimestamp(2, Timestamp.valueOf(reponse.getDateReponse()));
            stmt.setInt(3, reponse.getReclamationId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la réponse : " + e.getMessage());
            return false;
        }
    }
    public boolean deleteReponse(int id) {
        String sql = "DELETE FROM reponse WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la réponse : " + e.getMessage());
            return false;
        }
    }
    public boolean updateReponse(Reponse reponse) {
        String sql = "UPDATE reponse SET contenu = ?, created_at = ?, reclamation_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reponse.getContenu());
            stmt.setTimestamp(2, Timestamp.valueOf(reponse.getDateReponse()));
            stmt.setInt(3, reponse.getReclamationId());
            stmt.setInt(4, reponse.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la réponse : " + e.getMessage());
            return false;
        }
    }


    public List<Reponse> getAllReponses() {
        List<Reponse> list = new ArrayList<>();
        String sql = "SELECT * FROM reponse";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Reponse r = new Reponse(
                        rs.getString("contenu"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getInt("reclamation_id")
                );
                r.setId(rs.getInt("id"));
                list.add(r);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réponses : " + e.getMessage());
        }

        return list;
    }

    public Reponse getReponseByReclamationId(int reclamationId) {
        String sql = "SELECT * FROM reponse WHERE reclamation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reclamationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Reponse r = new Reponse(
                        rs.getString("contenu"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getInt("reclamation_id")
                );
                r.setId(rs.getInt("id"));
                return r;
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la réponse : " + e.getMessage());
        }

        return null;
    }
}
