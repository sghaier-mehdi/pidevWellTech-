package com.welltech.model;

import javafx.beans.property.*;
import java.math.BigDecimal;

public class OrderItem {
    private final LongProperty id = new SimpleLongProperty();
    private final ObjectProperty<Order> order = new SimpleObjectProperty<>();
    private final ObjectProperty<Product> product = new SimpleObjectProperty<>();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> subtotal = new SimpleObjectProperty<>();

    public OrderItem() {
        // Add listeners to automatically calculate subtotal when quantity or unitPrice changes
        quantity.addListener((obs, oldVal, newVal) -> calculateSubtotal());
        unitPrice.addListener((obs, oldVal, newVal) -> calculateSubtotal());
    }

    public OrderItem(Order order, Product product, int quantity) {
        this();
        this.order.set(order);
        this.product.set(product);
        this.quantity.set(quantity);
        this.unitPrice.set(product.getPrice());
        calculateSubtotal();
    }

    // Getters and Setters
    public long getId() { return id.get(); }
    public LongProperty idProperty() { return id; }
    public void setId(long value) { id.set(value); }

    public Order getOrder() { return order.get(); }
    public ObjectProperty<Order> orderProperty() { return order; }
    public void setOrder(Order value) { order.set(value); }

    public Product getProduct() { return product.get(); }
    public ObjectProperty<Product> productProperty() { return product; }
    public void setProduct(Product value) { product.set(value); }

    public int getQuantity() { return quantity.get(); }
    public IntegerProperty quantityProperty() { return quantity; }
    public void setQuantity(int value) { quantity.set(value); }

    public BigDecimal getUnitPrice() { return unitPrice.get(); }
    public ObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }
    public void setUnitPrice(BigDecimal value) { unitPrice.set(value); }

    public BigDecimal getSubtotal() { return subtotal.get(); }
    public ObjectProperty<BigDecimal> subtotalProperty() { return subtotal; }
    public void setSubtotal(BigDecimal value) { subtotal.set(value); }

    private void calculateSubtotal() {
        if (quantity.get() > 0 && unitPrice.get() != null) {
            subtotal.set(unitPrice.get().multiply(BigDecimal.valueOf(quantity.get())));
        }
    }
} 