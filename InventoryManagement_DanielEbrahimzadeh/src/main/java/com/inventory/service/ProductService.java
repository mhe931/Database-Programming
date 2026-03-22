package com.inventory.service;

import com.inventory.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

/**
 * Service class implementing CRUD operations for Product entities using EntityManager.
 */
public class ProductService {

    private final EntityManagerFactory emf;

    public ProductService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void saveProduct(Product p) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            em.persist(p);
            tx.commit();
            System.out.printf("[CREATE] Saved: %s%n", p.toString());
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        } finally {
            if (em != null) em.close();
        }
    }

    public Product findById(Long id) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            return em.find(Product.class, id);
        } finally {
            if (em != null) em.close();
        }
    }

    public java.util.List<Product> findAll() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            return em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

    public void updatePrice(Long id, double newPrice) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            
            Product product = em.find(Product.class, id);
            if (product != null) {
                product.setPrice(newPrice);
                tx.commit();
                System.out.printf("[UPDATE] Price modified to %.2f for ID %d%n", newPrice, id);
            } else {
                System.out.printf("[UPDATE] Product ID %d not found.%n", id);
                tx.rollback();
            }
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        } finally {
            if (em != null) em.close();
        }
    }

    public void removeProduct(Long id) {
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
                System.out.printf("[DELETE] Removed Product ID %d%n", id);
            } else {
                System.out.printf("[DELETE] Product ID %d not found.%n", id);
                tx.rollback();
            }
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        } finally {
            if (em != null) em.close();
        }
    }
}
