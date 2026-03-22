package com.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Domain entity mapping representing the 'products' table.
 * Eschewing the generic 'Employee' analogy, this entity models quantitative 
 * inventory metrics essential for e-commerce and point-of-sale paradigms.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private Double price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    // Zero-argument constructor mandated by the JPA specification
    public Product() {}

    public Product(String name, String category, Double price, Integer stockQuantity) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // Accessors and Mutators

    public Long getId() {
        return id;
    }

    // Standard practice dictates the ORM assigns the ID natively, but exposed for edge cases.
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    @Override
    public String toString() {
        return String.format(
            "Product [ID=%d, Name=%-20s, Category=%-15s, Price=%7.2f, Stock=%4d]", 
            id, name, category, price, stockQuantity
        );
    }
}
