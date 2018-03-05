package org.demoiselle.jee.crud.pagination;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig;
import org.demoiselle.jee.crud.fields.FieldsContext;
import org.demoiselle.jee.crud.helper.DemoiselleCrudHelper;

/**
 * A Helper class that paginates results based on the PaginationContext and the FilterContext.
 * Both of them are given and are supposed to either be manually created or built from the request parameters.
 *
 * @param <T> The Entity Class of the query
 */
public class QueryPaginationHelper<T> {

    /**
     * The pagination context with the current pagination parameters.
     */
    private final PaginationContext paginationContext;

    /**
     * The global configuration for the Demoiselle CRUD class. Obtained as a RequestScoped bean.
     */
    private DemoiselleCrudConfig crudConfig;

    /**
     * The EntityManager for the query.
     */
    private EntityManager entityManager;

    /**
     * The root entity class for the query.
     */
    private Class<T> entityClass;

    /**
     * The filter context with the current query filter parameters.
     */
    private FieldsContext fieldsContext;

    /**
     * Create a new instance of the Helper for an EntityManager, an entity class and pagination/filter parameters
     * @param em The entity manager that will be used for the query.
     * @param entityClass The root entity class of the root query.
     * @param paginationContext The current pagination context/parameters.
     * @param fieldsContext The current filter context/parameters.
     * @param <T> The entity class type
     * @return A new QueryPaginationHelper instance for the givewn parameters.
     */
    public static <T> QueryPaginationHelper<T> createFor(EntityManager em, Class<T> entityClass, PaginationContext paginationContext, FieldsContext fieldsContext) {
        return new QueryPaginationHelper<>(em, entityClass, paginationContext, fieldsContext);
    }

    private QueryPaginationHelper(EntityManager entityManager, Class<T> entityClass, PaginationContext paginationContext, FieldsContext fieldsContext) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
        this.fieldsContext = fieldsContext;
        this.crudConfig = CDI.current().select(DemoiselleCrudConfig.class).get();
        this.paginationContext = paginationContext;
    }

    /**
     * Get the paginated result for the query, considering the current parameters of this specific QueryPaginationHelper instance.
     *
     * @param query The JPA query that will be paginated
     * @return The result list wrapped in a {@link Result} object, which will contain parameters such as the limit/offset for the pagination and the
     * search parameters.
     */
    public Result getPaginatedResult(Query query) {
        ResultSet result = new ResultSet();
        result.setFieldsContext(fieldsContext);
        if (paginationContext.isPaginationEnabled()) {
            Integer firstResult = paginationContext.getOffset() == null ? 0 : paginationContext.getOffset();
            Integer maxResults = getMaxResult();
            Long count = getResultCount();

            if (firstResult < count) {
                query.setFirstResult(firstResult);
                query.setMaxResults(maxResults);
            }

            result.setCount(count);
        }

        result.setContent(query.getResultList());
        if (result.getContent() != null && !result.getContent().isEmpty()
                && paginationContext.isPaginationEnabled()
                && result.getContent().size() <= result.getCount()
                && result.getCount() < getMaxResult()) {
            paginationContext.setLimit(result.getCount().intValue());
        }
        result.setEntityClass(entityClass);
        result.setPaginationContext(paginationContext);

        return result;
    }

    private Integer getMaxResult() {
        if (paginationContext.getLimit() == null || paginationContext.getOffset() == null) {
            return crudConfig.getDefaultPagination();
        }

        return (paginationContext.getLimit() - paginationContext.getOffset()) + 1;
    }

    private Long getResultCount() {
        return new DemoiselleCrudHelper<T>(entityManager, entityClass).getCount();
    }
}
