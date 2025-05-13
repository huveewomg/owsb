package com.owsb.repository;

import java.util.List;

/**
 * Generic repository interface following the Repository Pattern
 * @param <T> The entity type this repository manages
 */
public interface Repository<T> {
    List<T> findAll();
    T findById(String id);
    boolean save(T entity);
    boolean update(T entity);
    boolean delete(String id);
}