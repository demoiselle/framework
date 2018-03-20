package org.demoiselle.jee.crud.helper;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.CrudMessage;
import org.demoiselle.jee.crud.CrudUtilHelper;
import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.ReservedKeyWords;
import org.demoiselle.jee.crud.TreeNodeField;
import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig;
import org.demoiselle.jee.crud.field.FieldHelper;
import org.demoiselle.jee.crud.field.FieldHelperMessage;
import org.demoiselle.jee.crud.field.JsonFilterTransformer;
import org.demoiselle.jee.crud.field.QueryFieldsHelper;
import org.demoiselle.jee.crud.fields.FieldsContext;
import org.demoiselle.jee.crud.filter.FilterContext;
import org.demoiselle.jee.crud.filter.FilterHelper;
import org.demoiselle.jee.crud.filter.QueryPredicatesHelper;
import org.demoiselle.jee.crud.pagination.PaginationContext;
import org.demoiselle.jee.crud.pagination.PaginationHelper;
import org.demoiselle.jee.crud.pagination.PaginationHelperMessage;
import org.demoiselle.jee.crud.pagination.QueryPaginationHelper;
import org.demoiselle.jee.crud.pagination.ResultSet;
import org.demoiselle.jee.crud.sort.QuerySortHelper;
import org.demoiselle.jee.crud.sort.SortContext;
import org.demoiselle.jee.crud.sort.SortHelper;
import org.demoiselle.jee.crud.sort.SortHelperMessage;
import org.demoiselle.jee.crud.sort.SortModel;
import org.slf4j.LoggerFactory;

/**
 * The main objective if this class is to let developers use the Demoiselle CRUD features without the need to cling
 * to the AbstractREST/AbstractBusiness/AbstractDAO hierarchy. This way, features such as pagination and search can be used
 * in more complex scenarios where the given hierarchy is not desirable.
 *
 * @author SERPRO
 * @param <T> JPA Entity type that will be used for the queries.
 */
public class DemoiselleCrudHelper<T, V> {
    private final EntityManager em;
    private final Class<T> entityClass;
    private final SortHelperMessage sortHelperMessage;
    private Class<V> resultClass;
    private final DemoiselleRequestContext drc;
    private final DemoiselleCrudConfig crudConfig;
    private final CrudMessage crudMessage;
    private final FieldsContext fieldsContext;
    private final FilterContext filterContext;
    private final PaginationHelperMessage paginationHelperMessage;
    private final FieldHelperMessage fieldHelperMessage;
    private PaginationContext paginationContext;
    private final SortContext sortContext;
    private Function<T, V> resultTransformer;

    public static <T,V> DemoiselleCrudHelper<T,V> createUsingCDI(EntityManager em, Class<T> entityClass) {
        return new DemoiselleCrudHelper.Builder(em, entityClass).build();
    }

