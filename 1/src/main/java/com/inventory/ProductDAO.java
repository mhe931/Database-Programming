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
 *
 * Input validation is performed before database calls to reject invalid data
 * early. Error logging includes SQL state and error codes for debugging.
 */
public class ProductDAO {

    // ──────────────────────────────────────────────
    //  CREATE
    // ──────────────────────────────────────────────

    /**
     * Inserts a new product into the database.
     * Validates that the price is not negative before executing the insert.
     *
     * @param name     the product name
     * @param category the product category
     * @param price    the product price (must be >= 0)
     */
    public void createProduct(String name, String category, double price) {
        if (price < 0) {
            System.err.printf("[CREATE] Rejected: price cannot be negative (%.2f) for '%s'.%n", price, name);
            return;
        }

        String sql = "INSERT INTO products (name, category, price) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();

            System.out.printf("[CREATE] Product added: %s | %s | %.2f%n", name, category, price);

        } catch (SQLException e) {
            System.err.printf("[CREATE] SQL error inserting '%s': State=%s | Code=%d | %s%n",
                    name, e.getSQLState(), e.getErrorCode(), e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  READ
    // ──────────────────────────────────────────────

    /**
     * Retrieves all products from the database.
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
            System.err.printf("[READ] SQL error fetching products: State=%s | Code=%d | %s%n",
                    e.getSQLState(), e.getErrorCode(), e.getMessage());
        }

        return products;
    }

    // ──────────────────────────────────────────────
    //  UPDATE
    // ──────────────────────────────────────────────

    /**
     * Updates the price of an existing product identified by its ID.
     * Validates that the new price is not negative before executing the update.
     *
     * @param id       the product ID
     * @param newPrice the new price to set (must be >= 0)
     */
    public void updateProductPrice(int id, double newPrice) {
        if (newPrice < 0) {
            System.err.printf("[UPDATE] Rejected: price cannot be negative (%.2f) for ID %d.%n", newPrice, id);
            return;
        }

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
            System.err.printf("[UPDATE] SQL error updating ID %d: State=%s | Code=%d | %s%n",
                    id, e.getSQLState(), e.getErrorCode(), e.getMessage());
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
            System.err.printf("[DELETE] SQL error deleting ID %d: State=%s | Code=%d | %s%n",
                    id, e.getSQLState(), e.getErrorCode(), e.getMessage());
        }
    }
}
