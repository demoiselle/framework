/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.Column;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.core.MultivaluedMap;

import org.demoiselle.jee.core.api.crud.Crud;
import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.annotation.SoftDeletable;
import org.demoiselle.jee.crud.batch.BatchConfig;
import org.demoiselle.jee.crud.cache.EntityModifiedEvent;
import org.demoiselle.jee.crud.exception.DemoiselleCrudException;
import org.demoiselle.jee.crud.filter.FilterOp;
import org.demoiselle.jee.crud.message.DemoiselleCrudMessage;
import org.demoiselle.jee.crud.pagination.PageResult;
import org.demoiselle.jee.crud.pagination.PaginationHelperConfig;
import org.demoiselle.jee.crud.pagination.ResultSet;
import org.demoiselle.jee.crud.softdelete.SoftDeleteMeta;
import org.demoiselle.jee.crud.sort.CrudSort;
import org.demoiselle.jee.crud.specification.Specification;

@TransactionAttribute(TransactionAttributeType.MANDATORY)
public abstract class AbstractDAO<T, I> implements Crud<T, I> {

    @Inject
    private PaginationHelperConfig paginationConfig;

    @Inject
    private DemoiselleRequestContext drc;

    @Inject
    private DemoiselleCrudMessage bundle;

    @Inject
    private BatchConfig batchConfig;

    @Inject
    private Event<EntityModifiedEvent<?>> entityModifiedEvent;

    private final Class<T> entityClass;

    private final SoftDeleteMeta softDeleteMeta;

    protected abstract EntityManager getEntityManager();

    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Returns the message from the bundle, or the fallback if the bundle is null
     * (e.g. when AbstractDAO is instantiated outside a CDI container).
     */
    private String msg(java.util.function.Supplier<String> bundleCall, String fallback) {
        if (bundle != null) {
            return bundleCall.get();
        }
        return fallback;
    }

