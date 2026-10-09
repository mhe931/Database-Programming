package com.inventory.service;

import com.inventory.dao.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Common CRUD operations shared by the five entity-specific services. */
public class EntityService<T> {
    protected final JpaRepository<T> repository;

    protected EntityService(JpaRepository<T> repository) {
        this.repository = repository;
    }

    public T save(T entity) { return repository.save(entity); }
    public Optional<T> findById(Object id) { return repository.findById(id); }
    public List<T> findAll() { return repository.findAll(); }
    public boolean deleteById(Object id) { return repository.deleteById(id); }
}
