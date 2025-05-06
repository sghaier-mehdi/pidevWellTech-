package com.welltech.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> price = new SimpleObjectProperty<>();
    private final IntegerProperty stockQuantity = new SimpleIntegerProperty();
    private final StringProperty imageUrl = new SimpleStringProperty();
    private final BooleanProperty active = new SimpleBooleanProperty(true);
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    public Product() {
        this.createdAt.set(LocalDateTime.now());
        this.updatedAt.set(LocalDateTime.now());
    }

    public Product(Long id, String name, String description, BigDecimal price, 
                  Integer stockQuantity, String imageUrl) {
        this();
        this.id.set(id);
        this.name.set(name);
        this.description.set(description);
        this.price.set(price);
        this.stockQuantity.set(stockQuantity);
        this.imageUrl.set(imageUrl);
    }

    // Getters and Setters
    public long getId() { return id.get(); }
    public LongProperty idProperty() { return id; }
    public void setId(long value) { id.set(value); }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String value) { name.set(value); }

    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }
    public void setDescription(String value) { description.set(value); }

    public BigDecimal getPrice() { return price.get(); }
    public ObjectProperty<BigDecimal> priceProperty() { return price; }
    public void setPrice(BigDecimal value) { price.set(value); }

    public int getStockQuantity() { return stockQuantity.get(); }
    public IntegerProperty stockQuantityProperty() { return stockQuantity; }
    public void setStockQuantity(int value) { stockQuantity.set(value); }

    public String getImageUrl() { return imageUrl.get(); }
    public StringProperty imageUrlProperty() { return imageUrl; }
    public void setImageUrl(String value) { imageUrl.set(value); }

    public boolean isActive() { return active.get(); }
    public BooleanProperty activeProperty() { return active; }
    public void setActive(boolean value) { active.set(value); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt.set(value); }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt.set(value); }

    @Override
    public String toString() {
        return getName();
    }
} 