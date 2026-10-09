package com.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(name = "uk_products_sku", columnNames = "sku"))
@Check(constraints = "price >= 0")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String name;

    @NotBlank
    @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String sku;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToMany
    @JoinTable(name = "product_suppliers",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "supplier_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_product_supplier", columnNames = {"product_id", "supplier_id"}))
    private Set<Supplier> suppliers = new LinkedHashSet<>();

    protected Product() {
    }

    public Product(String name, String sku, String description, BigDecimal price, Category category) {
        this.name = name;
        this.sku = sku;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public void addSupplier(Supplier supplier) {
        suppliers.add(supplier);
        supplier.getProducts().add(this);
    }

    public void removeSupplier(Supplier supplier) {
        suppliers.remove(supplier);
        supplier.getProducts().remove(this);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Category getCategory() { return category; }
    public Set<Supplier> getSuppliers() { return suppliers; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", sku='" + sku + "', name='" + name + "', price=" + price + "}";
    }
}
