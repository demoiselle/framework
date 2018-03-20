package org.demoiselle.jee.crud.count;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Set;

import org.demoiselle.jee.crud.TreeNodeField;
import org.demoiselle.jee.crud.filter.FilterContext;
import org.demoiselle.jee.crud.helper.DemoiselleCrudHelper;

public class QueryCountHelper<T> {

    private final EntityManager em;
    private final Class<T> entityClass;

    public QueryCountHelper(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    public Long getResultCount(FilterContext filterContext) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countCriteria = cb.createQuery(Long.class);
        Root<T> entityRoot = countCriteria.from(entityClass);
        countCriteria.select(cb.count(entityRoot));

        TreeNodeField<String, Set<String>> defaultFields = filterContext.getDefaultFilters();
        DemoiselleCrudHelper.addSearchIfEnabledForQuery(filterContext, entityClass, cb, countCriteria, entityRoot, defaultFields);
        return em.createQuery(countCriteria).getSingleResult();
    }
}
