package com.inventory.service;

import com.inventory.dao.JpaRepository;
import com.inventory.entity.Product;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;

public final class ProductService extends EntityService<Product> {
    private final EntityManagerFactory factory;

    public ProductService(EntityManagerFactory factory) {
        super(new JpaRepository<>(factory, Product.class));
        this.factory = factory;
    }

    @Override
    public Optional<Product> findById(Object id) {
        return JpaRepository.inTransaction(factory, entityManager ->
                entityManager.createQuery("""
                                select distinct p from Product p
                                left join fetch p.category
                                left join fetch p.suppliers
                                where p.id = :id
                                """, Product.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst());
    }

    @Override
    public List<Product> findAll() {
        return JpaRepository.inTransaction(factory, entityManager ->
                entityManager.createQuery("""
                                select distinct p from Product p
                                left join fetch p.category
                                left join fetch p.suppliers
                                """, Product.class)
                        .getResultList());
    }

    public Optional<Product> findBySku(String sku) {
        return JpaRepository.inTransaction(factory, entityManager ->
                entityManager.createQuery(
                                """
                                select distinct p from Product p
                                left join fetch p.category
                                left join fetch p.suppliers
                                where p.sku = :sku
                                """,
                                Product.class)
                        .setParameter("sku", sku)
                        .getResultStream()
                        .findFirst());
    }
}
