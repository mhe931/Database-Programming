package com.inventory.service;

import com.inventory.dao.JpaRepository;
import com.inventory.entity.Product;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;

public final class ProductService extends EntityService<Product> {
    private final EntityManagerFactory factory;

    public ProductService(EntityManagerFactory factory) {
        super(new JpaRepository<>(factory, Product.class));
        this.factory = factory;
    }

    public Optional<Product> findBySku(String sku) {
        return JpaRepository.inTransaction(factory, entityManager ->
                entityManager.createQuery(
                                "select distinct p from Product p left join fetch p.suppliers where p.sku = :sku",
                                Product.class)
                        .setParameter("sku", sku)
                        .getResultStream()
                        .findFirst());
    }
}
