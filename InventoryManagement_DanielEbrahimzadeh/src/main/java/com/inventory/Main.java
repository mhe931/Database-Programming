package com.inventory;

import com.inventory.entity.Product;
import com.inventory.service.ProductService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise gateway acting as the execution bootstrapper for the Inventory Management System.
 * Orchestrates the full architectural lifecycle over the persistence context unit.
 */
public class Main {

    public static void main(String[] args) {
        // Suppressing highly granular built-in Hibernate logs to preserve console legibility
        Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);

        System.out.println("=======================================================================");
        System.out.println("        Inventory Management System — JPA Validation Subroutine");
        System.out.println("=======================================================================\n");

        EntityManagerFactory emf = null;
        
        try {
            System.out.println("[BOOTSTRAP] Spawning EntityManagerFactory [InventoryPU]...");
            // Initializes the connection pool and the SQLite physical deployment 
            emf = Persistence.createEntityManagerFactory("InventoryPU");
            ProductService service = new ProductService(emf);

            System.out.println("\n[1] --- SYSTEM INGESTION (CREATE) ---");
            service.createProduct("Ultra-wide Monitor", "Hardware", 849.00, 42);
            service.createProduct("Developer Keyboard", "Peripherals", 129.50, 105);
            service.createProduct("Ergo Standing Desk", "Furniture", 550.00, 18);
            service.createProduct("Optical Gaming Mouse", "Peripherals", 65.00, 210);

            System.out.println("\n[2] --- INVENTORY AUDIT (READ ALL) ---");
            for (Product p : service.getAllProducts()) {
                System.out.println("    AUDIT -> " + p.toString());
            }

            System.out.println("\n[3] --- INVENTORY FLIGHT (UPDATE) ---");
            // The Development Keyboard dropped in price, stock expanded
            service.updateProduct(2L, 115.00, 140);

            System.out.println("\n[4] --- AUDIT POST-FLIGHT ---");
            for (Product p : service.getAllProducts()) {
                System.out.println("    AUDIT -> " + p.toString());
            }

            System.out.println("\n[5] --- SYSTEM SCRUB (DELETE) ---");
            // End-of-life for the Standing Desk
            service.deleteProduct(3L);

            System.out.println("\n[6] --- FINAL STATE AUDIT (READ ALL) ---");
            for (Product p : service.getAllProducts()) {
                System.out.println("    AUDIT -> " + p.toString());
            }

        } catch (Exception e) {
            System.err.println("\n[CRITICAL FAULT] Fatal execution disruption: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Graceful shutdown protocol guaranteeing socket locks inside SQLite dissipate
            if (emf != null && emf.isOpen()) {
                emf.close();
                System.out.println("\n[BOOTSTRAP] EntityManagerFactory gracefully collapsed. Execution resolved safely.");
            }
        }
    }
}
