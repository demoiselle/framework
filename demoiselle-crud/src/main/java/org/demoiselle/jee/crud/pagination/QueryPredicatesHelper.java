package org.demoiselle.jee.crud.pagination;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.demoiselle.jee.crud.exception.DemoiselleCrudException;
import org.demoiselle.jee.crud.fields.FieldsContext;
import org.demoiselle.jee.crud.filter.FilterContext;

/**
 * This helper class is responsible for adding search parameters to a query, based on a given context. This
 * context can either be generated from the current request or manually.
 */
public class QueryPredicatesHelper<T> {
    private final Class<T> entityClass;
    private final FilterContext filterContext;

    public QueryPredicatesHelper(Class<T> entityClass, FilterContext filterContext) {
        this.filterContext = filterContext;
        this.entityClass = entityClass;
    }

    public Predicate[] buildPredicates(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, Root<T> root) {
        List<Predicate> predicates = new LinkedList<>();

        if (filterContext.getFilters() != null) {
            filterContext.getFilters().getChildren().stream().forEach(child -> {

                List<Predicate> predicateAndKeys = new LinkedList<>();
                List<Predicate> predicateSameKey = new LinkedList<>();

                // Many parameters for the same key, generate OR clause
                if (!child.getChildren().isEmpty()) {

                    Join<?, ?> join = root.join(child.getKey());
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
                                } else if (isEnumFilter(child.getKey(), value)) {
                                    predicateAndKeys.add(criteriaBuilder.equal(root.get(child.getKey()), convertEnumToInt(child.getKey(), value)));
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
                        } else if (value.equalsIgnoreCase("isTrue")) {
                            predicateAndKeys.add(criteriaBuilder.isTrue(root.get(child.getKey())));
                        } else if (value.equalsIgnoreCase("isFalse")) {
                            predicateAndKeys.add(criteriaBuilder.isFalse(root.get(child.getKey())));
                        } else if (isEnumFilter(child.getKey(), value)) {
                            predicateAndKeys.add(criteriaBuilder.equal(root.get(child.getKey()), convertEnumToInt(child.getKey(), value)));
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
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            if (key.equalsIgnoreCase(field.getName())) {
                return field.getType().isEnum();
            }
        }

        return false;
    }

    protected int convertEnumToInt(String key, String value) {
        Field[] fields = entityClass.getDeclaredFields();
        try {

            for (Field field : fields) {
                if (key.equals(field.getName())) {
                    if (field.getType().isEnum()) {
                        Class<?> c = Class.forName(field.getType().getName());
                        Object[] objects = c.getEnumConstants();
                        for (Object obj : objects) {
                            if (obj.toString().equalsIgnoreCase(value))
                                return ((Enum<?>)obj).ordinal();
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


    protected boolean isLikeFilter(String key, String value) {
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
}
