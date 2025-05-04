package com.welltech.dao;

import com.welltech.db.DatabaseConnection;
import com.welltech.model.Coupon;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Coupon-related database operations
 */
public class CouponDAO {
    
    /**
     * Insert a new coupon into the database
     * @param coupon Coupon object to insert
     * @return Generated coupon ID if successful, -1 otherwise
     */
    public int insertCoupon(Coupon coupon) {
        String sql = "INSERT INTO coupon (name, discount_percentage, code, is_active, expiration_date, usage_count) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, coupon.getName());
            statement.setInt(2, coupon.getDiscountPercentage());
            statement.setString(3, coupon.getCode());
            statement.setInt(4, coupon.isActive() ? 1 : 0);
            
            if (coupon.getExpirationDate() != null) {
                statement.setTimestamp(5, Timestamp.valueOf(coupon.getExpirationDate()));
            } else {
                statement.setNull(5, java.sql.Types.TIMESTAMP);
            }
            
            statement.setInt(6, coupon.getUsageCount());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting coupon: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Get a coupon by its ID
     * @param id Coupon ID
     * @return Coupon object if found, null otherwise
     */
    public Coupon getCouponById(int id) {
        String sql = "SELECT * FROM coupon WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return extractCouponFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error getting coupon by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get a coupon by its unique code
     * @param code Coupon code
     * @return Coupon object if found, null otherwise
     */
    public Coupon getCouponByCode(String code) {
        String sql = "SELECT * FROM coupon WHERE code = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, code);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return extractCouponFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error getting coupon by code: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all coupons from the database
     * @return List of Coupon objects
     */
    public List<Coupon> getAllCoupons() {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM coupon";
        
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                coupons.add(extractCouponFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all coupons: " + e.getMessage());
            e.printStackTrace();
        }
        
        return coupons;
    }
    
    /**
     * Get all active coupons (not expired and is_active = true)
     * @return List of active Coupon objects
     */
    public List<Coupon> getActiveCoupons() {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM coupon WHERE is_active = 1 AND (expiration_date IS NULL OR expiration_date > NOW())";
        
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                coupons.add(extractCouponFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error getting active coupons: " + e.getMessage());
            e.printStackTrace();
        }
        
        return coupons;
    }
    
    /**
     * Update a coupon in the database
     * @param coupon Coupon object to update
     * @return true if successful, false otherwise
     */
    public boolean updateCoupon(Coupon coupon) {
        String sql = "UPDATE coupon SET name = ?, discount_percentage = ?, code = ?, is_active = ?, expiration_date = ?, usage_count = ? WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, coupon.getName());
            statement.setInt(2, coupon.getDiscountPercentage());
            statement.setString(3, coupon.getCode());
            statement.setInt(4, coupon.isActive() ? 1 : 0);
            
            if (coupon.getExpirationDate() != null) {
                statement.setTimestamp(5, Timestamp.valueOf(coupon.getExpirationDate()));
            } else {
                statement.setNull(5, java.sql.Types.TIMESTAMP);
            }
            
            statement.setInt(6, coupon.getUsageCount());
            statement.setInt(7, coupon.getId());
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating coupon: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a coupon from the database
     * @param id ID of the coupon to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteCoupon(int id) {
        String sql = "DELETE FROM coupon WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting coupon: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Increment the usage count of a coupon
     * @param couponId ID of the coupon
     * @return true if successful, false otherwise
     */
    public boolean incrementUsageCount(int couponId) {
        String sql = "UPDATE coupon SET usage_count = usage_count + 1 WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, couponId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error incrementing coupon usage count: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Extract coupon data from a ResultSet
     * @param rs ResultSet containing coupon data
     * @return Coupon object
     * @throws SQLException if a database error occurs
     */
    private Coupon extractCouponFromResultSet(ResultSet rs) throws SQLException {
        Coupon coupon = new Coupon();
        coupon.setId(rs.getInt("id"));
        coupon.setName(rs.getString("name"));
        coupon.setDiscountPercentage(rs.getInt("discount_percentage"));
        coupon.setCode(rs.getString("code"));
        coupon.setActive(rs.getInt("is_active") == 1);
        
        // Convert TIMESTAMP to LocalDateTime
        Timestamp expirationTimestamp = rs.getTimestamp("expiration_date");
        if (expirationTimestamp != null) {
            coupon.setExpirationDate(expirationTimestamp.toLocalDateTime());
        }
        
        coupon.setUsageCount(rs.getInt("usage_count"));
        
        return coupon;
    }
} 