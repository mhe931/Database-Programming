package com.inventory;

import com.inventory.entity.Product;
import com.inventory.service.ProductService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bootstraps the application, executing a full CRUD lifecycle and printing the state.
 */
public class Main {

    public static void main(String[] args) {
        Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);

        System.out.println("=======================================================================");
        System.out.println("          JPA / Hibernate Implementation Phase");
        System.out.println("=======================================================================\n");

        EntityManagerFactory emf = null;
        try {
            System.out.println("[INIT] Creating EntityManagerFactory...");
            emf = Persistence.createEntityManagerFactory("InventoryPU");
            ProductService service = new ProductService(emf);

            System.out.println("\n--- Step 1: Saving Products ---");
            service.saveProduct(new Product("Ultra-wide Monitor", "Hardware", 849.00, 42));
            service.saveProduct(new Product("Developer Keyboard", "Peripherals", 129.50, 105));
            service.saveProduct(new Product("Ergo Standing Desk", "Furniture", 550.00, 18));
            printDatabaseState(service);

            System.out.println("\n--- Step 2: Finding Product by ID (ID=2) ---");
            Product p = service.findById(2L);
            System.out.println("Found: " + (p != null ? p.toString() : "null"));

            System.out.println("\n--- Step 3: Updating Price (ID=2 to 115.00) ---");
            service.updatePrice(2L, 115.00);
            printDatabaseState(service);

            System.out.println("\n--- Step 4: Removing Product (ID=3) ---");
            service.removeProduct(3L);
            printDatabaseState(service);

        } catch (Exception e) {
            System.err.println("[ERROR] Exception occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (emf != null && emf.isOpen()) {
                emf.close();
                System.out.println("\n[TEARDOWN] EntityManagerFactory closed successfully.");
            }
        }
    }

    private static void printDatabaseState(ProductService service) {
        System.out.println("\n>>> Current Database State:");
        java.util.List<Product> products = service.findAll();
        if (products.isEmpty()) {
            System.out.println("    (Empty)");
        } else {
            for (Product pro : products) {
                System.out.println("    " + pro.toString());
            }
        }
        System.out.println("<<<");
    }
}
