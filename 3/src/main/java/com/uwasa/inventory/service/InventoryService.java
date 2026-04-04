package com.uwasa.inventory.service;

import com.uwasa.inventory.entity.Category;
import com.uwasa.inventory.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class InventoryService {

    private final EntityManagerFactory emf;

    public InventoryService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    // 1. Create: Save a Category and its List of Products in one transaction
    public void createCategoryWithProducts(Category category) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(category);
            em.getTransaction().commit();
            System.out.println(">>> SUCCESS: Created Category with Products: " + category.getName());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    // 2. Read: Fetch a Category and its associated Products (Handling Lazy Loading)
    public Category getCategoryWithProducts(Long categoryId) {
        EntityManager em = emf.createEntityManager();
        try {
            Category category = em.find(Category.class, categoryId);
            if (category != null) {
                // Handle the Lazy Loading before the entity manager is closed.
                // Requesting size() forces Hibernate to execute the select query for the products map.
                category.getProducts().size(); 
            }
            return category;
        } finally {
            em.close();
        }
    }

    // 3. Update: Move a Product from one Category to another
    public void moveProductToCategory(Long productId, Long newCategoryId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Product product = em.find(Product.class, productId);
            Category newCategory = em.find(Category.class, newCategoryId);
            
            if (product != null && newCategory != null) {
                // Ensure bidirectional relationship is properly updated
                Category oldCategory = product.getCategory();
                if (oldCategory != null) {
                    oldCategory.getProducts().remove(product);
                }
                newCategory.addProduct(product);
                
                // Merge changes
                em.merge(product);
                em.merge(newCategory);
            }
            
            em.getTransaction().commit();
            System.out.println(">>> SUCCESS: Moved Product ID " + productId + " to Category ID " + newCategoryId);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    // 4. Delete: Remove a Category and demonstrate cascading affects
    public void deleteCategory(Long categoryId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Category category = em.find(Category.class, categoryId);
            if (category != null) {
                em.remove(category);
            }
            em.getTransaction().commit();
            System.out.println(">>> SUCCESS: Deleted Category ID " + categoryId + " (cascading deletes to products triggered)");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
