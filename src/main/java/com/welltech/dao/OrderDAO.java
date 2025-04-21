package com.welltech.dao;

import com.welltech.model.Order;
import com.welltech.model.OrderItem;
import com.welltech.model.User;
import com.welltech.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class OrderDAO {
    
    public Order save(Order order) {
        String sql = "INSERT INTO orders (user_id, total_amount, status, shipping_address, payment_method, payment_status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, order.getUser().getId());
            pstmt.setBigDecimal(2, order.getTotalAmount());
            pstmt.setString(3, order.getStatus());
            pstmt.setString(4, order.getShippingAddress());
            pstmt.setString(5, order.getPaymentMethod());
            pstmt.setString(6, order.getPaymentStatus());
            pstmt.setTimestamp(7, Timestamp.valueOf(order.getCreatedAt()));
            pstmt.setTimestamp(8, Timestamp.valueOf(order.getUpdatedAt()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setId(generatedKeys.getLong(1));
                    saveOrderItems(order);
                    return order;
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving order", e);
        }
    }
    
    public Order update(Order order) {
        String sql = "UPDATE orders SET user_id = ?, total_amount = ?, status = ?, shipping_address = ?, " +
                    "payment_method = ?, payment_status = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, order.getUser().getId());
            pstmt.setBigDecimal(2, order.getTotalAmount());
            pstmt.setString(3, order.getStatus());
            pstmt.setString(4, order.getShippingAddress());
            pstmt.setString(5, order.getPaymentMethod());
            pstmt.setString(6, order.getPaymentStatus());
            pstmt.setTimestamp(7, Timestamp.valueOf(order.getUpdatedAt()));
            pstmt.setLong(8, order.getId());
            
            pstmt.executeUpdate();
            updateOrderItems(order);
            return order;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating order", e);
        }
    }
    
    public void delete(Order order) {
        String sql = "DELETE FROM orders WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, order.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting order", e);
        }
    }
    
    public Order findById(long id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding order by ID", e);
        }
        return null;
    }
    
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // First, collect all basic order data
            List<Long> orderIds = new ArrayList<>();
            List<Integer> userIds = new ArrayList<>();
            
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getLong("id"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setShippingAddress(rs.getString("shipping_address"));
                order.setPaymentMethod(rs.getString("payment_method"));
                order.setPaymentStatus(rs.getString("payment_status"));
                order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                
                orderIds.add(order.getId());
                userIds.add(rs.getInt("user_id"));
                orders.add(order);
            }
            
            // After ResultSet is closed, load users and order items
            UserDAO userDAO = new UserDAO();
            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);
                order.setUser(userDAO.getUserById(userIds.get(i)));
                loadOrderItems(order);
            }
            
            return orders;
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding all orders", e);
        }
    }
    
    public List<Order> findByUser(User user) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, user.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding orders by user", e);
        }
        return orders;
    }
    
    private void saveOrderItems(Order order) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price_per_unit) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            for (OrderItem item : order.getOrderItems()) {
                pstmt.setLong(1, order.getId());
                pstmt.setLong(2, item.getProduct().getId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setBigDecimal(4, item.getUnitPrice());
                
                pstmt.executeUpdate();
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving order items", e);
        }
    }
    
    private void updateOrderItems(Order order) {
        // First delete existing items
        String deleteSql = "DELETE FROM order_items WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setLong(1, order.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting order items", e);
        }
        
        // Then save new items
        saveOrderItems(order);
    }
    
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        
        // Set user
        UserDAO userDAO = new UserDAO();
        order.setUser(userDAO.getUserById(rs.getInt("user_id")));
        
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        // Load order items
        loadOrderItems(order);
        
        return order;
    }
    
    private void loadOrderItems(Order order) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        ObservableList<OrderItem> items = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, order.getId());
            
            // First, collect all data from ResultSet
            List<Long> itemIds = new ArrayList<>();
            List<Long> productIds = new ArrayList<>();
            List<Integer> quantities = new ArrayList<>();
            List<BigDecimal> prices = new ArrayList<>();
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    itemIds.add(rs.getLong("id"));
                    productIds.add(rs.getLong("product_id"));
                    quantities.add(rs.getInt("quantity"));
                    prices.add(rs.getBigDecimal("price_per_unit"));
                }
            }
            
            // Now create OrderItems with the collected data
            ProductDAO productDAO = new ProductDAO();
            for (int i = 0; i < itemIds.size(); i++) {
                OrderItem item = new OrderItem();
                item.setId(itemIds.get(i));
                item.setOrder(order);
                item.setProduct(productDAO.findById(productIds.get(i)));
                item.setQuantity(quantities.get(i));
                item.setUnitPrice(prices.get(i));
                items.add(item);
            }
            
            // Set all items to the order at once
            order.setOrderItems(items);
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading order items", e);
        }
    }
} 