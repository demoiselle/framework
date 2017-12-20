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
import java.util.TimeZone;
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

//TODO CLF revisar
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public abstract class AbstractDAO<T, I> implements Crud<T, I> {
	/**
	 * Formato da data/hora. ISO8601.
	 */
	private static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	/**
	 * Time zone padrão. UTC.
	 */
	private static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");

    @Inject
    private PaginationHelperConfig paginationConfig;

    @Inject
    private DemoiselleRequestContext drc;

    private final Class<T> entityClass;

    protected abstract EntityManager getEntityManager();

    private final Logger logger = Logger.getLogger(this.getClass().getName());

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
        } catch (final Exception e) {
            throw new DemoiselleCrudException("Não foi possível salvar", e);
        }
    }

    @Override
    public T mergeHalf(I id, T entity) {
        try {
            final StringBuilder sb = new StringBuilder();
            final Map<String, Object> params = new ConcurrentHashMap<>();
            //
            sb.append("UPDATE ");
            sb.append(entityClass.getCanonicalName());
            sb.append(" SET ");
            //
            for (final Field field : entityClass.getDeclaredFields()) {
                if (!field.isAnnotationPresent(ManyToOne.class)) {
                    final Column column = field.getAnnotation(Column.class);
                    //
                    if (column == null || !column.updatable()) {
                        continue;
                    }
                }
                //
                field.setAccessible(true);
                //
                final String name = field.getName();
                final Object value = field.get(entity);
                //
                if (value != null) {
                    if (!params.isEmpty()) {
                        sb.append(", ");
                    }
                    //
                    sb.append(name).append(" = :").append(name);
                    params.putIfAbsent(name, value);
                }
            }
            //
            if (!params.isEmpty()) {
                final String idName
                        = CrudUtilHelper.getMethodAnnotatedWithID(entityClass);
                //
                sb.append(" WHERE ").append(idName).append(" = :").append(idName);
                params.putIfAbsent(idName, id);
                //
                final Query query = getEntityManager().createQuery(sb.toString());
                //
                for (final Map.Entry<String, Object> entry : params.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
                //
                query.executeUpdate();
            }
            //
            return entity;
        } catch (final Exception e) {
            throw new DemoiselleCrudException("Não foi possível salvar", e);
        }
    }

    @Override
    public T mergeFull(T entity) {
        try {
            getEntityManager().merge(entity);
            return entity;
        } catch (final Exception e) {
            // TODO: CLF Severe? Pode cair aqui somente por ter violação de Unique
            throw new DemoiselleCrudException("Não foi possível salvar", e);
        }
    }

    @Override
    public void remove(I id) {
        try {
            getEntityManager().remove(getEntityManager().find(entityClass, id));
        } catch (final Exception e) {
            throw new DemoiselleCrudException("Não foi possível excluir", e);
        }
    }

    @Override
    public T find(I id) {
        try {
            return getEntityManager().find(entityClass, id);
        } catch (final Exception e) {
            throw new DemoiselleCrudException("Não foi possível consultar", e);
        }

    }

    @Override
    public Result find() {

        try {

            final Result result = new ResultSet();

            final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            final CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);

            configureCriteriaQuery(criteriaBuilder, criteriaQuery);

            final TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);

            if (drc.isPaginationEnabled()) {
                final Integer firstResult = drc.getOffset() == null ? 0 : drc.getOffset();
                final Integer maxResults = getMaxResult();
                final Long count = count();

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

        } catch (final Exception e) {
            logger.severe(e.getMessage());
            throw new DemoiselleCrudException("Não foi possível consultar", e);
        }
    }

    protected void configureCriteriaQuery(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> criteriaQuery) {
        final Root<T> from = criteriaQuery.from(entityClass);
        if (drc.getFilters() != null) {
            criteriaQuery.select(from).where(buildPredicates(criteriaBuilder, criteriaQuery, from));
        }

        configureOrder(criteriaBuilder, criteriaQuery, from);
    }

    protected void configureOrder(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> criteriaQuery, Root<T> root) {

        if (!drc.getSorts().isEmpty()) {
            final List<Order> orders = new ArrayList<>();

            drc.getSorts().stream().forEachOrdered(sortModel -> {

                if (sortModel.getType().equals(CrudSort.ASC)) {
                    orders.add(criteriaBuilder.asc(root.get(sortModel.getField())));
                } else {
                    orders.add(criteriaBuilder.desc(root.get(sortModel.getField())));
                }
            });

            criteriaQuery.orderBy(orders);
        }

    }

    protected Predicate[] buildPredicates(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, Root<T> root) {
        final List<Predicate> predicates = new LinkedList<>();

        if (drc.getFilters() != null) {
            drc.getFilters().getChildren().stream().forEach(child -> {

                final List<Predicate> predicateAndKeys = new LinkedList<>();
                final List<Predicate> predicateSameKey = new LinkedList<>();

                // Many parameters for the same key, generate OR clause
                if (!child.getChildren().isEmpty()) {

                    final Join<?, ?> join = root.join(child.getKey());
                    child.getChildren().stream().forEach(values -> {

                        predicateSameKey.clear();

                        if (!child.getChildren().isEmpty()) {

                            values.getValue().stream().forEach(value -> {
                                if ("null".equals(value) || value == null) {
                                    predicateSameKey.add(criteriaBuilder.isNull(join.get(values.getKey())));
                                } else if (values.getValue().isEmpty()) {
                                    predicateSameKey.add(criteriaBuilder.isEmpty(join.get(values.getKey())));
                                } else if (isLikeFilter(values.getKey(), value)) {
                                    predicateSameKey.add(buildLikePredicate(criteriaBuilder, criteriaQuery, join, values.getKey(), value));
                                } else if (isRelationalFilter(values.getKey(), value)) {
                                    predicateSameKey.add(buildRelationalPredicate(criteriaBuilder, criteriaQuery, join, values.getKey(), value));
                                } else if (isEnumFilter(child.getKey(), value)) {
                                	predicateAndKeys.add(criteriaBuilder.equal(root.get(child.getKey()), convertEnumToInt(child.getKey(), value)));
                                } else if (isDateFilter(child.getKey(), value)) {
                                	predicateAndKeys.add(criteriaBuilder.equal(root.get(child.getKey()), convertStringToDate(child.getKey(), value)));
                                } else {
                                    predicateSameKey.add(criteriaBuilder.equal(join.get(values.getKey()), value));
                                }
                            });

                            predicates.add(criteriaBuilder.or(predicateSameKey.toArray(new Predicate[]{})));
                        }
                    });
                } else {
                    child.getValue().stream().forEach(value -> {
                        if ("null".equals(value) || value == null) {
                            predicateAndKeys.add(criteriaBuilder.isNull(root.get(child.getKey())));
                        } else if (child.getValue().isEmpty()) {
                            predicateAndKeys.add(criteriaBuilder.isEmpty(root.get(child.getKey())));
                        } else if (isLikeFilter(child.getKey(), value)) {
                            predicateAndKeys.add(buildLikePredicate(criteriaBuilder, criteriaQuery, root, child.getKey(), value));
                        } else if (isRelationalFilter(child.getKey(), value)) {
                            predicateAndKeys.add(buildRelationalPredicate(criteriaBuilder, criteriaQuery, root, child.getKey(), value));
                        } else if (value.equalsIgnoreCase("isTrue")) {
                            predicateAndKeys.add(criteriaBuilder.isTrue(root.get(child.getKey())));
                        } else if (value.equalsIgnoreCase("isFalse")) {
                            predicateAndKeys.add(criteriaBuilder.isFalse(root.get(child.getKey())));
                        } else if (isEnumFilter(child.getKey(), value)) {
                        	predicateAndKeys.add(criteriaBuilder.equal(root.get(child.getKey()), convertEnumToInt(child.getKey(), value)));
                        } else if (isDateFilter(child.getKey(), value)) {
                        	predicateAndKeys.add(criteriaBuilder.equal(root.get(child.getKey()), convertStringToDate(child.getKey(), value)));
                        } else {
                            predicateAndKeys.add(criteriaBuilder.equal(root.get(child.getKey()), value));
                        }
                    });

                    predicates.add(criteriaBuilder.and(predicateAndKeys.toArray(new Predicate[]{})));
                }
            });
        }

        return predicates.toArray(new Predicate[]{});
    }

    protected boolean isEnumFilter(String key, String value) {
		final Field[] fields = entityClass.getDeclaredFields();

		for (final Field field : fields) {
			if (key.equalsIgnoreCase(field.getName())) {
				return field.getType().isEnum();
			}
		}

		return false;
	}

	protected int convertEnumToInt(String key, String value) {
		final Field[] fields = entityClass.getDeclaredFields();
		try {

			for (final Field field : fields) {
				if (key.equals(field.getName())) {
					if (field.getType().isEnum()) {
						final Class<?> c = Class.forName(field.getType().getName());
						final Object[] objects = c.getEnumConstants();
						for (final Object obj : objects) {
							if (obj.toString().equalsIgnoreCase(value)) {
								return ((Enum<?>)obj).ordinal();
							}
						}
					} else {
						throw new DemoiselleCrudException("Não foi possível consultar");
					}
				}
			}

			// If doesnt find any constant throws
			throw new DemoiselleCrudException("Não foi possível encontrar o valor [%s] nas constantes".replace("%s", value));

		} catch (IllegalArgumentException | ClassNotFoundException | SecurityException e) {
			throw new DemoiselleCrudException("Não foi possível consultar", e);
		}

	}

	protected Date convertStringToDate(String key, String value) {
		final SimpleDateFormat formatter = new SimpleDateFormat(ISO8601_PATTERN);
		//
		formatter.setTimeZone(timeZoneUTC);
		//
		try {
			return formatter.parse(value);
		} catch (final ParseException e) {
			throw new DemoiselleCrudException(String.format("Não foi possível converter a string (%s) para uma data", value), e);
		}
	}

    protected boolean isLikeFilter(String key, String value) {
        return value.startsWith("*") || value.endsWith("*");
    }

    protected boolean isRelationalFilter(String key, String value) {
        return value.startsWith(">") || value.startsWith("<");
    }

    protected boolean isDateFilter(String key, String value) {
    	try {
			DateUtils.parseDate(value, ISO8601_PATTERN);
			//
			return true;
		} catch (final ParseException e) {
			return false;
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
	protected Predicate buildRelationalPredicate(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, From<?, ?> root, String key, String value) {
    	final String pattern = value.trim();
    	//
    	if (pattern.startsWith(">=")) {
    		return criteriaBuilder.greaterThanOrEqualTo(root.get(key), (Comparable<Object>)convertToAppropriateType(key, pattern.substring(2)));
    	} else if (pattern.startsWith("<=")) {
    		return criteriaBuilder.lessThanOrEqualTo(root.get(key), (Comparable<Object>)convertToAppropriateType(key, pattern.substring(2)));
    	} else if (pattern.startsWith(">")) {
    		return criteriaBuilder.greaterThan(root.get(key), (Comparable<Object>)convertToAppropriateType(key, pattern.substring(1)));
    	} else if (pattern.startsWith("<")) {
    		return criteriaBuilder.lessThan(root.get(key), (Comparable<Object>)convertToAppropriateType(key, pattern.substring(1)));
    	} else {
    		throw new DemoiselleCrudException("Operador relacional não encontrado no filtro");
    	}
    }

    protected Comparable<?> convertToAppropriateType(String key, String value) {
    	if (isEnumFilter(key, value)) {
    		return convertEnumToInt(key, value);
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

        return drc.getLimit() - drc.getOffset() + 1;
    }

    public Long count() {
        final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<Long> countCriteria = criteriaBuilder.createQuery(Long.class);
        final Root<T> entityRoot = countCriteria.from(entityClass);
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
}