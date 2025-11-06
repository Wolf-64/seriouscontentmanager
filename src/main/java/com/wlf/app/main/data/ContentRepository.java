package com.wlf.app.main.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ContentRepository {

    private static final ContentRepository INSTANCE =
            new ContentRepository(EntityManagerProvider.get());

    public static ContentRepository getInstance() {
        return INSTANCE;
    }

    private final EntityManagerFactory emf;

    private ContentRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<ContentEntity> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<ContentEntity> query = em.createQuery("SELECT u FROM ContentEntity u", ContentEntity.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public ContentEntity findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(ContentEntity.class, id);
        } finally {
            em.close();
        }
    }

    public void save(ContentEntity content) {
        var em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(content);
        em.getTransaction().commit();
        em.close();
    }
}