/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.cache.Cacheable;
import org.demoiselle.jee.crud.cache.QueryCacheStore;
import org.demoiselle.jee.crud.field.FieldHelper;
import org.demoiselle.jee.crud.filter.FilterHelper;
import org.demoiselle.jee.crud.pagination.LinkHeaderBuilder;
import org.demoiselle.jee.crud.pagination.PageResult;
import org.demoiselle.jee.crud.pagination.PaginationHelper;
import org.demoiselle.jee.crud.sort.SortHelper;

/**
 * Class responsible for managing the Request and Response used on CRUD feature.
 * 
 * The request will be treat if:
 *  - The target class of request is a subclass of {@link AbstractREST} and 
 *  - The target method of request is annotated with {@link GET} annotation
 *  
 *  The request will be treated and parsed for:
 *  - {@link PaginationHelper} to extract information about 'pagination' like a 'range' parameter;
 *  - {@link FieldHelper} to extract information about 'field' like a 'fields=field1,field2,...' parameter;
 *  - {@link FilterHelper} to extract information about the fields of entity that will be filter on the database.
 *  - {@link SortHelper} to extract information about the 'sort' link a 'sort' and 'desc' parameters;
 *  
 * The response will be treat if:
 *  - The type of return is a {@link Result} type.
 *  
 *  The response will build the result and the HTTP Headers.
 *
 * @author SERPRO
 */
