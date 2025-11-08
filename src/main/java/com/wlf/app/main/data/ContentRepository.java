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

    public List<ContentModel> findAll() {
        return executeAndReturn((em) -> {
            TypedQuery<ContentEntity> query = em.createQuery("SELECT u FROM ContentEntity u", ContentEntity.class);
            return query.getResultList().stream().map(ContentMapper.INSTANCE::toGuiModel).toList();
        });
    }

    public ContentModel findById(Long id) {
        return executeAndReturn((em) -> {
            ContentEntity entity = em.find(ContentEntity.class, id);
            ContentModel model = null;
            if (entity != null) {
                model = ContentMapper.INSTANCE.toGuiModel(entity);
            }
            return model;
        });
    }

    public void save(ContentModel content) {
        execute((em) -> {
            ContentEntity entity = ContentMapper.INSTANCE.toEntity(content);
            em.persist(entity);
            content.setId(entity.getId());
        });
    }

    public void update(ContentModel content) {
        execute((em) -> em.merge(ContentMapper.INSTANCE.toEntity(content)));
    }

    public void delete(ContentModel content) {
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