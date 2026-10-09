package com.inventory.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Small transaction-per-operation repository for the course application.
 * Callers may also group related writes with {@link #inTransaction}.
 */
public class JpaRepository<T> {
    private final EntityManagerFactory entityManagerFactory;
    private final Class<T> entityType;

    public JpaRepository(EntityManagerFactory entityManagerFactory, Class<T> entityType) {
        this.entityManagerFactory = entityManagerFactory;
        this.entityType = entityType;
    }

    public T save(T entity) {
        return inTransaction(entityManagerFactory, entityManager -> {
            if (entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity) == null) {
                entityManager.persist(entity);
                return entity;
            }
            return entityManager.merge(entity);
        });
    }

    public Optional<T> findById(Object id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return Optional.ofNullable(entityManager.find(entityType, id));
        }
    }

    public List<T> findAll() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return entityManager.createQuery("select entity from " + entityType.getSimpleName() + " entity", entityType)
                    .getResultList();
        }
    }

    public boolean deleteById(Object id) {
        return inTransaction(entityManagerFactory, entityManager -> {
            T entity = entityManager.find(entityType, id);
            if (entity == null) {
                return false;
            }
            entityManager.remove(entity);
            return true;
        });
    }

    public static <R> R inTransaction(EntityManagerFactory factory, Function<EntityManager, R> work) {
        EntityManager entityManager = factory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            R result = work.apply(entityManager);
            transaction.commit();
            return result;
        } catch (RuntimeException | Error failure) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw failure;
        } finally {
            entityManager.close();
        }
    }
}
