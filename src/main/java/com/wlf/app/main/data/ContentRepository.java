package com.wlf.app.main.data;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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

    public List<ContentModel> filterByExample(ContentModel.Filter filter) {
        return executeAndReturn(em -> {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<ContentEntity> query = criteriaBuilder.createQuery(ContentEntity.class);
            Root<ContentEntity> root = query.from(ContentEntity.class);

            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null && !filter.name().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + filter.name() + "%"));
            }
            if (filter.game() != Game.ANY) {
                predicates.add(criteriaBuilder.equal(root.get("game"), filter.game()));
            }
            if (filter.type() != Type.UNDEFINED) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filter.type()));
            }
            if (filter.mode() != Mode.ALL) {
                predicates.add(criteriaBuilder.equal(root.get("modes"), filter.mode()));
            }
            if (filter.mode() != Mode.ALL) {
                predicates.add(criteriaBuilder.equal(root.get("modes"), filter.mode()));
            }
            if (filter.installed()) {
                predicates.add(criteriaBuilder.equal(root.get("installed"), 1));
            }
            if (filter.completed()) {
                predicates.add(criteriaBuilder.equal(root.get("completed"), 1));
            }
            /*
            for (var field : filter.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(filter);
                    if (value == null) continue;

                    if (value instanceof String s) {
                        if (!s.isBlank()) {
                            predicates.add(criteriaBuilder.like(root.get(field.getName()), "%" + s + "%"));
                        }
                    } else if (value instanceof Game game) {
                        if (game != Game.ANY) {
                            predicates.add(criteriaBuilder.equal(root.get(field.getName()), "%" + game.ordinal() + "%"));
                        }
                    } else if (value instanceof Type type) {
                        if (type != Type.UNDEFINED) {
                            predicates.add(criteriaBuilder.equal(root.get(field.getName()), "%" + type.ordinal() + "%"));
                        }
                    } else if (value instanceof Mode mode) {
                        if (mode != Mode.ALL) {
                            predicates.add(criteriaBuilder.equal(root.get(field.getName()), "%" + mode.ordinal() + "%"));
                        }
                    } else if (value instanceof boolean b) {
                        if (b) {
                            predicates.add(criteriaBuilder.equal(root.get(field.getName()), 1));
                        }
                    } else {
                        predicates.add(criteriaBuilder.equal(root.get(field.getName()), value));
                    }
                } catch (IllegalAccessException exception) {
                    log.warn(exception.toString());
                }
            }

             */

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