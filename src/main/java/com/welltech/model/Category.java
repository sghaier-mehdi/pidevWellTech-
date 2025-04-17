package com.welltech.model; // Replace with your actual package

import java.util.Objects;

public class Category {
    private int id;
    private String name;
    // Add other fields if your category table has them (e.g., description)

    // Constructor for fetching from DB
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Constructor for creating a new category before saving
    public Category(String name) {
        this.name = name;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Setters (you might not need setId if DB auto-generates)
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --- IMPORTANT for ComboBox display ---
    @Override
    public String toString() {
        return name; // This determines what text is shown in the ComboBox
    }

    // --- IMPORTANT for ComboBox selection comparison ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        // If ID is 0 (not yet saved), compare by name, otherwise by ID
        if (id == 0 && category.id == 0) {
            return Objects.equals(name, category.name);
        }
        return id == category.id;
    }

    @Override
    public int hashCode() {
        // If ID is 0, use name's hashcode, otherwise ID's hashcode
        return id != 0 ? Objects.hash(id) : Objects.hash(name);
    }
}