    public DemoiselleCrudHelper(EntityManager em, Class<T> entityClass, SortHelperMessage sortHelperMessage, Class<V> resultClass, DemoiselleRequestContext drc, DemoiselleCrudConfig crudConfig, CrudMessage crudMessage, FieldsContext fieldsContext, FilterContext filterContext, PaginationHelperMessage paginationHelperMessage, FieldHelperMessage fieldHelperMessage, PaginationContext paginationContext, SortContext sortContext, Function<T, V> resultTransformer) {
        this.em = em;
        this.entityClass = entityClass;
        this.sortHelperMessage = sortHelperMessage;
        this.resultClass = resultClass;
        this.drc = drc;
        this.crudConfig = crudConfig;
        this.crudMessage = crudMessage;
        this.fieldsContext = fieldsContext;
        this.filterContext = filterContext;
        this.paginationHelperMessage = paginationHelperMessage;
        this.fieldHelperMessage = fieldHelperMessage;
        this.paginationContext = paginationContext;
        this.sortContext = sortContext;
        this.resultTransformer = resultTransformer;
    }

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DemoiselleCrudHelper.class);

    /**
     * Execute the query, using the given features and parameters.
     *
     * @param criteriaQuery The criteria query that will be modified and executed. The modification will be done in-place.
     * @param root The JPA entity class that will be the root for the query.
     * @return A {@link Result} instance containing the entries and the query parameters used.
     */
    public Result executeQuery(CriteriaQuery<T> criteriaQuery, Root<T> root) {
        validateFilterFieldsIfEnabled();
        LOG.debug("Initializing a query");
        addSearchIfEnabled(criteriaQuery, root);
        addSortIfEnabled(criteriaQuery, root);

        ResultSet resultSet;
        TypedQuery<T> query = QueryFieldsHelper.createFilteredQuery(em, criteriaQuery, entityClass, fieldsContext);
        if (paginationContext.isPaginationEnabled()) {
            LOG.debug("Paginating the result for criteriaQuery = {}, root = {}", new Object[]{criteriaQuery, root});
            resultSet = QueryPaginationHelper
                    .createFor(em, crudConfig, entityClass, paginationContext, fieldsContext, filterContext)
                    .getPaginatedResult(query);
        } else {
            Query jpaQuery = QueryFieldsHelper.createFilteredQuery(em, criteriaQuery, entityClass, fieldsContext);
            resultSet = ResultSet.forList(
                    jpaQuery.getResultList(),
                    entityClass, paginationContext, fieldsContext);
        }
        if (resultClass != entityClass) {
            resultSet = ResultSet.transform(resultSet, resultClass, resultTransformer);
        }
        if (fieldsContext.isFieldsEnabled()) {
            List<String> fields;
            if (fieldsContext.getFlatFields() != null && !fieldsContext.getFlatFields().isEmpty()) {
                fields = fieldsContext.getFlatFields();
            } else {
                fields = fieldsContext.getAllowedFields();
            }
            resultSet = ResultSet.transform(resultSet, resultClass, new JsonFilterTransformer(resultClass, fields));
        }
        return resultSet;
    }

    private void validateFilterFieldsIfEnabled() {
        if (fieldsContext.isFieldsEnabled()) {
            LOG.debug("Field filtering is enabled, validating fields...");
            CrudUtilHelper.validateFlatFields(fieldsContext.getFlatFields(), fieldsContext.getAllowedFields(), resultClass, crudMessage);
        }
    }

    /**
     * Add sorting parameters if it's been enabled from the request or explicitly.
     *
     * @param criteriaQuery The criteria query that may be modified to use sorting.
     * @param root The root JPA entity class for the query.
     */
    private void addSortIfEnabled(CriteriaQuery<T> criteriaQuery, Root<T> root) {
        if (sortContext.isSortEnabled()) {
            if (drc.getDemoiselleResultAnnotation() != null) {
                SortHelper.validateSorts(sortContext.getSorts(), Arrays.asList(drc.getDemoiselleResultAnnotation().searchFields()), entityClass);
            }
            new QuerySortHelper(sortContext).configureOrder(em.getCriteriaBuilder(), criteriaQuery, root);
        }
    }

    /**
     * Add predicates to the criteria, based on the parameters, if any of them have been enabled from the request or explicitly.

     * @param criteriaQuery The criteria query that may be modified to use the search parameters.
     * @param root The root JPA entity class for the query.
     */
    private void addSearchIfEnabled(CriteriaQuery criteriaQuery, Root root) {
        TreeNodeField<String, Set<String>> defaultFields = CrudUtilHelper.extractSearchFieldsFromAnnotation(drc.getDemoiselleResultAnnotation(), entityClass);
        addSearchIfEnabledForQuery(filterContext, entityClass, em.getCriteriaBuilder(), criteriaQuery, root, defaultFields);
    }

    /**
     * Add predicates to the criteria, based on the parameters, if any of them have been enabled from the request or explicitly.
     *
     * @param filterContext
     * @param entityClass
     * @param cb
     * @param criteriaQuery
     * @param root
     */
    public static void addSearchIfEnabledForQuery(FilterContext filterContext,
                                                   Class<?> entityClass,
                                                   CriteriaBuilder cb,
                                                   CriteriaQuery criteriaQuery,
                                                   Root root,
                                                   TreeNodeField<String, Set<String>> defaultFields) { LOG.debug("Adding search for query if needed");
        if (filterContext.isFilterEnabled() && filterContext.getFilters() != null) {
            LOG.debug("Detected that search needs to be enabled for this query");
            CrudMessage crudMessage = CDI.current().select(CrudMessage.class).get();
            CrudUtilHelper.validateFields(filterContext.getFilters(), defaultFields, crudMessage, entityClass);
            QueryPredicatesHelper predicatesHelper = new QueryPredicatesHelper(entityClass, filterContext);
            criteriaQuery.where(predicatesHelper.buildPredicates(cb, criteriaQuery, root));
        }
    }

    public Long getCount() {
        return getCount(filterContext);
    }

    public Long getCount(FilterContext filterContextArg) {
        LOG.debug("Getting count for the query ");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countCriteria = cb.createQuery(Long.class);
        Root<T> entityRoot = countCriteria.from(entityClass);
        countCriteria.select(cb.count(entityRoot));

        TreeNodeField<String, Set<String>> defaultFields = filterContextArg.getDefaultFilters();
        addSearchIfEnabledForQuery(filterContextArg, entityClass, cb, countCriteria, entityRoot, defaultFields);
        return em.createQuery(countCriteria).getSingleResult();
    }


    public static class Builder<T,V> {

        private EntityManager em;
        private Class<T> entityClass;
        private SortHelperMessage sortHelperMessage;
        private Class<V> resultClass;
        private DemoiselleRequestContext drc;
        private DemoiselleCrudConfig crudConfig;
        private CrudMessage crudMessage;
        private FieldsContext fieldsContext = FieldsContext.disabledFields();
        private FilterContext filterContext = FilterContext.disabledFilter();
        private PaginationHelperMessage paginationHelperMessage;
        private FieldHelperMessage fieldHelperMessage;
        private PaginationContext paginationContext = PaginationContext.disabledPagination();
        private SortContext sortContext = SortContext.disabledSort();
        private Function<T, V> resultTransformer;

        public Builder(EntityManager em, Class<T> entityClass) {
            this(true, em, entityClass);
        }

        public Builder(boolean useCDI, EntityManager em, Class<T> entityClass) {
            if (useCDI) {
                initUsingCDI();
            }
            this.em = em;
            this.entityClass = entityClass;
        }

        public Builder setSortHelperMessage(SortHelperMessage sortHelperMessage) {
            this.sortHelperMessage = sortHelperMessage;
            return this;
        }

        public Builder setResultClass(Class<V> resultClass) {
            this.resultClass = resultClass;
            return this;
        }

        public Builder setDemoiselleRequestContext(DemoiselleRequestContext drc) {
            this.drc = drc;
            return this;
        }

        public Builder setCrudConfig(DemoiselleCrudConfig crudConfig) {
            this.crudConfig = crudConfig;
            return this;
        }

        public Builder setCrudMessage(CrudMessage crudMessage) {
            this.crudMessage = crudMessage;
            return this;
        }

        public Builder setFieldsContext(FieldsContext fieldsContext) {
            this.fieldsContext = fieldsContext;
            return this;
        }

        public Builder setFilterContext(FilterContext filterContext) {
            this.filterContext = filterContext;
            return this;
        }

        public Builder setPaginationHelperMessage(PaginationHelperMessage paginationHelperMessage) {
            this.paginationHelperMessage = paginationHelperMessage;
            return this;
        }

        public Builder setFieldHelperMessage(FieldHelperMessage fieldHelperMessage) {
            this.fieldHelperMessage = fieldHelperMessage;
            return this;
        }

        public Builder setPaginationContext(PaginationContext paginationContext) {
            this.paginationContext = paginationContext;
            return this;
        }

        public Builder setSortContext(SortContext sortContext) {
            this.sortContext = sortContext;
            return this;
        }

        public Builder setResultTransformer(Function<T, V> resultTransformer) {
            this.resultTransformer = resultTransformer;
            return this;
        }


        public DemoiselleCrudHelper build() {
            return new DemoiselleCrudHelper(em, entityClass, sortHelperMessage, resultClass, drc, crudConfig, crudMessage, fieldsContext, filterContext, paginationHelperMessage, fieldHelperMessage, paginationContext, sortContext, resultTransformer);
        }

        private void initUsingCDI() {
            this.drc = CDI.current().select(DemoiselleRequestContext.class).get();
            this.drc.setEntityClass(entityClass);
            if (this.drc.getResultClass() != Object.class) {
                this.resultClass = (Class<V>) this.drc.getResultClass();
            } else {
                this.resultClass = (Class<V>) this.entityClass;
            }
            if (drc.getResultTransformer() != null) {
                this.resultTransformer = drc.getResultTransformer();
            } else {
                this.resultTransformer = (Function<T,V>)Function.identity();
            }
            this.paginationContext = drc.getPaginationContext().copy();
            this.fieldsContext = drc.getFieldsContext().copy();
            this.filterContext = drc.getFilterContext().copy();
            this.sortContext = drc.getSortContext().copy();
            this.crudConfig = CDI.current().select(DemoiselleCrudConfig.class).get();
            this.fieldHelperMessage = CDI.current().select(FieldHelperMessage.class).get();
            this.crudMessage = CDI.current().select(CrudMessage.class).get();
            this.sortHelperMessage = CDI.current().select(SortHelperMessage.class).get();
            this.paginationHelperMessage = CDI.current().select(PaginationHelperMessage.class).get();
        }

        public Builder setPaginationLimit(int limit) {
            paginationContext.setLimit(limit);
            return this;
        }

        public Builder setPaginationOffset(int offset) {
            paginationContext.setOffset(offset);
            return this;
        }

        public Builder setPaginationRange(int maxPageSize, String... ranges) {
            paginationContext = PaginationHelper.createContextFromRange(paginationHelperMessage, maxPageSize, Arrays.asList(ranges));
            return this;
        }

        public Builder setFilterFields(String... fields) {
            fieldsContext.setFlatFields(Arrays.asList(fields));
            return this;
        }

        public Builder enableSearchFromRequest() {
            HttpServletRequest servletRequest = CDI.current().select(HttpServletRequest.class).get();
            Map<String, String[]> map = servletRequest.getParameterMap();
            MultivaluedMap<String, String> multivaluedMap = generateMultivalueMapFrom(map);
            return enableSearchWithParameters(multivaluedMap);
        }

        public Builder enableSearchWithParameters(Map.Entry<String, String>... entries) {
            MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();
            Stream.of(entries).forEach(entry -> multivaluedMap.add(entry.getKey(), entry.getValue()));
            return enableSearchWithParameters(multivaluedMap);
        }

        private Builder enableSearchWithParameters(MultivaluedMap<String, String> parameterMap) {
            filterContext.setFilterEnabled(true);
            filterContext.setFilters(FilterHelper.extractFiltersFromParameterMap(entityClass,
                    parameterMap
            ));
            return this;
        }

        public Builder disableSearch() {
            filterContext.setFilterEnabled(false);
            return this;
        }

        private MultivaluedMap<String, String> generateMultivalueMapFrom(Map<String, String[]> map) {
            MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();
            for (Map.Entry<String, String[]> entry : map.entrySet()) {
                Arrays.stream(entry.getValue())
                        .forEach(value -> multivaluedMap.add(entry.getKey(), value));
            }
            return multivaluedMap;
        }

        public Builder enablePaginationFromRequest() {
            paginationContext.setPaginationEnabled(true);
            HttpServletRequest servletRequest = CDI.current().select(HttpServletRequest.class).get();

            MultivaluedMap<String, String> paramMap = generateMultivalueMapFrom(servletRequest.getParameterMap());
            if (paramMap.containsKey(ReservedKeyWords.DEFAULT_RANGE_KEY.getKey())) {
                List<String> rangeList = paramMap.get(ReservedKeyWords.DEFAULT_RANGE_KEY.getKey());
                enablePaginationWithRanges(rangeList);
            }
            return this;
        }

        public Builder enablePaginationWithRanges(String... ranges) {
            return enablePaginationWithRanges(Arrays.asList(ranges));
        }

        public Builder enablePaginationWithRanges(List<String> ranges) {
            paginationContext.setPaginationEnabled(true);
            DemoiselleCrudConfig crudConfig = CDI.current().select(DemoiselleCrudConfig.class).get();
            paginationContext = PaginationHelper.createContextFromRange(paginationHelperMessage, crudConfig.getDefaultPagination(), ranges);
            return this;
        }

        public Builder enableFilterFromRequest(String... allowedFields) {
            List<String> allowedFieldsArr = Arrays.asList("*");
            if (allowedFields.length == 0) {
                if (drc != null && drc.getFieldsContext().getAllowedFields() != null) {
                    allowedFieldsArr = drc.getFieldsContext().getAllowedFields();
                }
            } else {
                allowedFieldsArr = Arrays.asList(allowedFields);
            }
            return enableFilterFromRequest(allowedFieldsArr);
        }

        public Builder enableFilterFromRequest(List<String> allowedFields) {
            LOG.debug("Habilitando o filtro buscando os campos através do Request");
            HttpServletRequest servletRequest = CDI.current().select(HttpServletRequest.class).get();

            MultivaluedMap<String, String> paramMap = generateMultivalueMapFrom(servletRequest.getParameterMap());
            List<String> queryStringFields = FieldHelper.extractQueryStringFieldsFromMap(paramMap, fieldHelperMessage);

            enableFilterForFields(queryStringFields, allowedFields);
            return this;
        }

        public Builder enableFilterForFields(String... fields) {
            return enableFilterForFields(Arrays.asList(fields), Arrays.asList("*"));
        }

        public Builder enableFilterForFields(List<String> fields, List<String> allowedFields) {
            if (fields.isEmpty()) {
                return this;
            }
            fieldsContext.setFieldsEnabled(true);
            fieldsContext.setFlatFields(fields);
            fieldsContext.setAllowedFields(allowedFields);
            return this;
        }

        public Builder enableSort(List<SortModel>  sortModels) {
            sortContext.setSortEnabled(true);
            sortContext.setSorts(sortModels);
            return this;
        }

        public Builder enableSort(SortModel... sortModels) {
            enableSort(Arrays.asList(sortModels));
            return this;
        }
        public Builder enableSortFromRequest() {
            HttpServletRequest servletRequest = CDI.current().select(HttpServletRequest.class).get();

            MultivaluedMap<String, String> paramMap = generateMultivalueMapFrom(servletRequest.getParameterMap());
            List<SortModel> sorts = SortHelper.extractSortsFromParameterMap(paramMap, crudMessage, sortHelperMessage);
            enableSort(sorts);
            return this;
        }

        public Builder disableSort() {
            sortContext.setSortEnabled(false);
            return this;
        }

        public Builder transformResultWith(Class resultClassArg, Function resultTransformerFn) {
            resultTransformer = resultTransformerFn;
            resultClass = resultClassArg;
            return this;
        }

        public Builder disableTransform() {
            resultClass = (Class<V>) entityClass;
            resultTransformer = (Function<T,V>)Function.identity();
            return this;
        }
    }
}
