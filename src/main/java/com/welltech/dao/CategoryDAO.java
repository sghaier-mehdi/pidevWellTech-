package com.welltech.dao; // This package declaration is correct

// *** THESE IMPORT STATEMENTS ARE THE FIX ***
import com.welltech.model.Category;       // Correct model package
import com.welltech.db.DatabaseConnection; // Correct database utility package

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    // Method to get all categories (for the ComboBox)
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name FROM category ORDER BY name"; // Order alphabetically

        // Use the CORRECT DatabaseConnection class from the CORRECT package
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                // Use the CORRECT Category class from the CORRECT package
                categories.add(new Category(id, name));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            // Consider more robust error handling/logging
        }
        return categories;
    }

    // Method to add a new category
    // Takes the CORRECT Category object as input
    public boolean addCategory(Category category) {
        // Basic check for null or empty name
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            System.err.println("Cannot add category with empty name.");
            return false;
        }

        String sql = "INSERT INTO category (name) VALUES (?)";
        boolean success = false;

        // Use the CORRECT DatabaseConnection class
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Get generated ID back if needed

            pstmt.setString(1, category.getName().trim());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                success = true;
                // Optionally get the generated ID and set it back to the category object
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Use the setId method from the CORRECT Category class
                        category.setId(generatedKeys.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            // Handle potential duplicate entry if name is UNIQUE
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate entry")) {
                System.err.println("Error adding category: A category with the name '" + category.getName() + "' already exists.");
            } else {
                System.err.println("Error adding category: " + e.getMessage());
            }
            // Optionally print stack trace for debugging other SQL errors
            // e.printStackTrace();
        }
        return success;
    }

    // Add methods for updateCategory and deleteCategory if you need full CRUD later
    // public boolean updateCategory(Category category) { ... }
    // public boolean deleteCategory(int categoryId) { ... }
}