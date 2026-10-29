package com.eduguard.dao;

import com.eduguard.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class GenericDAO<T> {

    private final Class<T> type;

    public GenericDAO(Class<T> type) {
        this.type = type;
    }

    public T save(T entity) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getFactory().openSession()) {
            tx = s.beginTransaction();
            T saved = s.merge(entity);
            tx.commit();
            return saved;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public List<T> findAll() {
        try (Session s = HibernateUtil.getFactory().openSession()) {
            return s.createQuery("from " + type.getSimpleName(), type).list();
        }
    }

    public T findById(int id) {
        try (Session s = HibernateUtil.getFactory().openSession()) {
            return s.get(type, id);
        }
    }

    public void delete(int id) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getFactory().openSession()) {
            tx = s.beginTransaction();
            T entity = s.get(type, id);
            if (entity != null) s.remove(entity);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}