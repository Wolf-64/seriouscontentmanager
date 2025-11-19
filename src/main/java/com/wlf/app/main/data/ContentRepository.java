package com.wlf.app.main.data;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import javafx.beans.property.Property;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
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
            var list = query.getResultList().stream().map(ContentMapper.INSTANCE::toGuiModel).toList();
            return list;
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

    public List<ContentModel> filterByExample(TableFilter filter) {
        return executeAndReturn(em -> {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<ContentEntity> query = criteriaBuilder.createQuery(ContentEntity.class);
            Root<ContentEntity> root = query.from(ContentEntity.class);

            List<Predicate> predicates = new ArrayList<>();

            for (var property : filter.getClass().getDeclaredFields()) {
                property.setAccessible(true);
                try {
                    Property<?> field = (Property<?>) property.get(filter);
                    Object value = field.getValue();

                    if (value == null) {
                        continue;
                    }

                    if (value instanceof String s) {
                        if (!s.isBlank()) {
                            predicates.add(criteriaBuilder.like(root.get(field.getName()), "%" + s + "%"));
                        }
                    } else if (value instanceof Number n
                            && n.doubleValue() > 0) {
                        predicates.add(criteriaBuilder.equal(root.get(field.getName()), value));
                    } else if (value instanceof Game game) {
                        if (game != Game.ANY) {
                            predicates.add(criteriaBuilder.equal(root.get(field.getName()), game));
                        }
                    } else if (value instanceof Type type) {
                        if (type != Type.UNDEFINED) {
                            predicates.add(criteriaBuilder.equal(root.get(field.getName()), type));
                        }
                    } else if (value instanceof Mode mode) {
                        if (mode != Mode.ALL) {
                            predicates.add(criteriaBuilder.equal(root.get(field.getName()), mode));
                        }
                    } else if (value instanceof boolean b) {
                        if (b) {
                            predicates.add(criteriaBuilder.equal(root.get(field.getName()), 1));
                        }
                    }
                } catch (IllegalAccessException exception) {
                    log.warn(exception.toString());
                }
            }

            if (filter.getDateCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreated"), filter.getDateCreatedFrom()));
            } else if (filter.getDateCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateCreated"), filter.getDateCreatedTo()));
            }

            query.where(predicates.toArray(Predicate[]::new));
            return em.createQuery(query).getResultList().stream().map(ContentMapper.INSTANCE::toGuiModel).toList();
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
        if (content.getId() == null) {
            throw new IllegalArgumentException("Cannot update database entry with no ID!");
        }
        execute((em) -> em.merge(ContentMapper.INSTANCE.toEntity(content)));
    }

    public void delete(ContentModel content) {
        if (content.getId() == null) {
            throw new IllegalArgumentException("Cannot delete database entry with no ID!");
        }
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