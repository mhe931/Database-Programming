package com.inventory;

import com.inventory.dao.JpaRepository;
import com.inventory.dao.SqliteSchema;
import com.inventory.entity.Category;
import com.inventory.entity.InventoryItem;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.entity.Warehouse;
import com.inventory.service.CategoryService;
import com.inventory.service.InventoryService;
import com.inventory.service.ProductService;
import com.inventory.service.SupplierService;
import com.inventory.service.WarehouseService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.validation.ConstraintViolationException;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws SQLException {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        String databaseUrl = System.getProperty("inventory.jdbc.url",
                "jdbc:sqlite:inventory_db.sqlite?foreign_keys=on");
        SqliteSchema.initialize(databaseUrl);
        Map<String, Object> overrides = Map.of(
                "jakarta.persistence.jdbc.url", databaseUrl,
                "jakarta.persistence.schema-generation.database.action", "none");

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("InventoryPU", overrides);
        try {
            runDemonstration(factory, databaseUrl);
        } finally {
            factory.close();
        }
    }

    private static void runDemonstration(EntityManagerFactory factory, String databaseUrl) throws SQLException {
        CategoryService categories = new CategoryService(factory);
        ProductService products = new ProductService(factory);
        SupplierService suppliers = new SupplierService(factory);
        WarehouseService warehouses = new WarehouseService(factory);
        InventoryService inventory = new InventoryService(factory);

        Category category = categories.save(new Category("Café & Küche", "Básicos para el hogar"));
        Supplier supplier = suppliers.save(new Supplier("東京 Trading", "orders@example.test"));
        Warehouse warehouse = warehouses.save(new Warehouse("北倉庫", "北海道・札幌"));
        Product product = new Product("Café molido ☕", "CAF-001", "Mezcla de origen", new BigDecimal("8.75"), category);
        product.addSupplier(supplier);
        product = products.save(product);

        Category persianCategory = categories.save(new Category("ادویه‌های ایرانی", "نمونه‌ای خیالی از بازار تهران"));
        Supplier arabicSupplier = suppliers.save(new Supplier("شركة النور", "sales@example.test"));
        Warehouse arabicWarehouse = warehouses.save(new Warehouse("مستودع دبي", "دبي"));
        Product persianProduct = new Product("زعفران ایرانی", "SAF-001", "زعفران ممتاز",
                new BigDecimal("14.50"), persianCategory);
        persianProduct.addSupplier(arabicSupplier);
        persianProduct = products.save(persianProduct);

        InventoryItem stock = inventory.save(new InventoryItem(product, warehouse, 24));
        InventoryItem persianStock = inventory.save(new InventoryItem(persianProduct, arabicWarehouse, 12));
        System.out.println("Created: " + product + " supplied by " + supplier.getName());
        System.out.println("Persian/Arabic sample: " + persianProduct + " supplied by " + arabicSupplier.getName()
                + " at " + arabicWarehouse.getName());
        System.out.println("SKU lookup: " + products.findBySku("CAF-001").orElseThrow());
        System.out.println("Inventory: " + inventory.findFor(product.getId(), warehouse.getId()).orElseThrow());
        System.out.println("Persian/Arabic inventory: "
                + inventory.findFor(persianProduct.getId(), arabicWarehouse.getId()).orElseThrow());

        InventoryItem adjusted = inventory.adjustQuantity(stock.getId(), -3);
        System.out.println("After selling 3 units: quantity=" + adjusted.getQuantity());

        try {
            products.save(new Product(" ", "INVALID", null, new BigDecimal("-1"), category));
            throw new IllegalStateException("Invalid product unexpectedly passed validation");
        } catch (ConstraintViolationException expected) {
            System.out.println("Validation rejected an invalid product.");
        }

        verifyForeignKeyRejection(databaseUrl, product.getId());
        demonstrateRollback(factory);

        inventory.deleteById(stock.getId());
        inventory.deleteById(persianStock.getId());
        products.deleteById(product.getId());
        products.deleteById(persianProduct.getId());
        suppliers.deleteById(supplier.getId());
        suppliers.deleteById(arabicSupplier.getId());
        categories.deleteById(category.getId());
        categories.deleteById(persianCategory.getId());
        warehouses.deleteById(warehouse.getId());
        warehouses.deleteById(arabicWarehouse.getId());
        System.out.println("Demo records removed; application resources closed by the caller.");
    }

    private static void verifyForeignKeyRejection(String databaseUrl, long productId) throws SQLException {
        try (var connection = DriverManager.getConnection(databaseUrl);
             var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("PRAGMA foreign_keys")) {
                if (!result.next() || result.getInt(1) != 1) {
                    throw new IllegalStateException("SQLite foreign-key enforcement is disabled");
                }
            }
            try {
                statement.executeUpdate("INSERT INTO product_suppliers(product_id, supplier_id) VALUES ("
                        + productId + ", 9223372036854775807)");
                throw new IllegalStateException("Invalid supplier foreign key unexpectedly succeeded");
            } catch (SQLException expected) {
                if (expected.getMessage() == null
                        || !expected.getMessage().toLowerCase(Locale.ROOT).contains("foreign key")) {
                    throw expected;
                }
                System.out.println("SQLite rejected an invalid supplier foreign key.");
            }
        }
    }

    private static void demonstrateRollback(EntityManagerFactory factory) {
        try {
            JpaRepository.inTransaction(factory, entityManager -> {
                entityManager.persist(new Category("Temporary rollback row", null));
                entityManager.flush();
                throw new IllegalStateException("Demonstration rollback");
            });
        } catch (IllegalStateException expected) {
            if (!expected.getMessage().equals("Demonstration rollback")) {
                throw expected;
            }
            System.out.println("A deliberate failure rolled back its pending write.");
        }
        boolean absent = new CategoryService(factory).findAll().stream()
                .noneMatch(category -> category.getName().equals("Temporary rollback row"));
        if (!absent) {
            throw new IllegalStateException("Rollback left a category behind");
        }
    }
}
