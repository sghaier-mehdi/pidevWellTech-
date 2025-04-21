package com.welltech.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private final LongProperty id = new SimpleLongProperty();
    private final ObjectProperty<User> user = new SimpleObjectProperty<>();
    private final ListProperty<OrderItem> orderItems = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<BigDecimal> totalAmount = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty shippingAddress = new SimpleStringProperty();
    private final StringProperty paymentMethod = new SimpleStringProperty();
    private final StringProperty paymentStatus = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    public Order() {
        this.createdAt.set(LocalDateTime.now());
        this.updatedAt.set(LocalDateTime.now());
        this.status.set("PENDING");
        this.paymentStatus.set("PENDING");
    }

    // Getters and Setters
    public long getId() { return id.get(); }
    public LongProperty idProperty() { return id; }
    public void setId(long value) { id.set(value); }

    public User getUser() { return user.get(); }
    public ObjectProperty<User> userProperty() { return user; }
    public void setUser(User value) { user.set(value); }

    public ObservableList<OrderItem> getOrderItems() { return orderItems.get(); }
    public ListProperty<OrderItem> orderItemsProperty() { return orderItems; }
    public void setOrderItems(ObservableList<OrderItem> value) { orderItems.set(value); }

    public BigDecimal getTotalAmount() { return totalAmount.get(); }
    public ObjectProperty<BigDecimal> totalAmountProperty() { return totalAmount; }
    public void setTotalAmount(BigDecimal value) { totalAmount.set(value); }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String value) { status.set(value); }

    public String getShippingAddress() { return shippingAddress.get(); }
    public StringProperty shippingAddressProperty() { return shippingAddress; }
    public void setShippingAddress(String value) { shippingAddress.set(value); }

    public String getPaymentMethod() { return paymentMethod.get(); }
    public StringProperty paymentMethodProperty() { return paymentMethod; }
    public void setPaymentMethod(String value) { paymentMethod.set(value); }

    public String getPaymentStatus() { return paymentStatus.get(); }
    public StringProperty paymentStatusProperty() { return paymentStatus; }
    public void setPaymentStatus(String value) { paymentStatus.set(value); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt.set(value); }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt.set(value); }

    public void calculateTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : getOrderItems()) {
            total = total.add(item.getSubtotal());
        }
        setTotalAmount(total);
    }
} 