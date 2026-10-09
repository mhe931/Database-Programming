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
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InventoryPersistenceTest {
    @TempDir
    Path temporaryDirectory;

    private EntityManagerFactory factory;
    private CategoryService categories;
    private ProductService products;
    private SupplierService suppliers;
    private WarehouseService warehouses;
    private InventoryService inventory;

    @BeforeEach
    void openDatabase() {
        String database = temporaryDirectory.resolve("inventory-test.sqlite").toString().replace('\\', '/');
        String jdbcUrl = "jdbc:sqlite:" + database + "?foreign_keys=on";
        SqliteSchema.initialize(jdbcUrl);
        factory = Persistence.createEntityManagerFactory("InventoryPU", Map.of(
                "jakarta.persistence.jdbc.url", jdbcUrl,
                "jakarta.persistence.schema-generation.database.action", "none"));
        categories = new CategoryService(factory);
        products = new ProductService(factory);
        suppliers = new SupplierService(factory);
        warehouses = new WarehouseService(factory);
        inventory = new InventoryService(factory);
    }

    @AfterEach
    void closeDatabase() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }

    @Test
    void validatesInputAndPersistsAllEntityRelationships() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();
            assertFalse(validator.validate(new Warehouse(" ", "")).isEmpty());
            assertFalse(validator.validate(new Supplier("Vendor", "not-an-email")).isEmpty());
        }

        Category category = categories.save(new Category("Café", "世界の調味料"));
        assertThrows(ConstraintViolationException.class, () ->
                products.save(new Product(" ", "INVALID", null, new BigDecimal("-1"), category)));
        Supplier supplierA = suppliers.save(new Supplier("São Paulo Supply", "one@example.test"));
        Supplier supplierB = suppliers.save(new Supplier("東京商会", "two@example.test"));
        Warehouse warehouse = warehouses.save(new Warehouse("Entrepôt Montréal", "Montréal, Québec"));
        Product product = new Product("Café crème", "CAF-001", "Crème brûlée ingredients",
                new BigDecimal("12.40"), category);
        product.addSupplier(supplierA);
        product.addSupplier(supplierB);
        product = products.save(product);
        InventoryItem item = inventory.save(new InventoryItem(product, warehouse, 17));

        Product reread = products.findBySku("CAF-001").orElseThrow();
        assertEquals("Café crème", reread.getName());
        var supplierNames = reread.getSuppliers().stream().map(Supplier::getName)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(supplierNames.containsAll(java.util.Set.of("São Paulo Supply", "東京商会")));
        assertEquals("世界の調味料", categories.findById(category.getId()).orElseThrow().getDescription());
        try (var entityManager = factory.createEntityManager()) {
            assertEquals(2, entityManager
                    .createQuery("select count(s) from Product p join p.suppliers s where p.sku = :sku", Long.class)
                    .setParameter("sku", "CAF-001").getSingleResult());
        }
        assertEquals(17, inventory.findFor(product.getId(), warehouse.getId()).orElseThrow().getQuantity());
        assertEquals(item.getId(), inventory.adjustQuantity(item.getId(), 5).getId());
        assertEquals(22, inventory.findById(item.getId()).orElseThrow().getQuantity());
        assertTrue(inventory.deleteById(item.getId()));
        assertTrue(products.deleteById(product.getId()));
        assertFalse(products.deleteById(product.getId()));
    }

    @Test
    void sqliteEnforcesForeignKeysAndDatabaseConstraints() throws Exception {
        Category category = categories.save(new Category("Hardware", null));
        Supplier supplier = suppliers.save(new Supplier("Vendor", null));
        Warehouse warehouse = warehouses.save(new Warehouse("Test warehouse", "Test location"));
        Product product = products.save(new Product("Bolt", "BLT-01", null, new BigDecimal("0.50"), category));

        String database = temporaryDirectory.resolve("inventory-test.sqlite").toString().replace('\\', '/');
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database + "?foreign_keys=on");
             var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("PRAGMA foreign_keys")) {
                assertTrue(result.next());
                assertEquals(1, result.getInt(1));
            }

            SQLException foreignKeyFailure = assertThrows(SQLException.class, () -> statement.executeUpdate(
                    "INSERT INTO product_suppliers(product_id, supplier_id) VALUES ("
                            + product.getId() + ", 987654321)"));
            assertTrue(foreignKeyFailure.getMessage().toLowerCase(java.util.Locale.ROOT).contains("foreign key"));
            assertThrows(SQLException.class, () -> statement.executeUpdate(
                    "INSERT INTO products(name, sku, price, category_id) VALUES ('Bad', 'BAD', -1, "
                            + category.getId() + ")"));
            assertThrows(SQLException.class, () -> statement.executeUpdate(
                    "INSERT INTO inventory_items(product_id, warehouse_id, quantity) VALUES ("
                            + product.getId() + ", " + warehouse.getId() + ", -1)"));
            statement.executeUpdate("INSERT INTO product_suppliers(product_id, supplier_id) VALUES ("
                    + product.getId() + ", " + supplier.getId() + ")");
            assertThrows(SQLException.class, () -> statement.executeUpdate(
                    "INSERT INTO product_suppliers(product_id, supplier_id) VALUES ("
                            + product.getId() + ", " + supplier.getId() + ")"));
        }
    }

    @Test
    void schemaArtifactCreatesExpectedTablesAndPreservesUnicode() throws Exception {
        Category category = categories.save(new Category("中文类别", "Crème brûlée – 東京"));
        String database = temporaryDirectory.resolve("inventory-test.sqlite").toString().replace('\\', '/');
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database + "?foreign_keys=on");
             var statement = connection.createStatement()) {
            var tables = statement.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name");
            var names = new java.util.ArrayList<String>();
            while (tables.next()) {
                names.add(tables.getString(1));
            }
            assertEquals(java.util.List.of("categories", "inventory_items", "product_suppliers", "products",
                    "suppliers", "warehouses"), names);
            var foreignKeys = statement.executeQuery("PRAGMA foreign_key_list(products)");
            assertTrue(foreignKeys.next());
            assertEquals("categories", foreignKeys.getString("table"));
            var unicode = statement.executeQuery("SELECT description FROM categories WHERE id = " + category.getId());
            assertTrue(unicode.next());
            assertEquals("Crème brûlée – 東京", unicode.getString(1));
        }
    }

    @Test
    void failedTransactionRollsBackAlreadyFlushedWrites() {
        assertThrows(IllegalStateException.class, () -> JpaRepository.inTransaction(factory, entityManager -> {
            entityManager.persist(new Category("Should be rolled back", null));
            entityManager.flush();
            throw new IllegalStateException("test rollback");
        }));
        assertTrue(categories.findAll().isEmpty());
    }

    @Test
    void stockCannotBeMadeNegativeAndCreatesUniquelyPerLocation() {
        Category category = categories.save(new Category("Food", null));
        Product product = products.save(new Product("Tea", "TEA-1", null, new BigDecimal("3.10"), category));
        Warehouse warehouse = warehouses.save(new Warehouse("Main", "Oslo"));
        InventoryItem item = inventory.save(new InventoryItem(product, warehouse, 2));

        assertThrows(IllegalArgumentException.class, () -> inventory.adjustQuantity(item.getId(), -3));
        assertEquals(2, inventory.findById(item.getId()).orElseThrow().getQuantity());
        assertThrows(PersistenceException.class, () -> inventory.save(new InventoryItem(product, warehouse, 1)));
    }
}
