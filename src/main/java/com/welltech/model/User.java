package com.welltech.model;

// Ensure UserRole is accessible (either in same package or imported if needed)
// Assuming UserRole.java is also in com.welltech.model

/**
 * User model class representing a user in the system
 */
public class User {
    private int id;
    private String username;
    private String password; // Store hashed passwords in production
    private String fullName;
    private String email;
    private String phoneNumber; // Should store E.164 format (+...)
    private UserRole role; // Uses the separate UserRole enum

    // Default constructor
    public User() {
    }

    // Parameterized constructor (without ID - for creation)
    public User(String username, String password, String fullName, String email, String phoneNumber, UserRole role) {
        this.username = username;
        this.password = password; // Handle hashing before calling this constructor
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber; // Ensure format validation before calling
        this.role = role;
    }

    // Constructor with ID (for retrieval from DB)
    public User(int id, String username, String password, String fullName, String email, String phoneNumber, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password; // Password retrieved from DB (should be hash)
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password; // Consider security implications
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        // Add validation/formatting here or before calling setter if needed
        this.phoneNumber = phoneNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override // Added Override annotation
    public String toString() {
        // Provides a useful default representation, often used in ComboBoxes if not customized
        return (fullName != null && !fullName.isEmpty()) ? fullName : username;
    }
}