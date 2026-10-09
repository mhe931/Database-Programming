package com.inventory.service;

import com.inventory.dao.JpaRepository;
import com.inventory.entity.Category;
import jakarta.persistence.EntityManagerFactory;

public final class CategoryService extends EntityService<Category> {
    public CategoryService(EntityManagerFactory factory) {
        super(new JpaRepository<>(factory, Category.class));
    }
}
