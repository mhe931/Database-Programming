package com.inventory.service;

import com.inventory.dao.JpaRepository;
import com.inventory.entity.InventoryItem;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;

public final class InventoryService extends EntityService<InventoryItem> {
    private final EntityManagerFactory factory;

    public InventoryService(EntityManagerFactory factory) {
        super(new JpaRepository<>(factory, InventoryItem.class));
        this.factory = factory;
    }

    @Override
    public Optional<InventoryItem> findById(Object id) {
        return JpaRepository.inTransaction(factory, entityManager ->
                entityManager.createQuery("""
                                select item from InventoryItem item
                                join fetch item.product product
                                join fetch product.category
                                join fetch item.warehouse
                                where item.id = :id
                                """, InventoryItem.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst());
    }

    @Override
    public List<InventoryItem> findAll() {
        return JpaRepository.inTransaction(factory, entityManager ->
                entityManager.createQuery("""
                                select item from InventoryItem item
                                join fetch item.product product
                                join fetch product.category
                                join fetch item.warehouse
                                """, InventoryItem.class)
                        .getResultList());
    }

    public Optional<InventoryItem> findFor(long productId, long warehouseId) {
        return JpaRepository.inTransaction(factory, entityManager ->
                entityManager.createQuery("""
                                select item from InventoryItem item
                                join fetch item.product product
                                join fetch product.category
                                join fetch item.warehouse
                                where item.product.id = :productId and item.warehouse.id = :warehouseId
                                """, InventoryItem.class)
                        .setParameter("productId", productId)
                        .setParameter("warehouseId", warehouseId)
                        .getResultStream()
                        .findFirst());
    }

    public InventoryItem adjustQuantity(long itemId, int delta) {
        return JpaRepository.inTransaction(factory, entityManager -> {
            InventoryItem item = entityManager.createQuery("""
                            select item from InventoryItem item
                            join fetch item.product product
                            join fetch product.category
                            join fetch item.warehouse
                            where item.id = :id
                            """, InventoryItem.class)
                    .setParameter("id", itemId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (item == null) {
                throw new IllegalArgumentException("No inventory item with id " + itemId);
            }
            item.changeQuantity(delta);
            return item;
        });
    }
}
