package com.inventory.jpa;

import com.inventory.jpa.dao.ProductDAO;
import com.inventory.jpa.entity.Product;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bootstraps the persistence context, creating the EntityManagerFactory
 * and validating the CRUD mapping on the 'products' SQLite table.
 */
public class Main {

    public static void main(String[] args) {
        // Disable overly verbose Hibernate logging to preserve clean console output
        Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);

        System.out.println("===================================================================");
        System.out.println("           JPA / Hibernate CRUD Operations Demo");
        System.out.println("===================================================================");

        EntityManagerFactory emf = null;
        try {
            System.out.println("[BOOTSTRAP] Initializing EntityManagerFactory...");
            emf = Persistence.createEntityManagerFactory("inventory-pu");
            ProductDAO dao = new ProductDAO(emf);

            System.out.println("\n[1] --- INSERT OPERATIONS ---");
            dao.createProduct("Ergonomic Chair", "Furniture", 299.50);
            dao.createProduct("Bluetooth Adapter", "Electronics", 15.00);
            dao.createProduct("Mechanical Keyboard", "Electronics", 120.00);

            System.out.println("\n[2] --- READ (SELECT ALL) ---");
            for (Product p : dao.getAllProducts()) {
                System.out.println("    FOUND -> " + p.toString());
            }

            System.out.println("\n[3] --- UPDATE OPERATION ---");
            // Change product ID 2 price and category
            dao.updateProduct(2L, "Accessories", 18.75);

            System.out.println("\n[4] --- SELECT AFTER UPDATE ---");
            for (Product p : dao.getAllProducts()) {
                System.out.println("    FOUND -> " + p.toString());
            }

            System.out.println("\n[5] --- DELETE OPERATION ---");
            dao.deleteProduct(3L);

            System.out.println("\n[6] --- FINAL STATE (SELECT ALL) ---");
            for (Product p : dao.getAllProducts()) {
                System.out.println("    FOUND -> " + p.toString());
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Exception occurred during CRUD lifecycle: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (emf != null && emf.isOpen()) {
                emf.close();
                System.out.println("\n[BOOTSTRAP] EntityManagerFactory gracefully closed.");
            }
        }
    }
}
