package com.inventory.service;

import com.inventory.dao.JpaRepository;
import com.inventory.entity.InventoryItem;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;

public final class InventoryService extends EntityService<InventoryItem> {
    private final EntityManagerFactory factory;

    public InventoryService(EntityManagerFactory factory) {
        super(new JpaRepository<>(factory, InventoryItem.class));
        this.factory = factory;
    }

    public Optional<InventoryItem> findFor(long productId, long warehouseId) {
        return JpaRepository.inTransaction(factory, entityManager ->
                entityManager.createQuery("""
                                select item from InventoryItem item
                                where item.product.id = :productId and item.warehouse.id = :warehouseId
                                """, InventoryItem.class)
                        .setParameter("productId", productId)
                        .setParameter("warehouseId", warehouseId)
                        .getResultStream()
                        .findFirst());
    }

    public InventoryItem adjustQuantity(long itemId, int delta) {
        return JpaRepository.inTransaction(factory, entityManager -> {
            InventoryItem item = entityManager.find(InventoryItem.class, itemId);
            if (item == null) {
                throw new IllegalArgumentException("No inventory item with id " + itemId);
            }
            item.changeQuantity(delta);
            return item;
        });
    }
}
