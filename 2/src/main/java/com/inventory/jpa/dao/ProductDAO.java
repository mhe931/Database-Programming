package com.inventory.jpa.dao;

import com.inventory.jpa.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;

/**
 * Data Access Object abstracting JPA interactions handling the EntityManager.
 */
public class ProductDAO {

    private final EntityManagerFactory emf;

    public ProductDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Creates and persists a new Product entity into the database.
     */
    public void createProduct(String name, String category, Double price) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product product = new Product(name, category, price);
            em.persist(product);
            tx.commit();
            System.out.printf("[CREATE] %s%n", product.toString());
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves a Product by its Primary Key ID.
     */
    public Product getProduct(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Product.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Executes JPQL to retrieve all persisted Product records.
     */
    public List<Product> getAllProducts() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Updates an existing product's category and price.
     */
    public void updateProduct(Long id, String newCategory, Double newPrice) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product product = em.find(Product.class, id);
            if (product != null) {
                product.setCategory(newCategory);
                product.setPrice(newPrice);
                // JPA automatically detects state changes upon commit (Dirty Checking)
                tx.commit();
                System.out.printf("[UPDATE] ID %d updated -> %s%n", id, product.toString());
            } else {
                System.out.printf("[UPDATE] Product ID %d not found.%n", id);
                tx.rollback();
            }
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Removes an entity from the database using its ID.
     */
    public void deleteProduct(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product product = em.find(Product.class, id);
            if (product != null) {
                em.remove(product);
                tx.commit();
                System.out.printf("[DELETE] Product ID %d removed.%n", id);
            } else {
                System.out.printf("[DELETE] Product ID %d not found.%n", id);
                tx.rollback();
            }
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
