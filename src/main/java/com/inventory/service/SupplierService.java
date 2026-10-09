package com.inventory.service;

import com.inventory.dao.JpaRepository;
import com.inventory.entity.Supplier;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;

public final class SupplierService extends EntityService<Supplier> {
    private final EntityManagerFactory factory;

    public SupplierService(EntityManagerFactory factory) {
        super(new JpaRepository<>(factory, Supplier.class));
        this.factory = factory;
    }

    @Override
    public Optional<Supplier> findById(Object id) {
        return JpaRepository.inTransaction(factory, entityManager ->
                entityManager.createQuery("""
                                select distinct s from Supplier s
                                left join fetch s.products p
                                left join fetch p.category
                                where s.id = :id
                                """, Supplier.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst());
    }

    @Override
    public List<Supplier> findAll() {
        return JpaRepository.inTransaction(factory, entityManager ->
                entityManager.createQuery("""
                                select distinct s from Supplier s
                                left join fetch s.products p
                                left join fetch p.category
                                """, Supplier.class)
                        .getResultList());
    }
}
