package com.bank.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.function.Function;

/**
 * @author Zaycev Denis
 */
public abstract class BaseDBService {

    private EntityManagerFactory factory;

    protected BaseDBService(EntityManagerFactory factory) {
        this.factory = factory;
    }

    protected <T> T inTransaction(Function<EntityManager, T> function) {
        EntityManager manager = factory.createEntityManager();
        manager.getTransaction().begin();

        T value;
        try {

            value = function.apply(manager);
            manager.getTransaction().commit();

        } catch (Throwable e) {
            manager.getTransaction().rollback();
            throw e;
        } finally {
            manager.close();
        }

        return value;
    }

    protected <T> T withJPA(Function<EntityManager, T> function) {
        EntityManager manager = factory.createEntityManager();

        try {
           return function.apply(manager);
        } finally {
            manager.close();
        }
    }


}