@Provider
public class CrudFilter implements ContainerResponseFilter, ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private UriInfo uriInfo;

    @Inject
    private DemoiselleRequestContext drc;

    @Inject
    private PaginationHelper paginationHelper;

    @Inject
    private SortHelper sortHelper;

    @Inject
    private FilterHelper filterHelper;

    @Inject
    private FieldHelper fieldHelper;

    @Inject
    private ReflectionCache reflectionCache;

    @Inject
    private QueryCacheStore queryCacheStore;
    
    private static final Logger logger = Logger.getLogger(CrudFilter.class.getName());

    /**
     * HTTP header indicating whether the response was served from cache.
     * Values: {@code HIT} (served from cache) or {@code MISS} (freshly computed).
     */
    static final String HTTP_HEADER_X_CACHE = "X-Cache";

    /**
     * Request context property key used to pass a cached result from the
     * request filter to the response filter.
     */
    static final String CACHE_HIT_PROPERTY = "demoiselle.crud.cache.hit";

    /**
     * Default maximum depth for recursive field projection.
     * Configurable via {@code demoiselle.crud.field.maxDepth}.
     */
    static final int DEFAULT_MAX_FIELD_DEPTH = 10;

    public CrudFilter() {}

    public CrudFilter(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleRequestContext drc, PaginationHelper paginationHelper, SortHelper sortHelper, FilterHelper filterHelper, FieldHelper fieldHelper, ReflectionCache reflectionCache, QueryCacheStore queryCacheStore) {
        this.resourceInfo = resourceInfo;
        this.uriInfo = uriInfo;
        this.drc = drc;
        this.paginationHelper = paginationHelper;
        this.sortHelper = sortHelper;
        this.filterHelper = filterHelper;
        this.fieldHelper = fieldHelper;
        this.reflectionCache = reflectionCache;
        this.queryCacheStore = queryCacheStore;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (isRequestForCrud()) {
            try {
                paginationHelper.execute(resourceInfo, uriInfo);
                sortHelper.execute(resourceInfo, uriInfo);
                filterHelper.execute(resourceInfo, uriInfo);
                fieldHelper.execute(resourceInfo, uriInfo);

                // Check cache for @Cacheable GET methods
                Cacheable cacheable = findCacheableAnnotation();
                if (cacheable != null && queryCacheStore != null) {
                    String cacheKey = buildFilterCacheKey();
                    Object cached = queryCacheStore.get(cacheKey);
                    if (cached != null) {
                        logger.log(Level.FINE, "Cache HIT for key: {0}", cacheKey);
                        // Store cached result in request context for the response filter
                        requestContext.setProperty(CACHE_HIT_PROPERTY, cached);
                    }
                }
            } 
            catch (IllegalArgumentException e) {
                throw new BadRequestException(e.getMessage());
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext response) throws IOException {

        if (response.getEntity() instanceof Result) {

            // Check if the request filter found a cache HIT
            Object cachedResult = req.getProperty(CACHE_HIT_PROPERTY);
            if (cachedResult != null) {
                response.setEntity(cachedResult);
                response.getHeaders().putSingle(HTTP_HEADER_X_CACHE, "HIT");
                response.setStatus(Status.OK.getStatusCode());
                logger.log(Level.FINE, "Serving cached result for request");
                return;
            }

            buildHeaders(response);
            
            Object body = buildContentBody(response);
            response.setEntity(body);

            // Cache the response body for @Cacheable methods
            Cacheable cacheable = findCacheableAnnotation();
            if (cacheable != null && queryCacheStore != null) {
                String cacheKey = buildFilterCacheKey();
                queryCacheStore.put(cacheKey, body, cacheable.ttl());
                response.getHeaders().putSingle(HTTP_HEADER_X_CACHE, "MISS");
                logger.log(Level.FINE, "Cache MISS — stored result for key: {0}", cacheKey);
            }

            if (!paginationHelper.isPartialContentResponse()) {
                response.setStatus(Status.OK.getStatusCode());
            } 
            else {
                response.setStatus(Status.PARTIAL_CONTENT.getStatusCode());
            }
        } 
        else {
            if (Status.BAD_REQUEST.getStatusCode() == response.getStatus() && drc.getEntityClass() == null) {
                paginationHelper.buildAcceptRangeWithResponse(response);
            }
        }

    }

    /**
     * Build all HTTP Headers.
     * 
     */
    private void buildHeaders(ContainerResponseContext response) {
        String exposeHeaders = ReservedHTTPHeaders.HTTP_HEADER_ACCEPT_RANGE.getKey() + ", " + ReservedHTTPHeaders.HTTP_HEADER_CONTENT_RANGE.getKey() + ", " + HttpHeaders.LINK;

        // Add PageResult metadata headers when the entity is a PageResult
        if (response.getEntity() instanceof PageResult<?> pageResult) {
            response.getHeaders().putSingle(ReservedHTTPHeaders.HTTP_HEADER_TOTAL_COUNT.getKey(), pageResult.totalElements());
            response.getHeaders().putSingle(ReservedHTTPHeaders.HTTP_HEADER_TOTAL_PAGES.getKey(), pageResult.totalPages());
            response.getHeaders().putSingle(ReservedHTTPHeaders.HTTP_HEADER_CURRENT_PAGE.getKey(), pageResult.currentPage());
            response.getHeaders().putSingle(ReservedHTTPHeaders.HTTP_HEADER_PAGE_SIZE.getKey(), pageResult.pageSize());
            response.getHeaders().putSingle(ReservedHTTPHeaders.HTTP_HEADER_HAS_NEXT.getKey(), pageResult.hasNext());
            response.getHeaders().putSingle(ReservedHTTPHeaders.HTTP_HEADER_HAS_PREVIOUS.getKey(), pageResult.hasPrevious());

            // Build RFC 8288 Link header from PageResult metadata
            String baseUri = uriInfo.getRequestUri().toString().replaceFirst("[?&]range=[^&]*", "");
            String linkHeader = LinkHeaderBuilder.build(baseUri, pageResult);
            if (!linkHeader.isEmpty()) {
                response.getHeaders().putSingle(ReservedHTTPHeaders.HTTP_HEADER_LINK.getKey(), linkHeader);
            }

            exposeHeaders += ", " + ReservedHTTPHeaders.HTTP_HEADER_TOTAL_COUNT.getKey()
                    + ", " + ReservedHTTPHeaders.HTTP_HEADER_TOTAL_PAGES.getKey()
                    + ", " + ReservedHTTPHeaders.HTTP_HEADER_CURRENT_PAGE.getKey()
                    + ", " + ReservedHTTPHeaders.HTTP_HEADER_PAGE_SIZE.getKey()
                    + ", " + ReservedHTTPHeaders.HTTP_HEADER_HAS_NEXT.getKey()
                    + ", " + ReservedHTTPHeaders.HTTP_HEADER_HAS_PREVIOUS.getKey()
                    + ", " + ReservedHTTPHeaders.HTTP_HEADER_LINK.getKey();
        }

        response.getHeaders().putSingle(ReservedHTTPHeaders.HTTP_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS.getKey(), exposeHeaders);
        paginationHelper.buildHeaders(resourceInfo, uriInfo).forEach((k, v) -> response.getHeaders().putSingle(k, v));
    }

    /**
     * Check if the actual request is valid for a Crud feature.
     * 
     * @return is a request for crud or not
     */
    private Boolean isRequestForCrud() {
        if (AbstractREST.class.isAssignableFrom(resourceInfo.getResourceClass())
                && resourceInfo.getResourceMethod().isAnnotationPresent(GET.class)) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     * Build the result used on 'Body' HTTP Response.
     * 
     * If the request used the {@link FieldHelper} feature or used the {@link Search} annotation the 
     * result from database will be parsed to a Map to filter theses fields.
     * 
     * @param response
     * @return result
     */
    private Object buildContentBody(ContainerResponseContext response) {

        @SuppressWarnings("unchecked")
        List<Object> content = (List<Object>) ((Result) response.getEntity()).getContent();
        
        TreeNodeField<String, Set<String>> fields = getFields();
        
        if(fields != null){
            content = new LinkedList<>();
            Class<?> targetClass = CrudUtilHelper.getTargetClass(resourceInfo.getResourceClass());
            Iterator<?> it = ((Result) response.getEntity()).getContent().iterator();
            
            while(it.hasNext()){
                Object object = it.next();
                content.add(resolveFields(object, fields, targetClass, DEFAULT_MAX_FIELD_DEPTH));
            }
        }
        return content;
    }

    /**
     * Recursively resolves field projections from the given object based on the
     * tree of requested fields.
     *
     * <p>Leaf nodes (no children) extract the direct field value.
     * Intermediate nodes recurse into the nested object up to {@code maxDepth} levels.</p>
     *
     * @param object      the source object to extract values from
     * @param node        the current tree node describing requested fields
     * @param targetClass the class used for reflection lookups
     * @param maxDepth    remaining recursion depth (0 = stop recursing)
     * @return a map of field name → value (or nested map for intermediate nodes)
     */
    Map<String, Object> resolveFields(Object object, TreeNodeField<String, Set<String>> node, Class<?> targetClass, int maxDepth) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (TreeNodeField<String, Set<String>> child : node.getChildren()) {
            if (child.getChildren().isEmpty()) {
                // Leaf: extract direct value
                Object value = getFieldValue(object, child.getKey(), targetClass);
                if (value != null) {
                    result.put(child.getKey(), value);
                }
            } else if (maxDepth > 0) {
                // Intermediate node: recurse into nested object
                Object nestedObject = getFieldValue(object, child.getKey(), targetClass);
                if (nestedObject != null) {
                    Class<?> nestedClass = nestedObject.getClass();
                    result.put(child.getKey(), resolveFields(nestedObject, child, nestedClass, maxDepth - 1));
                }
            }
        }
        return result;
    }

    /**
     * Extracts the value of a named field from the given object using the
     * {@link ReflectionCache}.
     *
     * @param object      the object to read the field from
     * @param fieldName   the name of the field
     * @param targetClass the class used for cached field lookup
     * @return the field value, or {@code null} if the field does not exist or an error occurs
     */
    private Object getFieldValue(Object object, String fieldName, Class<?> targetClass) {
        try {
            Map<String, Field> cachedFields = reflectionCache.getFields(targetClass);
            Field field = cachedFields.get(fieldName);
            if (field == null) {
                return null;
            }
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            Object value = field.get(object);
            field.setAccessible(accessible);
            return value;
        } catch (IllegalAccessException | SecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Retrieve the fields used to build the content.
     * 
     * @return
     */
    private TreeNodeField<String, Set<String>> getFields() {
        
        if(drc.getFields() != null){
            return drc.getFields();
        }
        
        return CrudUtilHelper.extractFieldsFromSearchAnnotation(resourceInfo);

    }

    /**
     * Checks whether the current resource method is annotated with {@link Cacheable}.
     *
     * @return the {@link Cacheable} annotation if present, or {@code null}
     */
    private Cacheable findCacheableAnnotation() {
        if (resourceInfo == null || resourceInfo.getResourceMethod() == null) {
            return null;
        }
        Method method = resourceInfo.getResourceMethod();
        return method.getAnnotation(Cacheable.class);
    }

    /**
     * Builds a cache key for the current request based on the entity class
     * and the full request URI (including query parameters).
     *
     * <p>Key format: {@code entityClassName:requestURI}</p>
     *
     * @return the cache key string
     */
    private String buildFilterCacheKey() {
        String entityClassName = CrudUtilHelper.getTargetClass(resourceInfo.getResourceClass()).getName();
        String requestUri = uriInfo.getRequestUri().toString();
        return entityClassName + ":filter:" + requestUri;
    }

}