    @SuppressWarnings("unchecked")
    public AbstractDAO() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.softDeleteMeta = resolveSoftDeleteMeta(this.entityClass);
    }

    /**
     * Resolves soft delete metadata from the {@link SoftDeletable} annotation
     * on the entity class. Validates that the specified field exists and that
     * the configured type is supported (LocalDateTime, Boolean, Instant).
     *
     * @param entityClass the entity class to inspect
     * @return a SoftDeleteMeta if the annotation is present, or null otherwise
     * @throws DemoiselleCrudException if the field does not exist or the type is unsupported
     */
    private SoftDeleteMeta resolveSoftDeleteMeta(Class<T> entityClass) {
        SoftDeletable annotation = entityClass.getAnnotation(SoftDeletable.class);
        if (annotation == null) {
            return null;
        }

        String fieldName = annotation.field();
        Class<?> fieldType = annotation.type();

        // Validate that the specified field exists in the entity
        boolean fieldExists = CrudUtilHelper.getAllFields(new ArrayList<>(), entityClass)
                .stream()
                .anyMatch(f -> f.getName().equals(fieldName));

        if (!fieldExists) {
            throw new DemoiselleCrudException(
                    "Soft delete field '" + fieldName + "' not found in entity " + entityClass.getSimpleName());
        }

        // Validate that the type is supported
        if (fieldType != LocalDateTime.class && fieldType != Boolean.class
                && fieldType != boolean.class && fieldType != Instant.class) {
            throw new DemoiselleCrudException(
                    "Unsupported soft delete field type: " + fieldType.getSimpleName()
                            + ". Supported types: LocalDateTime, Boolean, Instant");
        }

        return new SoftDeleteMeta(fieldName, fieldType);
    }

    /**
     * Builds a predicate that filters out soft-deleted records.
     * For Boolean types: {@code field = false OR field IS NULL}.
     * For temporal types (LocalDateTime, Instant): {@code field IS NULL}.
     *
     * @param cb   the CriteriaBuilder
     * @param root the Root of the query
     * @return a Predicate that excludes soft-deleted records
     */
    protected Predicate softDeletePredicate(CriteriaBuilder cb, Root<T> root) {
        if (softDeleteMeta.isBoolean()) {
            return cb.or(
                    cb.isFalse(root.get(softDeleteMeta.fieldName())),
                    cb.isNull(root.get(softDeleteMeta.fieldName()))
            );
        }
        return cb.isNull(root.get(softDeleteMeta.fieldName()));
    }

    /**
     * Returns the soft delete metadata for this DAO's entity class,
     * or null if the entity is not annotated with {@link SoftDeletable}.
     *
     * @return the SoftDeleteMeta, or null
     */
    protected SoftDeleteMeta getSoftDeleteMeta() {
        return softDeleteMeta;
    }

    /**
     * Fires an {@link EntityModifiedEvent} if the CDI event injection is available.
     * Safely handles the case where the DAO is used outside a CDI container (e.g., in unit tests).
     *
     * @param action the type of modification
     * @param payload the entity or ID involved
     */
    @SuppressWarnings("unchecked")
    private void fireEntityModifiedEvent(EntityModifiedEvent.Action action, Object payload) {
        if (entityModifiedEvent != null) {
            entityModifiedEvent.fire(new EntityModifiedEvent<>(
                    (Class<Object>) (Class<?>) entityClass, action, payload));
        }
    }

    @Override
    public T persist(T entity) {
        try {
            getEntityManager().persist(entity);
            fireEntityModifiedEvent(EntityModifiedEvent.Action.PERSIST, entity);
            return entity;
        } catch (PersistenceException e) {
            throw new DemoiselleCrudException(msg(() -> bundle.persistError(), "Não foi possível salvar"), e);
        }
    }

    /**
     * Persists all entities in the given list using batch processing.
     * Flushes and clears the EntityManager every N records (configured via BatchConfig).
     *
     * @param entities the list of entities to persist
     * @return the list of persisted entities
     * @throws DemoiselleCrudException if a PersistenceException occurs, including the index of the failing entity
     */
    public List<T> persistAll(List<T> entities) {
        int batchSize = batchConfig.getSize();
        List<T> result = new ArrayList<>(entities.size());
        for (int i = 0; i < entities.size(); i++) {
            try {
                getEntityManager().persist(entities.get(i));
                result.add(entities.get(i));
            } catch (PersistenceException e) {
                throw new DemoiselleCrudException(
                    "Erro ao persistir entidade no índice " + i, e);
            }
            if ((i + 1) % batchSize == 0) {
                getEntityManager().flush();
                getEntityManager().clear();
            }
        }
        getEntityManager().flush();
        getEntityManager().clear();
        return result;
    }

    public int removeAll(List<I> ids) {
        int batchSize = batchConfig.getSize();
        int removed = 0;
        for (int i = 0; i < ids.size(); i++) {
            remove(ids.get(i));
            removed++;
            if ((i + 1) % batchSize == 0) {
                getEntityManager().flush();
                getEntityManager().clear();
            }
        }
        getEntityManager().flush();
        getEntityManager().clear();
        return removed;
    }

    public int updateAll(Specification<T> spec, Map<String, Object> updates) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<T> cu = cb.createCriteriaUpdate(entityClass);
        Root<T> root = cu.from(entityClass);

        updates.forEach((field, value) -> cu.set(root.get(field), value));

        if (spec != null) {
            cu.where(spec.toPredicate(root, null, cb));
        }

        return getEntityManager().createQuery(cu).executeUpdate();
    }

    @Override
    public T mergeHalf(I id, T entity) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaUpdate<T> update = cb.createCriteriaUpdate(entityClass);
            Root<T> root = update.from(entityClass);

            boolean hasUpdates = false;

            for (final Field field : getAllFields(entityClass)) {
                if (!field.isAnnotationPresent(ManyToOne.class)) {
                    final Column column = field.getAnnotation(Column.class);
                    if (column == null || !column.updatable()) {
                        continue;
                    }
                }
                field.setAccessible(true);
                final Object value = field.get(entity);
                if (value != null) {
                    update.set(root.get(field.getName()), value);
                    hasUpdates = true;
                }
            }

            if (hasUpdates) {
                String idName = CrudUtilHelper.getMethodAnnotatedWithID(entityClass);
                update.where(cb.equal(root.get(idName), id));
                getEntityManager().createQuery(update).executeUpdate();
            }

            fireEntityModifiedEvent(EntityModifiedEvent.Action.MERGE, entity);
            return entity;
        } catch (final PersistenceException | IllegalAccessException e) {
            throw new DemoiselleCrudException(
                msg(() -> bundle.mergeError(), "Não foi possível salvar"), e);
        }
    }

    @Override
    public T mergeFull(T entity) {
        try {
            T result = getEntityManager().merge(entity);
            fireEntityModifiedEvent(EntityModifiedEvent.Action.MERGE, result);
            return result;
        } catch (PersistenceException e) {
            throw new DemoiselleCrudException(msg(() -> bundle.mergeError(), "Não foi possível salvar"), e);
        }
    }

    @Override
    public void remove(I id) {
        try {
            if (softDeleteMeta != null) {
                CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
                CriteriaUpdate<T> update = cb.createCriteriaUpdate(entityClass);
                Root<T> root = update.from(entityClass);

                Object deleteValue;
                Class<?> fieldType = softDeleteMeta.fieldType();
                if (softDeleteMeta.isBoolean()) {
                    deleteValue = Boolean.TRUE;
                } else if (fieldType == Instant.class) {
                    deleteValue = Instant.now();
                } else {
                    deleteValue = LocalDateTime.now();
                }

                update.set(root.get(softDeleteMeta.fieldName()), deleteValue);

                String idFieldName = CrudUtilHelper.getMethodAnnotatedWithID(entityClass);
                update.where(cb.equal(root.get(idFieldName), id));
                getEntityManager().createQuery(update).executeUpdate();
            } else {
                getEntityManager().remove(getEntityManager().find(entityClass, id));
            }
            fireEntityModifiedEvent(EntityModifiedEvent.Action.REMOVE, id);
        } catch (PersistenceException e) {
            throw new DemoiselleCrudException(msg(() -> bundle.removeError(), "Não foi possível excluir"), e);
        }
    }

    @Override
    public T find(I id) {
        try {
            if (softDeleteMeta != null) {
                CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
                CriteriaQuery<T> cq = cb.createQuery(entityClass);
                Root<T> root = cq.from(entityClass);

                String idFieldName = CrudUtilHelper.getMethodAnnotatedWithID(entityClass);

                cq.select(root).where(
                        cb.equal(root.get(idFieldName), id),
                        softDeletePredicate(cb, root)
                );

                List<T> results = getEntityManager().createQuery(cq).getResultList();
                return results.isEmpty() ? null : results.get(0);
            }
            return getEntityManager().find(entityClass, id);
        } catch (PersistenceException e) {
            throw new DemoiselleCrudException(msg(() -> bundle.findError(), "Não foi possível consultar"), e);
        }
    }

    @Override
    public Result find() {

        try {

            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);

            configureCriteriaQuery(criteriaBuilder, criteriaQuery);

            TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);

            EntityGraph<T> graph = getEntityGraph();
            if (graph != null) {
                query.setHint("jakarta.persistence.fetchgraph", graph);
            }

            Result result;

            if (drc.isPaginationEnabled()) {
                Integer firstResult = drc.getOffset() == null ? 0 : drc.getOffset();
                Integer maxResults = getMaxResult();
                Long count = count();

                if (firstResult < count) {
                    query.setFirstResult(firstResult);
                    query.setMaxResults(maxResults);
                }

                drc.setCount(count);

                result = PageResult.of(query.getResultList(), count, firstResult, maxResults);
            } else {
                ResultSet rs = new ResultSet();
                rs.setContent(query.getResultList());
                result = rs;
            }

            if (result.getContent() != null && !result.getContent().isEmpty()
                    && drc.isPaginationEnabled()
                    && result.getContent().size() <= drc.getCount() && drc.getCount() < getMaxResult()) {
                drc.setLimit(drc.getCount().intValue());
            }

            drc.setEntityClass(entityClass);

            return result;

        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
            throw new DemoiselleCrudException(msg(() -> bundle.findError(), "Não foi possível consultar"), e);
        }
    }

    /**
     * Finds all entities including those marked as soft-deleted.
     * Applies DRC filters, pagination, ordering, and entity graph hints,
     * but does NOT apply the soft delete predicate.
     *
     * @return a Result (PageResult when pagination is enabled, ResultSet otherwise)
     */
    public Result findIncludingDeleted() {

        try {

            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<T> from = criteriaQuery.from(entityClass);

            List<Predicate> predicates = new ArrayList<>();

            if (drc.getFilters() != null) {
                predicates.addAll(Arrays.asList(buildPredicates(criteriaBuilder, criteriaQuery, from)));
            }

            // Intentionally skip softDeletePredicate

            if (!predicates.isEmpty()) {
                criteriaQuery.select(from).where(predicates.toArray(new Predicate[0]));
            } else {
                criteriaQuery.select(from);
            }

            configureOrder(criteriaBuilder, criteriaQuery, from);

            TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);

            EntityGraph<T> graph = getEntityGraph();
            if (graph != null) {
                query.setHint("jakarta.persistence.fetchgraph", graph);
            }

            Result result;

            if (drc.isPaginationEnabled()) {
                Integer firstResult = drc.getOffset() == null ? 0 : drc.getOffset();
                Integer maxResults = getMaxResult();
                Long count = countIncludingDeleted();

                if (firstResult < count) {
                    query.setFirstResult(firstResult);
                    query.setMaxResults(maxResults);
                }

                drc.setCount(count);

                result = PageResult.of(query.getResultList(), count, firstResult, maxResults);
            } else {
                ResultSet rs = new ResultSet();
                rs.setContent(query.getResultList());
                result = rs;
            }

            if (result.getContent() != null && !result.getContent().isEmpty()
                    && drc.isPaginationEnabled()
                    && result.getContent().size() <= drc.getCount() && drc.getCount() < getMaxResult()) {
                drc.setLimit(drc.getCount().intValue());
            }

            drc.setEntityClass(entityClass);

            return result;

        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
            throw new DemoiselleCrudException(msg(() -> bundle.findError(), "Não foi possível consultar"), e);
        }
    }

    /**
     * Counts all entities including those marked as soft-deleted.
     * Applies DRC filters but does NOT apply the soft delete predicate.
     *
     * @return the total count including soft-deleted records
     */
    private Long countIncludingDeleted() {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> countCriteria = criteriaBuilder.createQuery(Long.class);
        Root<T> entityRoot = countCriteria.from(entityClass);
        countCriteria.select(criteriaBuilder.count(entityRoot));

        List<Predicate> predicates = new ArrayList<>();

        if (drc.getFilters() != null) {
            predicates.addAll(Arrays.asList(buildPredicates(criteriaBuilder, countCriteria, entityRoot)));
        }

        // Intentionally skip softDeletePredicate

        if (!predicates.isEmpty()) {
            countCriteria.where(predicates.toArray(new Predicate[0]));
        }

        return getEntityManager().createQuery(countCriteria).getSingleResult();
    }

    /**
     * Finds entities matching the given Specification, combined with any filters
     * from the DemoiselleRequestContext using AND. Applies pagination when enabled.
     * If {@code spec} is null, behaves like the standard {@link #find()}.
     *
     * @param spec the Specification to apply, or null for no additional predicate
     * @return a Result (PageResult when pagination is enabled, ResultSet otherwise)
     */
    public Result find(Specification<T> spec) {

        if (spec == null) {
            return find();
        }

        try {

            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<T> root = criteriaQuery.from(entityClass);

            List<Predicate> predicates = new ArrayList<>();

            // Specification predicate
            predicates.add(spec.toPredicate(root, criteriaQuery, criteriaBuilder));

            // Existing filter predicates from DemoiselleRequestContext
            if (drc.getFilters() != null) {
                predicates.addAll(Arrays.asList(buildPredicates(criteriaBuilder, criteriaQuery, root)));
            }

            // Soft delete predicate
            if (softDeleteMeta != null) {
                predicates.add(softDeletePredicate(criteriaBuilder, root));
            }

            criteriaQuery.select(root).where(predicates.toArray(new Predicate[0]));
            configureOrder(criteriaBuilder, criteriaQuery, root);

            TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);

            EntityGraph<T> graph = getEntityGraph();
            if (graph != null) {
                query.setHint("jakarta.persistence.fetchgraph", graph);
            }

            Result result;

            if (drc.isPaginationEnabled()) {
                Integer firstResult = drc.getOffset() == null ? 0 : drc.getOffset();
                Integer maxResults = getMaxResult();
                Long count = countWithSpecification(spec);

                if (firstResult < count) {
                    query.setFirstResult(firstResult);
                    query.setMaxResults(maxResults);
                }

                drc.setCount(count);

                result = PageResult.of(query.getResultList(), count, firstResult, maxResults);
            } else {
                ResultSet rs = new ResultSet();
                rs.setContent(query.getResultList());
                result = rs;
            }

            if (result.getContent() != null && !result.getContent().isEmpty()
                    && drc.isPaginationEnabled()
                    && result.getContent().size() <= drc.getCount() && drc.getCount() < getMaxResult()) {
                drc.setLimit(drc.getCount().intValue());
            }

            drc.setEntityClass(entityClass);

            return result;

        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
            throw new DemoiselleCrudException(msg(() -> bundle.findError(), "Não foi possível consultar"), e);
        }
    }

    /**
     * Counts entities matching the given Specification combined with DRC filters.
     *
     * @param spec the Specification to apply
     * @return the count of matching entities
     */
    private Long countWithSpecification(Specification<T> spec) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> countCriteria = criteriaBuilder.createQuery(Long.class);
        Root<T> entityRoot = countCriteria.from(entityClass);
        countCriteria.select(criteriaBuilder.count(entityRoot));

        List<Predicate> predicates = new ArrayList<>();

        if (spec != null) {
            predicates.add(spec.toPredicate(entityRoot, countCriteria, criteriaBuilder));
        }

        if (drc.getFilters() != null) {
            predicates.addAll(Arrays.asList(buildPredicates(criteriaBuilder, countCriteria, entityRoot)));
        }

        if (softDeleteMeta != null) {
            predicates.add(softDeletePredicate(criteriaBuilder, entityRoot));
        }

        if (!predicates.isEmpty()) {
            countCriteria.where(predicates.toArray(new Predicate[0]));
        }

        return getEntityManager().createQuery(countCriteria).getSingleResult();
    }

    /**
     * Returns the EntityGraph to apply as a fetch graph hint on find() queries.
     * Subclasses can override this to provide a custom EntityGraph for controlling
     * the fetch strategy of relationships.
     *
     * @return the EntityGraph to use, or null to use default fetch strategy
     */
    protected EntityGraph<T> getEntityGraph() {
        return null;
    }

    protected void configureCriteriaQuery(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> criteriaQuery) {
        Root<T> from = criteriaQuery.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();

        if (drc.getFilters() != null) {
            predicates.addAll(Arrays.asList(buildPredicates(criteriaBuilder, criteriaQuery, from)));
        }

        if (softDeleteMeta != null) {
            predicates.add(softDeletePredicate(criteriaBuilder, from));
        }

        if (!predicates.isEmpty()) {
            criteriaQuery.select(from).where(predicates.toArray(new Predicate[0]));
        } else {
            criteriaQuery.select(from);
        }

        configureOrder(criteriaBuilder, criteriaQuery, from);
    }

    protected void configureOrder(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> criteriaQuery, Root<T> root) {

        if (!drc.getSorts().isEmpty()) {
            List<Order> orders = new ArrayList<>();

            drc.getSorts().stream().forEachOrdered(sortModel -> {

                if (sortModel.type().equals(CrudSort.ASC)) {
                    orders.add(criteriaBuilder.asc(root.get(sortModel.field())));
                } else {
                    orders.add(criteriaBuilder.desc(root.get(sortModel.field())));
                }
            });

            criteriaQuery.orderBy(orders);
        }

    }

    protected Predicate[] buildPredicates(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, Root<T> root) {
        List<Predicate> predicates = new LinkedList<>();

        if (drc.getFilters() != null) {
            drc.getFilters().getChildren().stream().forEach(child -> {

                List<Predicate> predicatesToBuild = new LinkedList<>();

                /*
                 * If the child doesnt child the element is on fist level.
                 * 
                 * ?description=test
                 * 
                 */
                if (child.getChildren().isEmpty()) {
                    
                    child.getValue().stream().forEach(value -> {
                        fillPredicates(predicatesToBuild, root, criteriaBuilder, criteriaQuery, child, value, null);
                    });
                    
                }
                else{

                    /*
                     * If the child has child the element has second level
                     * 
                     * ?category(description)=test
                     */
                    
                    Join<?, ?> join = root.join(child.getKey());
                    child.getChildren().stream().forEach( child2ndLevel -> {

                        child2ndLevel.getValue().stream().forEach(value -> {
                            fillPredicates(predicatesToBuild, join, criteriaBuilder, criteriaQuery, child2ndLevel, value, child);
                        });
                    });
                } 
                
                predicates.add(criteriaBuilder.or(predicatesToBuild.toArray(new Predicate[]{})));
            });
        }

        return predicates.toArray(new Predicate[]{});
    }
    
    private void fillPredicates(List<Predicate> predicates, From<?, ?>  from, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, TreeNodeField<String, Set<String>> child, String value, TreeNodeField<String, Set<String>> parent) {
        
        if (child.getValue().isEmpty()) {
            predicates.add(criteriaBuilder.isEmpty(from.get(child.getKey())));
        } else {
            FilterOp op = resolveFilterOp(child.getKey(), value, parent);
            predicates.add(buildPredicate(op, from, criteriaBuilder, criteriaQuery));
        }
    }

    /**
     * Resolves the filter operation type based on the value and context.
     *
     * @param key    the field name
     * @param value  the filter value
     * @param parent the parent tree node (null for first-level filters)
     * @return a FilterOp instance representing the resolved filter operation
     */
    protected FilterOp resolveFilterOp(String key, String value, TreeNodeField<String, Set<String>> parent) {
        if ("null".equals(value) || value == null) {
            return new FilterOp.IsNull(key);
        }

        // Operator prefixes (precedence over existing filters)
        if (value.startsWith("gt:")) {
            return new FilterOp.GreaterThan(key, value.substring(3));
        }
        if (value.startsWith("lt:")) {
            return new FilterOp.LessThan(key, value.substring(3));
        }
        if (value.startsWith("gte:")) {
            return new FilterOp.GreaterThanOrEqual(key, value.substring(4));
        }
        if (value.startsWith("lte:")) {
            return new FilterOp.LessThanOrEqual(key, value.substring(4));
        }
        if (value.startsWith("between:")) {
            String[] parts = value.substring(8).split(",", -1);
            if (parts.length != 2) {
                throw new IllegalArgumentException("between: requer exatamente 2 valores separados por vírgula");
            }
            return new FilterOp.Between(key, parts[0].trim(), parts[1].trim());
        }
        if (value.startsWith("in:")) {
            List<String> vals = Arrays.stream(value.substring(3).split(","))
                .map(String::trim).toList();
            return new FilterOp.In(key, vals);
        }

        // Existing filters (Like, IsNull, IsTrue, IsFalse, Enum, UUID, Equals)
        if (isLikeFilter(value)) {
            return new FilterOp.Like(key, value);
        }
        if ("isTrue".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
            return new FilterOp.IsTrue(key);
        }
        if ("isFalse".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return new FilterOp.IsFalse(key);
        }
        if (isEnumFilter(key, value, parent)) {
            return new FilterOp.EnumFilter(key, value, convertEnumToInt(key, value, parent));
        }
        if (isUUIDFilter(key, value, parent)) {
            return new FilterOp.UUIDFilter(key, UUID.fromString(value));
        }
        return new FilterOp.Equals(key, value);
    }

    /**
     * Builds a JPA Predicate from a FilterOp using exhaustive instanceof checks.
     * All 7 variants of the sealed interface are handled without a default clause.
     *
     * @param op   the filter operation
     * @param from the JPA From (Root or Join)
     * @param cb   the CriteriaBuilder
     * @param cq   the CriteriaQuery
     * @return the corresponding JPA Predicate
     */
    protected Predicate buildPredicate(FilterOp op, From<?, ?> from, CriteriaBuilder cb, CriteriaQuery<?> cq) {
        if (op instanceof FilterOp.IsNull isNull) {
            return cb.isNull(from.get(isNull.key()));
        } else if (op instanceof FilterOp.Like like) {
            return buildLikePredicate(cb, cq, from, like.key(), like.pattern());
        } else if (op instanceof FilterOp.IsTrue isTrue) {
            return cb.isTrue(from.get(isTrue.key()));
        } else if (op instanceof FilterOp.IsFalse isFalse) {
            return cb.isFalse(from.get(isFalse.key()));
        } else if (op instanceof FilterOp.EnumFilter enumFilter) {
            return cb.equal(from.get(enumFilter.key()), enumFilter.ordinal());
        } else if (op instanceof FilterOp.UUIDFilter uuidFilter) {
            return cb.equal(from.get(uuidFilter.key()), uuidFilter.value());
        } else if (op instanceof FilterOp.Equals equals) {
            return cb.equal(from.get(equals.key()), equals.value());
        } else if (op instanceof FilterOp.GreaterThan gt) {
            return cb.greaterThan(from.get(gt.key()), gt.value());
        } else if (op instanceof FilterOp.LessThan lt) {
            return cb.lessThan(from.get(lt.key()), lt.value());
        } else if (op instanceof FilterOp.GreaterThanOrEqual gte) {
            return cb.greaterThanOrEqualTo(from.get(gte.key()), gte.value());
        } else if (op instanceof FilterOp.LessThanOrEqual lte) {
            return cb.lessThanOrEqualTo(from.get(lte.key()), lte.value());
        } else if (op instanceof FilterOp.Between btw) {
            return cb.between(from.get(btw.key()), btw.lower(), btw.upper());
        } else if (op instanceof FilterOp.In in) {
            return from.get(in.key()).in(in.values());
        }
        // This is unreachable because FilterOp is a sealed interface and all variants
        // are handled above. Included only to satisfy the compiler.
        throw new AssertionError("Unhandled FilterOp variant: " + op.getClass().getName());
    }
    
    protected Boolean isEnumFilter(String key, String value, TreeNodeField<String, Set<String>> tnf) {
		for (Field field : getAllFields(entityClass)) {
		    
		    if(tnf != null){
		        
		        if(field.getName().equalsIgnoreCase(tnf.getKey())){
		            try{
    		            Class<?> c = Class.forName(field.getType().getName());
    		            for(Field field2ndLevel : getAllFields(c)) {
    		                if (key.equalsIgnoreCase(field2ndLevel.getName())) {
    		                    return field2ndLevel.getType().isEnum();
    		                }
    		            }
		            }
		            catch (IllegalArgumentException | ClassNotFoundException | SecurityException e) {
		                throw new DemoiselleCrudException(msg(() -> bundle.enumTypeCheckError(), "Não foi possível verificar se campo é do tipo 'ENUM'"), e);
		            }
		        }
		    }
		    else if (key.equalsIgnoreCase(field.getName())) {
				return field.getType().isEnum();
		    }
		}

		return Boolean.FALSE;
	}
    
    protected Boolean isUUIDFilter(String key, String value, TreeNodeField<String, Set<String>> tnf) {
        for (Field field: getAllFields(entityClass)) {
            
            if(tnf != null){
                
                if(field.getName().equalsIgnoreCase(tnf.getKey())){
                    try{
                        Class<?> c = Class.forName(field.getType().getName());
                        for(Field field2ndLevel : getAllFields(c)) {
                            if (key.equalsIgnoreCase(field2ndLevel.getName())) {
                                return field2ndLevel.getType().isAssignableFrom(UUID.class);
                            }
                        }
                    }
                    catch (IllegalArgumentException | ClassNotFoundException | SecurityException e) {
                        throw new DemoiselleCrudException(msg(() -> bundle.uuidTypeCheckError(), "Não foi possível verificar se campo é do tipo 'UUID'"), e);
                    }
                }
            }
            else if (key.equalsIgnoreCase(field.getName())) {
                return field.getType().isAssignableFrom(UUID.class);
            }
        }
        
        return Boolean.FALSE;
    }

	protected Integer convertEnumToInt(String key, String value, TreeNodeField<String, Set<String>> tnf) {
		for (Field field : getAllFields(entityClass)) {
		    
		    if(tnf != null){
                
                if(field.getName().equalsIgnoreCase(tnf.getKey())){
                    try {
                        Class<?> c = Class.forName(field.getType().getName());
                        
                        for(Field field2ndLevel : getAllFields(c)) {
                            if (key.equalsIgnoreCase(field2ndLevel.getName()) && field2ndLevel.getType().isEnum()) {
                                Integer enumResult = getEnumOrdinal(field2ndLevel, value);
                                if(enumResult != null) {
                                    return enumResult;
                                }                            
                            }
                        }
                    }
                    catch (IllegalArgumentException | ClassNotFoundException | SecurityException e) {
                        throw new DemoiselleCrudException(msg(() -> bundle.enumConversionError(), "Não foi possível realizar a conversão de Enum para Integer"), e);
                    }
                }
            }
            else{   
                
				if (key.equals(field.getName())) {
					if (field.getType().isEnum()) {
						Integer enumResult = getEnumOrdinal(field, value);
						if(enumResult != null) {
						    return enumResult;
						}
					} 
					else {
					    throw new DemoiselleCrudException(msg(() -> bundle.enumTypeCheckError(), "Não foi possível verificar se campo é do tipo 'ENUM'"));	
					}
				} 
            }
		}
		
		// If doesnt find any constant throws
		throw new DemoiselleCrudException(msg(() -> bundle.enumValueNotFound(value), "Não foi possível encontrar o valor [" + value + "] nas constantes"));
		
	}
	
	private Integer getEnumOrdinal(Field field, String value) {
	    
	    try {
    	    Class<?> c = Class.forName(field.getType().getName());
            for (Object obj : c.getEnumConstants()) {
                if (value.equalsIgnoreCase(obj.toString())){
                    return ((Enum<?>)obj).ordinal();        
                }
            }
	    } catch (IllegalArgumentException | ClassNotFoundException | SecurityException e) {
            throw new DemoiselleCrudException(msg(() -> bundle.enumConversionError(), "Não foi possível realizar a conversão de Enum para Integer"), e);
        }
	    
	    return null;
	}
	
	private List<Field> getAllFields(Class<?> clazz){
	    List<Field> fields = new ArrayList<>();
	    return CrudUtilHelper.getAllFields(fields, clazz);
	}

    protected Boolean isLikeFilter(String value) {
        return value.startsWith("*") || value.endsWith("*");
    }

    protected Predicate buildLikePredicate(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, From<?, ?> root, String key, String value) {
        String pattern = value.trim();
        //
        if (pattern.startsWith("*")) {
            pattern = "%" + pattern.substring(1);
        }
        if (pattern.endsWith("*")) {
            pattern = pattern.substring(0, pattern.length() - 1) + "%";
        }
        //
        return criteriaBuilder.like(criteriaBuilder.lower(root.get(key)), pattern.toLowerCase());
    }

    protected Integer getMaxResult() {
        if (drc.getLimit() == null || drc.getOffset() == null) {
            return paginationConfig.getDefaultPagination();
        }

        return (drc.getLimit() - drc.getOffset()) + 1;
    }

    public Long count() {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> countCriteria = criteriaBuilder.createQuery(Long.class);
        Root<T> entityRoot = countCriteria.from(entityClass);
        countCriteria.select(criteriaBuilder.count(entityRoot));

        List<Predicate> predicates = new ArrayList<>();

        if (drc.getFilters() != null) {
            predicates.addAll(Arrays.asList(buildPredicates(criteriaBuilder, countCriteria, entityRoot)));
        }

        if (softDeleteMeta != null) {
            predicates.add(softDeletePredicate(criteriaBuilder, entityRoot));
        }

        if (!predicates.isEmpty()) {
            countCriteria.where(predicates.toArray(new Predicate[0]));
        }

        return getEntityManager().createQuery(countCriteria).getSingleResult();
    }

    protected Predicate[] extractPredicates(MultivaluedMap<String, String> queryParameters,
            CriteriaBuilder criteriaBuilder, Root<T> root) {
        return new Predicate[]{};
    }

    public PaginationHelperConfig getPaginationConfig() {
        return paginationConfig;
    }

    public DemoiselleRequestContext getDrc() {
        return drc;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }
        
}