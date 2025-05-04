package com.welltech.dao;

import com.welltech.db.DatabaseConnection;
import com.welltech.model.Objective;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Objective-related database operations
 */
public class ObjectiveDAO {
    
    /**
     * Insert a new objective into the database
     * @param objective Objective object to insert
     * @return Generated objective ID if successful, -1 otherwise
     */
    public int insertObjective(Objective objective) {
        String sql = "INSERT INTO objective (title, description, points_required, user_id) VALUES (?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, objective.getTitle());
            statement.setString(2, objective.getDescription());
            statement.setInt(3, objective.getPointsRequired());
            
            if (objective.getUserId() != null) {
                statement.setInt(4, objective.getUserId());
            } else {
                statement.setNull(4, java.sql.Types.INTEGER);
            }
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting objective: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Get an objective by its ID
     * @param id Objective ID
     * @return Objective object if found, null otherwise
     */
    public Objective getObjectiveById(int id) {
        String sql = "SELECT * FROM objective WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return extractObjectiveFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error getting objective by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all objectives from the database
     * @return List of Objective objects
     */
    public List<Objective> getAllObjectives() {
        List<Objective> objectives = new ArrayList<>();
        String sql = "SELECT * FROM objective";
        
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                objectives.add(extractObjectiveFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all objectives: " + e.getMessage());
            e.printStackTrace();
        }
        
        return objectives;
    }
    
    /**
     * Find objectives with minimum points requirement
     * @param points Minimum points required
     * @return List of Objective objects with pointsRequired >= points, ordered by pointsRequired DESC
     */
    public List<Objective> findObjectivesWithMinimumPoints(int points) {
        List<Objective> objectives = new ArrayList<>();
        String sql = "SELECT * FROM objective WHERE points_required >= ? ORDER BY points_required DESC";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, points);
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                objectives.add(extractObjectiveFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error finding objectives with minimum points: " + e.getMessage());
            e.printStackTrace();
        }
        
        return objectives;
    }
    
    /**
     * Find objectives by title (case-insensitive)
     * @param title Title to search for
     * @return List of Objective objects with title LIKE %title%, ordered by title ASC
     */
    public List<Objective> findByTitle(String title) {
        List<Objective> objectives = new ArrayList<>();
        String sql = "SELECT * FROM objective WHERE LOWER(title) LIKE LOWER(?) ORDER BY title ASC";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, "%" + title + "%");
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                objectives.add(extractObjectiveFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error finding objectives by title: " + e.getMessage());
            e.printStackTrace();
        }
        
        return objectives;
    }
    
    /**
     * Find objectives created between two dates
     * @param start Start date
     * @param end End date
     * @return List of Objective objects created between start and end, ordered by createdAt DESC
     */
    public List<Objective> findByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Objective> objectives = new ArrayList<>();
        String sql = "SELECT * FROM objective WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setTimestamp(1, Timestamp.valueOf(start));
            statement.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                objectives.add(extractObjectiveFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error finding objectives by date range: " + e.getMessage());
            e.printStackTrace();
        }
        
        return objectives;
    }
    
    /**
     * Find top N objectives with highest points requirement
     * @param limit Maximum number of objectives to return
     * @return List of top N Objective objects by pointsRequired, sorted DESC
     */
    public List<Objective> findTopObjectives(int limit) {
        List<Objective> objectives = new ArrayList<>();
        String sql = "SELECT * FROM objective ORDER BY points_required DESC LIMIT ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, limit);
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                objectives.add(extractObjectiveFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error finding top objectives: " + e.getMessage());
            e.printStackTrace();
        }
        
        return objectives;
    }
    
    /**
     * Update an objective in the database
     * @param objective Objective object to update
     * @return true if successful, false otherwise
     */
    public boolean updateObjective(Objective objective) {
        String sql = "UPDATE objective SET title = ?, description = ?, points_required = ?, user_id = ? WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, objective.getTitle());
            statement.setString(2, objective.getDescription());
            statement.setInt(3, objective.getPointsRequired());
            
            if (objective.getUserId() != null) {
                statement.setInt(4, objective.getUserId());
            } else {
                statement.setNull(4, java.sql.Types.INTEGER);
            }
            
            statement.setInt(5, objective.getId());
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating objective: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete an objective from the database
     * @param id ID of the objective to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteObjective(int id) {
        String sql = "DELETE FROM objective WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting objective: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Extract objective data from a ResultSet
     * @param rs ResultSet containing objective data
     * @return Objective object
     * @throws SQLException if a database error occurs
     */
    private Objective extractObjectiveFromResultSet(ResultSet rs) throws SQLException {
        Objective objective = new Objective();
        objective.setId(rs.getInt("id"));
        objective.setTitle(rs.getString("title"));
        objective.setDescription(rs.getString("description"));
        objective.setPointsRequired(rs.getInt("points_required"));
        
        // Handle nullable user_id
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            objective.setUserId(userId);
        }
        
        // Convert TIMESTAMP to LocalDateTime
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        if (createdAtTimestamp != null) {
            objective.setCreatedAt(createdAtTimestamp.toLocalDateTime());
        }
        
        Timestamp updatedAtTimestamp = rs.getTimestamp("updated_at");
        if (updatedAtTimestamp != null) {
            objective.setUpdatedAt(updatedAtTimestamp.toLocalDateTime());
        }
        
        return objective;
    }
} 