package com.welltech.model;

/**
 * User model class representing a user in the system
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserRole role;

    public User(int id, String username, String password, String fullName, String phoneNumber, UserRole role) {
    }

    // Enum for user roles
    public enum UserRole {
        PATIENT,
        PSYCHIATRIST,
        ADMIN
    }
    
    // Default constructor
    public User() {
    }
    
    // Parameterized constructor
    public User(String username, String password, String fullName, String email, String phoneNumber, UserRole role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
    
    // Constructor with ID
    public User(int id, String username, String password, String fullName, String email, String phoneNumber, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
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
        this.password = password;
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
        this.phoneNumber = phoneNumber;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    @Override
    public String toString() {
        return (fullName != null && !fullName.isEmpty()) ? fullName : username;
    }
} 