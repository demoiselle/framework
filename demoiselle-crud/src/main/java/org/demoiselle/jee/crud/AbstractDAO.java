/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.time.DateUtils;
import org.demoiselle.jee.core.api.crud.Crud;
import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.exception.DemoiselleCrudException;
import org.demoiselle.jee.crud.pagination.PaginationHelperConfig;
import org.demoiselle.jee.crud.pagination.ResultSet;
import org.demoiselle.jee.crud.sort.CrudSort;
import org.demoiselle.jee.crud.sort.SortModel;

@TransactionAttribute(TransactionAttributeType.MANDATORY)
public abstract class AbstractDAO<T, I> implements Crud<T, I> {

    @Inject
    private PaginationHelperConfig paginationConfig;

    @Inject
    private DemoiselleRequestContext drc;

    private final Class<T> entityClass;

    protected abstract EntityManager getEntityManager();

    private Logger logger = Logger.getLogger(this.getClass().getName());
    
	private static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	private static final String ISO8601_UTC_PATTERN = ISO8601_PATTERN + "'Z'";

	private static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");

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
        } catch (Exception e) {
            throw new DemoiselleCrudException("Não foi possível salvar", e);
        }
    }

    @Override
    public T mergeHalf(I id, T entity) {
        try {
            final StringBuilder sb = new StringBuilder();
            final Map<String, Object> params = new ConcurrentHashMap<>();
            sb.append("UPDATE ");
            sb.append(entityClass.getCanonicalName());
            sb.append(" SET ");
            for (final Field field : getAllFields(entityClass)) {
                if (!field.isAnnotationPresent(ManyToOne.class)) {
                    final Column column = field.getAnnotation(Column.class);
                    if (column == null || !column.updatable()) {
                        continue;
                    }
                }
                field.setAccessible(true);
                final String name = field.getName();
                final Object value = field.get(entity);
                if (value != null) {
                    if (!params.isEmpty()) {
                        sb.append(", ");
                    }
                    sb.append(name).append(" = :").append(name);
                    params.putIfAbsent(name, value);
                }
            }
            if (!params.isEmpty()) {
                final String idName = CrudUtilHelper.getMethodAnnotatedWithID(entityClass);
                sb.append(" WHERE ").append(idName).append(" = :").append(idName);
                params.putIfAbsent(idName, id);
                final Query query = getEntityManager().createQuery(sb.toString());
                for (final Map.Entry<String, Object> entry : params.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
                query.executeUpdate();
            }
            
            return entity;
        } catch (final Exception e) {
            throw new DemoiselleCrudException("Não foi possível salvar", e);
        }
    }

    @Override
    public T mergeFull(T entity) {
        try {
            return getEntityManager().merge(entity);
        } catch (Exception e) {
            throw new DemoiselleCrudException("Não foi possível salvar", e);
        }
    }

    @Override
    public void remove(I id) {
        try {
            getEntityManager().remove(getEntityManager().find(entityClass, id));
        } catch (Exception e) {
            throw new DemoiselleCrudException("Não foi possível excluir", e);
        }
    }

    @Override
    public T find(I id) {
        try {
            return getEntityManager().find(entityClass, id);
        } catch (Exception e) {
            throw new DemoiselleCrudException("Não foi possível consultar", e);
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

        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoiselleCrudException("Não foi possível consultar", e);
        }
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

                if (sortModel.getType().equals(CrudSort.ASC)) {
                	orders.add(criteriaBuilder.asc(buildSortPath(criteriaBuilder, criteriaQuery, root, sortModel)));
                } else {
                	orders.add(criteriaBuilder.desc(buildSortPath(criteriaBuilder, criteriaQuery, root, sortModel)));
                }
            });

            criteriaQuery.orderBy(orders);
        }

    }
    
    protected Path<?> buildSortPath(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> criteriaQuery, Root<T> root, SortModel model) {
    	if (CrudUtilHelper.hasSubField(model.getField())) {
    		String[] attrs = model.getField().split("[()]");
    		//
    		return root.get(attrs[0]).get(attrs[1]);
    	} else {
    		return root.get(model.getField());
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
        
        if ("null".equals(value) || value == null) {
            predicates.add(criteriaBuilder.isNull(from.get(child.getKey())));
        } else if (child.getValue().isEmpty()) {
            predicates.add(criteriaBuilder.isEmpty(from.get(child.getKey())));
        } else if (isLikeFilter(value)) {
            predicates.add(buildLikePredicate(criteriaBuilder, criteriaQuery, from, child.getKey(), value));
        } else if (isRelationalFilter(child.getKey(), value)) {
        	predicates.add(buildRelationalPredicate(criteriaBuilder, criteriaQuery, from, child.getKey(), value, parent));
        } else if ("isTrue".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
            predicates.add(criteriaBuilder.isTrue(from.get(child.getKey())));
        } else if ("isFalse".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            predicates.add(criteriaBuilder.isFalse(from.get(child.getKey())));
        } else if (isEnumFilter(child.getKey(), value, parent)) {
            predicates.add(criteriaBuilder.equal(from.get(child.getKey()), convertEnumToInt(child.getKey(), value, parent)));
        } else if (isDateFilter(child.getKey(), value)) {
        	predicates.add(criteriaBuilder.equal(from.get(child.getKey()), convertStringToDate(child.getKey(), value)));            
        } else if (isUUIDFilter(child.getKey(), value, parent)) {
            predicates.add(criteriaBuilder.equal(from.get(child.getKey()), UUID.fromString(value)));
        } else {
            predicates.add(criteriaBuilder.equal(from.get(child.getKey()), value));
        }
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
		                throw new DemoiselleCrudException("Não foi possível verificar se campo é do tipo 'ENUM'", e);
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
                        throw new DemoiselleCrudException("Não foi possível verificar se campo é do tipo 'UUID'", e);
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
                        throw new DemoiselleCrudException("Não foi possível realizar a conversão de Enum para Integer", e);
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
					    throw new DemoiselleCrudException("Não foi possível verificar se campo é do tipo 'ENUM'");	
					}
				} 
            }
		}
		
		// If doesnt find any constant throws
		throw new DemoiselleCrudException("Não foi possível encontrar o valor [%s] nas constantes".replace("%s", value));
		
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
            throw new DemoiselleCrudException("Não foi possível realizar a conversão de Enum para Integer", e);
        }
	    
	    return null;
	}
	
	private List<Field> getAllFields(Class<?> clazz){
	    List<Field> fields = new ArrayList<>();
	    return CrudUtilHelper.getAllFields(fields, clazz);
	}
	
	protected Date convertStringToDate(String key, String value) {
		final String pattern = getDatePattern(value);
		final SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		//
		if (ISO8601_UTC_PATTERN.equals(pattern)) {
			formatter.setTimeZone(timeZoneUTC);
		}
		//
		try {
			return formatter.parse(value);
		} catch (final ParseException e) {
			throw new DemoiselleCrudException(String.format("Não foi possível converter a string (%s) para uma data", value), e);
		}
	}

    protected Boolean isLikeFilter(String value) {
        return value.startsWith("*") || value.endsWith("*");
    }
    
    protected boolean isRelationalFilter(String key, String value) {
        return value.startsWith(">") || value.startsWith("<");
    }

    protected boolean isDateFilter(String key, String value) {
    	try {
			DateUtils.parseDate(value, getDatePattern(value));
			//
			return true;
		} catch (final ParseException e) {
			return false;
		}
    }

    protected String getDatePattern(String date) {
    	if (date.endsWith("Z")) {
    		return ISO8601_UTC_PATTERN;
    	} else {
    		return ISO8601_PATTERN;
    	}
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
    
    @SuppressWarnings("unchecked")
	protected Predicate buildRelationalPredicate(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, From<?, ?> root, String key, String value, TreeNodeField<String, Set<String>> tnf) {
		final String pattern = value.trim();
		//
		if (pattern.startsWith(">=")) {
			return criteriaBuilder.greaterThanOrEqualTo(root.get(key), (Comparable<Object>)convertToAppropriateType(key, pattern.substring(2), tnf));
		} else if (pattern.startsWith("<=")) {
			return criteriaBuilder.lessThanOrEqualTo(root.get(key), (Comparable<Object>)convertToAppropriateType(key, pattern.substring(2), tnf));
		} else if (pattern.startsWith(">")) {
			return criteriaBuilder.greaterThan(root.get(key), (Comparable<Object>)convertToAppropriateType(key, pattern.substring(1), tnf));
		} else if (pattern.startsWith("<")) {
			return criteriaBuilder.lessThan(root.get(key), (Comparable<Object>)convertToAppropriateType(key, pattern.substring(1), tnf));
		} else {
			throw new DemoiselleCrudException("Operador relacional não encontrado no filtro");
		}
	}

	protected Comparable<?> convertToAppropriateType(String key, String value, TreeNodeField<String, Set<String>> tnf) {
		if (isEnumFilter(key, value, tnf)) {
			return convertEnumToInt(key, value, tnf);
		} else if (isDateFilter(key, value)) {
			return convertStringToDate(key, value);
		} else {
			return value;
		}
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