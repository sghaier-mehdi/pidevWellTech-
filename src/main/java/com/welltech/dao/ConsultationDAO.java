package com.welltech.dao;

import com.welltech.model.Consultation;
import com.welltech.model.Consultation.ConsultationStatus;
import com.welltech.model.User;
import com.welltech.db.DatabaseConnection; // Use YOUR DatabaseConnection

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsultationDAO {

    private final UserDAO userDAO;

    public ConsultationDAO() {
        this.userDAO = new UserDAO();
    }

    private Consultation mapResultSetToConsultation(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int patientId = rs.getInt("patient_id");
        int psychiatristId = rs.getInt("psychiatrist_id");
        LocalDateTime consultationTime = rs.getTimestamp("consultation_time").toLocalDateTime();
        int durationMinutes = rs.getInt("duration_minutes");
        if (rs.wasNull()) durationMinutes = 0;

        String purpose = rs.getString("purpose");
        String notes = rs.getString("notes");
        ConsultationStatus status = ConsultationStatus.valueOf(rs.getString("status"));

        User patient = userDAO.getUserById(patientId); // Requires getUserById in UserDAO
        User psychiatrist = userDAO.getUserById(psychiatristId); // Requires getUserById in UserDAO

        return new Consultation(id, patient, psychiatrist, consultationTime, durationMinutes, purpose, notes, status);
    }

    public int insertConsultation(Consultation consultation) {
        String sql = "INSERT INTO consultations (patient_id, psychiatrist_id, consultation_time, duration_minutes, purpose, notes, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, consultation.getPatient().getId());
            pstmt.setInt(2, consultation.getPsychiatrist().getId());
            pstmt.setTimestamp(3, Timestamp.valueOf(consultation.getConsultationTime()));
            if (consultation.getDurationMinutes() > 0) {
                pstmt.setInt(4, consultation.getDurationMinutes());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setString(5, consultation.getPurpose());
            pstmt.setString(6, consultation.getNotes());
            pstmt.setString(7, consultation.getStatus().name());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating consultation failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating consultation failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error inserting consultation: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public Consultation getConsultationById(int id) {
        String sql = "SELECT * FROM consultations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToConsultation(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting consultation by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Consultation> getAllConsultations() {
        List<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT * FROM consultations ORDER BY consultation_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                consultations.add(mapResultSetToConsultation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all consultations: " + e.getMessage());
            e.printStackTrace();
        }
        return consultations;
    }

    public List<Consultation> getConsultationsByPatient(int patientId) {
        List<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT * FROM consultations WHERE patient_id = ? ORDER BY consultation_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    consultations.add(mapResultSetToConsultation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting consultations by patient: " + e.getMessage());
            e.printStackTrace();
        }
        return consultations;
    }

    public List<Consultation> getConsultationsByPsychiatrist(int psychiatristId) {
        List<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT * FROM consultations WHERE psychiatrist_id = ? ORDER BY consultation_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, psychiatristId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    consultations.add(mapResultSetToConsultation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting consultations by psychiatrist: " + e.getMessage());
            e.printStackTrace();
        }
        return consultations;
    }


    public boolean updateConsultation(Consultation consultation) {
        String sql = "UPDATE consultations SET patient_id = ?, psychiatrist_id = ?, consultation_time = ?, duration_minutes = ?, purpose = ?, notes = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, consultation.getPatient().getId());
            pstmt.setInt(2, consultation.getPsychiatrist().getId());
            pstmt.setTimestamp(3, Timestamp.valueOf(consultation.getConsultationTime()));
            if (consultation.getDurationMinutes() > 0) {
                pstmt.setInt(4, consultation.getDurationMinutes());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setString(5, consultation.getPurpose());
            pstmt.setString(6, consultation.getNotes());
            pstmt.setString(7, consultation.getStatus().name());
            pstmt.setInt(8, consultation.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating consultation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteConsultation(int id) {
        String sql = "DELETE FROM consultations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting consultation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}