package com.welltech.model;

import java.time.LocalDateTime;

/**
 * Coupon model class representing a discount coupon in the system
 */
public class Coupon {
    private int id;
    private String name;
    private int discountPercentage;
    private String code;
    private boolean isActive;
    private LocalDateTime expirationDate;
    private int usageCount;
    
    // Default constructor
    public Coupon() {
        this.isActive = true;
        this.usageCount = 0;
    }
    
    // Constructor without ID (for creating new coupons)
    public Coupon(String name, int discountPercentage, String code, LocalDateTime expirationDate) {
        this.name = name;
        this.discountPercentage = discountPercentage;
        this.code = code;
        this.isActive = true;
        this.expirationDate = expirationDate;
        this.usageCount = 0;
    }
    
    // Full constructor
    public Coupon(int id, String name, int discountPercentage, String code, 
                 boolean isActive, LocalDateTime expirationDate, int usageCount) {
        this.id = id;
        this.name = name;
        this.discountPercentage = discountPercentage;
        this.code = code;
        this.isActive = isActive;
        this.expirationDate = expirationDate;
        this.usageCount = usageCount;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(int discountPercentage) {
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        this.discountPercentage = discountPercentage;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public int getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }
    
    /**
     * Increment the usage count when the coupon is used
     */
    public void incrementUsageCount() {
        this.usageCount++;
    }
    
    /**
     * Check if the coupon is valid (active and not expired)
     * @return true if the coupon is valid, false otherwise
     */
    public boolean isValid() {
        return isActive && (expirationDate == null || expirationDate.isAfter(LocalDateTime.now()));
    }
    
    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", discountPercentage=" + discountPercentage + "%" +
                ", code='" + code + '\'' +
                ", isActive=" + isActive +
                ", expirationDate=" + expirationDate +
                ", usageCount=" + usageCount +
                '}';
    }
} 