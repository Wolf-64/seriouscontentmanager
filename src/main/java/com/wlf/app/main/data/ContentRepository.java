package com.wlf.app.main.data;

import jakarta.persistence.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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
        return executeAndReturn((em) -> {
            TypedQuery<ContentEntity> query = em.createQuery("SELECT u FROM ContentEntity u", ContentEntity.class);
            return query.getResultList();
        });
    }

    public ContentEntity findById(Long id) {
        return executeAndReturn((em) -> em.find(ContentEntity.class, id));
    }

    public long save(ContentEntity content) {
        return executeAndReturn((em) -> {
            em.persist(content);
            return content.getId();
        });
    }

    public void update(ContentEntity content) {
        execute((em) -> em.merge(content));
    }

    public void delete(ContentEntity content) {
        execute((em) -> {
            ContentEntity managed = em.find(ContentEntity.class, content.getId());

            if (managed != null) {
                em.remove(managed);
            }
        });
    }

    private void execute(Consumer<EntityManager> statement) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            statement.accept(em);
            em.getTransaction().commit();

        } catch (Exception e){
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private <T> T executeAndReturn(Function<EntityManager, T> statement) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            return statement.apply(em);
        } catch (Exception e){
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}