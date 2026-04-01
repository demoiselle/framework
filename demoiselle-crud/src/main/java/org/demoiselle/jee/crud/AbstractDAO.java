/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
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
import org.demoiselle.jee.crud.exception.DemoiselleCrudException;
import org.demoiselle.jee.crud.filter.FilterOp;
import org.demoiselle.jee.crud.message.DemoiselleCrudMessage;
import org.demoiselle.jee.crud.pagination.PaginationHelperConfig;
import org.demoiselle.jee.crud.pagination.ResultSet;
import org.demoiselle.jee.crud.sort.CrudSort;

@TransactionAttribute(TransactionAttributeType.MANDATORY)
public abstract class AbstractDAO<T, I> implements Crud<T, I> {

    @Inject
    private PaginationHelperConfig paginationConfig;

    @Inject
    private DemoiselleRequestContext drc;

    @Inject
    private DemoiselleCrudMessage bundle;

    private final Class<T> entityClass;

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
    }

    @Override
    public T persist(T entity) {
        try {
            getEntityManager().persist(entity);
            return entity;
        } catch (PersistenceException e) {
            throw new DemoiselleCrudException(msg(() -> bundle.persistError(), "Não foi possível salvar"), e);
        }
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

            return entity;
        } catch (final PersistenceException | IllegalAccessException e) {
            throw new DemoiselleCrudException(
                msg(() -> bundle.mergeError(), "Não foi possível salvar"), e);
        }
    }

    @Override
    public T mergeFull(T entity) {
        try {
            return getEntityManager().merge(entity);
        } catch (PersistenceException e) {
            throw new DemoiselleCrudException(msg(() -> bundle.mergeError(), "Não foi possível salvar"), e);
        }
    }

    @Override
    public void remove(I id) {
        try {
            getEntityManager().remove(getEntityManager().find(entityClass, id));
        } catch (PersistenceException e) {
            throw new DemoiselleCrudException(msg(() -> bundle.removeError(), "Não foi possível excluir"), e);
        }
    }

    @Override
    public T find(I id) {
        try {
            return getEntityManager().find(entityClass, id);
        } catch (PersistenceException e) {
            throw new DemoiselleCrudException(msg(() -> bundle.findError(), "Não foi possível consultar"), e);
        }

    }

    @Override
    public Result find() {

        try {

            Result result = new ResultSet();

            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);

            configureCriteriaQuery(criteriaBuilder, criteriaQuery);

            TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);

            EntityGraph<T> graph = getEntityGraph();
            if (graph != null) {
                query.setHint("jakarta.persistence.fetchgraph", graph);
            }

            if (drc.isPaginationEnabled()) {
                Integer firstResult = drc.getOffset() == null ? 0 : drc.getOffset();
                Integer maxResults = getMaxResult();
                Long count = count();

                if (firstResult < count) {
                    query.setFirstResult(firstResult);
                    query.setMaxResults(maxResults);
                }

                drc.setCount(count);
            }

            result.setContent(query.getResultList());
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
        if (drc.getFilters() != null) {
            criteriaQuery.select(from).where(buildPredicates(criteriaBuilder, criteriaQuery, from));
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
        }
        // This is unreachable because FilterOp is a sealed interface with exactly 7 variants,
        // all handled above. Included only to satisfy the compiler.
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

        if (drc.getFilters() != null) {
            countCriteria.where(buildPredicates(criteriaBuilder, countCriteria, entityRoot));
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