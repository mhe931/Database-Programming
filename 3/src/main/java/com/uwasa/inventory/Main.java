package com.uwasa.inventory;

import com.uwasa.inventory.entity.Category;
import com.uwasa.inventory.entity.Product;
import com.uwasa.inventory.service.InventoryService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("--- Starting Advanced Database Operations (One-to-Many) ---");
        System.out.println("=========================================================");
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("inventory-pu");
        InventoryService service = new InventoryService(emf);

        try {
            // ----- STEP 1: CREATE -----
            System.out.println("\n[STEP 1] Creating Categories and Products...");
            Category electronics = new Category("Electronics");
            electronics.addProduct(new Product("Laptop", 1200.00));
            electronics.addProduct(new Product("Smartphone", 800.00));
            service.createCategoryWithProducts(electronics);

            Category appliances = new Category("Home Appliances");
            appliances.addProduct(new Product("Microwave", 150.00));
            service.createCategoryWithProducts(appliances);

            Long electronicsId = electronics.getId();
            Long appliancesId = appliances.getId();

            // ----- STEP 2: READ (with Lazy Loading Handle) -----
            System.out.println("\n[STEP 2] Fetching Category with Products...");
            Category fetchedCategory = service.getCategoryWithProducts(electronicsId);
            System.out.println("Fetched Category: " + fetchedCategory.getName());
            for (Product p : fetchedCategory.getProducts()) {
                 System.out.println("  - " + p.getName() + " ($" + p.getPrice() + ")");
            }

            // ----- STEP 3: UPDATE -----
            System.out.println("\n[STEP 3] Moving a Product between Categories...");
            // Grab the microwave ID to pretend we are classifying it as an electronic device
            Long microwaveId = appliances.getProducts().get(0).getId();
            service.moveProductToCategory(microwaveId, electronicsId);
            
            // Verify move by re-fetching
            Category updatedElectronics = service.getCategoryWithProducts(electronicsId);
            System.out.println("Updated Electronics Products:");
            for (Product p : updatedElectronics.getProducts()) {
                System.out.println("  - " + p.getName());
            }

            // ----- STEP 4: DELETE (Cascade Validation) -----
            System.out.println("\n[STEP 4] Deleting Category to trigger Cascade Delete...");
            service.deleteCategory(electronicsId);

            // Double check it was actually deleted
            System.out.println("\nVerification: Trying to fetch deleted category...");
            Category deletedCategory = service.getCategoryWithProducts(electronicsId);
            if (deletedCategory == null) {
                 System.out.println(">>> Verified: Electronics Category and its associated Products are completely deleted.");
            }

        } finally {
            emf.close();
            System.out.println("\n=========================================================");
            System.out.println("--- Application Finished Successfully ---");
            System.out.println("=========================================================");
        }
    }
}
