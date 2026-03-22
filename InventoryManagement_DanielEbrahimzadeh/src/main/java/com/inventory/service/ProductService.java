package com.inventory.service;

import com.inventory.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Service architect integrating the raw EntityManager operations.
 * Isolates data access and transaction demarcations from the application execution layer.
 */
public class ProductService {

    private final EntityManagerFactory emf;

    public ProductService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Persists new inventory items.
     */
    public void createProduct(String name, String category, Double price, Integer stockQuantity) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            
            Product product = new Product(name, category, price, stockQuantity);
            em.persist(product);
            
            tx.commit();
            System.out.printf("[CREATE] %s%n", product.toString());
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("[ERROR] Failed to persist entity: " + e.getMessage());
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Retrieves all products executing JPQL over the object model.
     */
    public List<Product> getAllProducts() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            TypedQuery<Product> query = em.createQuery("SELECT p FROM Product p", Product.class);
            return query.getResultList();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Modifies stock levels and pricing on an existing product ID utilizing dirty checking.
     */
    public void updateProduct(Long id, Double newPrice, Integer newStock) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            
            Product product = em.find(Product.class, id);
            if (product != null) {
                product.setPrice(newPrice);
                product.setStockQuantity(newStock);
                tx.commit();
                System.out.printf("[UPDATE] ID %d modified -> %s%n", id, product.toString());
            } else {
                System.out.printf("[UPDATE] Product ID %d not found. Modification aborted.%n", id);
                tx.rollback();
            }
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("[ERROR] Failed to update entity: " + e.getMessage());
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Drops discontinued items from the physical data layer utilizing remove().
     */
    public void deleteProduct(Long id) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            
            Product product = em.find(Product.class, id);
            if (product != null) {
                em.remove(product);
                tx.commit();
                System.out.printf("[DELETE] Discontinued Product ID %d successfully scrubbed from database.%n", id);
            } else {
                System.out.printf("[DELETE] Product ID %d not found. Deletion aborted.%n", id);
                tx.rollback();
            }
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("[ERROR] Failed to delete entity: " + e.getMessage());
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
