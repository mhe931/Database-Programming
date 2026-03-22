package com.inventory;

import java.util.List;

/**
 * Main application demonstrating the full CRUD lifecycle:
 *   1. Initialize the database and create the products table.
 *   2. INSERT sample products.
 *   3. READ and display all products.
 *   4. UPDATE a product's price.
 *   5. DELETE a product.
 *   6. READ again to confirm final state.
 */
public class Main {

    public static void main(String[] args) {

        // ── Step 1: Initialize the database ──────────────────────
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("       Product Inventory — JDBC CRUD Demo");
        System.out.println("═══════════════════════════════════════════════════════\n");

        DatabaseConfig.initializeDatabase();

        ProductDAO dao = new ProductDAO();

        // ── Step 2: CREATE — Insert sample products ──────────────
        System.out.println("\n── INSERT Operations ──────────────────────────────────");
        dao.createProduct("Wireless Mouse",    "Electronics", 29.99);
        dao.createProduct("USB-C Hub",         "Electronics", 49.95);
        dao.createProduct("Standing Desk",     "Furniture",   349.00);
        dao.createProduct("Mechanical Keyboard", "Electronics", 89.50);
        dao.createProduct("Monitor Arm",       "Accessories", 44.75);

        // Validation demo — negative price should be rejected
        dao.createProduct("Bad Product",       "Test",        -9.99);

        // ── Step 3: READ — Display all products ──────────────────
        System.out.println("\n── SELECT ALL Products ────────────────────────────────");
        printProducts(dao.getAllProducts());

        // ── Step 4: UPDATE — Change the price of product ID 1 ────
        System.out.println("\n── UPDATE Operation ───────────────────────────────────");
        dao.updateProductPrice(1, 24.99);

        System.out.println("\n── Products after UPDATE ──────────────────────────────");
        printProducts(dao.getAllProducts());

        // ── Step 5: DELETE — Remove product ID 3 ─────────────────
        System.out.println("\n── DELETE Operation ───────────────────────────────────");
        dao.deleteProduct(3);

        System.out.println("\n── Products after DELETE ──────────────────────────────");
        printProducts(dao.getAllProducts());

        // ── Done ─────────────────────────────────────────────────
        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("       CRUD lifecycle complete.");
        System.out.println("═══════════════════════════════════════════════════════");
    }

    /**
     * Helper method to print the list of products or a "no products" message.
     */
    private static void printProducts(List<String> products) {
        if (products.isEmpty()) {
            System.out.println("  (no products found)");
        } else {
            products.forEach(System.out::println);
        }
    }
}
