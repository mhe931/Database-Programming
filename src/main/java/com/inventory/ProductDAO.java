package com.inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO (Data Access Object) encapsulates all SQL logic for CRUD operations
 * on the 'products' table. Every method uses try-with-resources for safe
 * connection and resource management.
 */
public class ProductDAO {

    // ──────────────────────────────────────────────
    //  CREATE
    // ──────────────────────────────────────────────

    /**
     * Inserts a new product into the database.
     *
     * @param name     the product name
     * @param category the product category
     * @param price    the product price
     */
    public void createProduct(String name, String category, double price) {
        String sql = "INSERT INTO products (name, category, price) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();

            System.out.printf("[CREATE] Product added: %s | %s | %.2f%n", name, category, price);

        } catch (SQLException e) {
            System.err.println("[CREATE] Error: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  READ
    // ──────────────────────────────────────────────

    /**
     * Retrieves all products from the database and prints them.
     *
     * @return a list of formatted strings representing each product
     */
    public List<String> getAllProducts() {
        String sql = "SELECT id, name, category, price FROM products";
        List<String> products = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String category = rs.getString("category");
                double price = rs.getDouble("price");

                String record = String.format("  ID: %d | Name: %-15s | Category: %-12s | Price: %.2f",
                        id, name, category, price);
                products.add(record);
            }

        } catch (SQLException e) {
            System.err.println("[READ] Error: " + e.getMessage());
        }

        return products;
    }

    // ──────────────────────────────────────────────
    //  UPDATE
    // ──────────────────────────────────────────────

    /**
     * Updates the price of an existing product identified by its ID.
     *
     * @param id       the product ID
     * @param newPrice the new price to set
     */
    public void updateProductPrice(int id, double newPrice) {
        String sql = "UPDATE products SET price = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.printf("[UPDATE] Product ID %d — new price: %.2f%n", id, newPrice);
            } else {
                System.out.printf("[UPDATE] No product found with ID %d.%n", id);
            }

        } catch (SQLException e) {
            System.err.println("[UPDATE] Error: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  DELETE
    // ──────────────────────────────────────────────

    /**
     * Deletes a product from the database by its ID.
     *
     * @param id the product ID to delete
     */
    public void deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.printf("[DELETE] Product ID %d removed.%n", id);
            } else {
                System.out.printf("[DELETE] No product found with ID %d.%n", id);
            }

        } catch (SQLException e) {
            System.err.println("[DELETE] Error: " + e.getMessage());
        }
    }
}
