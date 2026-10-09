package com.inventory.service;

import com.inventory.dao.JpaRepository;
import com.inventory.entity.Supplier;
import jakarta.persistence.EntityManagerFactory;

public final class SupplierService extends EntityService<Supplier> {
    public SupplierService(EntityManagerFactory factory) {
        super(new JpaRepository<>(factory, Supplier.class));
    }
}
