package com.inventory.service;

import com.inventory.dao.JpaRepository;
import com.inventory.entity.Warehouse;
import jakarta.persistence.EntityManagerFactory;

public final class WarehouseService extends EntityService<Warehouse> {
    public WarehouseService(EntityManagerFactory factory) {
        super(new JpaRepository<>(factory, Warehouse.class));
    }
}
