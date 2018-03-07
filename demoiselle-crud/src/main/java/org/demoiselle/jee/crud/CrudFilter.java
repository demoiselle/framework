/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig;
import org.demoiselle.jee.crud.field.FieldHelper;
import org.demoiselle.jee.crud.filter.FilterHelper;
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
    private DemoiselleCrudConfig crudConfig;

    private static final Logger logger = Logger.getLogger(CrudFilter.class.getName());

    public CrudFilter() {}

    public CrudFilter(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleCrudConfig crudConfig, DemoiselleRequestContext drc, PaginationHelper paginationHelper, SortHelper sortHelper, FilterHelper filterHelper, FieldHelper fieldHelper) {
        this.resourceInfo = resourceInfo;
        this.crudConfig = crudConfig;
        this.uriInfo = uriInfo;
        this.drc = drc;
        this.paginationHelper = paginationHelper;
        this.sortHelper = sortHelper;
        this.filterHelper = filterHelper;
        this.fieldHelper = fieldHelper;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        drc.setAbstractRestRequest(isAbstractRestRequest(resourceInfo));
        drc.setDemoiselleCrudAnnotation(CrudUtilHelper.getDemoiselleCrudAnnotation(resourceInfo));
        drc.setEntityClass(CrudUtilHelper.getTargetClass(this.resourceInfo));
        try {
            filterHelper.execute(resourceInfo, uriInfo);
            sortHelper.execute(resourceInfo, uriInfo);
            paginationHelper.execute(resourceInfo, uriInfo);
            fieldHelper.execute(resourceInfo, uriInfo);
        }
        catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private boolean isAbstractRestRequest(ResourceInfo resourceInfo) {
        return CrudUtilHelper.getAbstractRestTargetClass(resourceInfo) != null;
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext response) throws IOException {

        if (response.getEntity() instanceof Result) {
            Result result = (Result)response.getEntity();
            buildHeaders(response, result);

            response.setEntity(buildContentBody(response));


            if (!paginationHelper.isPartialContentResponse(result)) {
                response.setStatus(Status.OK.getStatusCode());
            }
            else {
                response.setStatus(Status.PARTIAL_CONTENT.getStatusCode());
            }
        }
        else {
            Class<?> targetClass = drc.getEntityClass();
            if (Status.BAD_REQUEST.getStatusCode() == response.getStatus() && targetClass != Object.class) {
                paginationHelper.buildAcceptRangeWithResponse(response);
            }
        }

    }

    /**
     * Build all HTTP Headers.
     *
     */
    private void buildHeaders(ContainerResponseContext response, Result result) {
        String exposeHeaders = ReservedHTTPHeaders.HTTP_HEADER_ACCEPT_RANGE.getKey() + ", " + ReservedHTTPHeaders.HTTP_HEADER_CONTENT_RANGE.getKey() + ", " + HttpHeaders.LINK;
        response.getHeaders().putSingle(ReservedHTTPHeaders.HTTP_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS.getKey(), exposeHeaders);
        paginationHelper.buildHeaders(resourceInfo, uriInfo, result).forEach((k, v) -> response.getHeaders().putSingle(k, v));
    }


    /**
     * Build the result used on 'Body' HTTP Response.
     *
     * If the request used the {@link FieldHelper} feature or used the {@link DemoiselleCrud} annotation the
     * result from database will be parsed to a Map to filter theses fields.
     *
     * @param response
     * @return result
     */
    private Object buildContentBody(ContainerResponseContext response) {

        @SuppressWarnings("unchecked")
        Result result = ((Result) response.getEntity());
        List<Object> content = (List<Object>) result.getContent();

        TreeNodeField<String, Set<String>> fields = getFields();
        if(fields != null && fields.getValue() != null && (!fields.getValue().isEmpty() || !fields.getChildren().isEmpty())){
            content = new LinkedList<>();
            Class<?> targetClass = getEntityClassFrom(result);
            Iterator<?> it = ((Result) response.getEntity()).getContent().iterator();

            while(it.hasNext()){
                Object object = it.next();
                Map<String, Object> keyValue = new LinkedHashMap<>();

                fields.getChildren().stream().forEach((leaf) -> {

                    // 1st level
                    if(leaf.getChildren().isEmpty()){
                        Set<String> searchFields = fields.getChildren()
                                .stream()
                                .filter( (child) -> child.getChildren().isEmpty())
                                .map( (child) -> child.getKey())
                                .collect(Collectors.toSet());

                        Arrays.asList(object.getClass().getDeclaredFields())
                                .stream()
                                .filter( (f) -> searchFields.contains(f.getName()))
                                .forEach( (field) -> {
                                    try{
                                        keyValue.put(field.getName(), getValueFromObjectField(targetClass, field.getName(), object));
                                    }
                                    catch (IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchMethodException | InvocationTargetException e) {
                                        logger.log(Level.SEVERE, e.getMessage(), e);
                                    }
                                });

                    }
                    else{
                        //2nd level
                        Map<String, Object> keyValueSecond = new LinkedHashMap<>();

                        leaf.getChildren().stream().forEach( (child) -> {

                            try{
                                Field field = targetClass.getDeclaredField(leaf.getKey());
                                Class<?> fieldClazz = field.getType();


                                Field secondField = fieldClazz.getDeclaredField(child.getKey());

                                boolean acessible = field.isAccessible();
                                boolean acessibleSecond = secondField.isAccessible();

                                field.setAccessible(true);
                                secondField.setAccessible(true);
                                Object secondObject = (Object) field.get(object);
                                if (secondObject != null) {
                                    keyValueSecond.put(secondField.getName(), secondField.get(secondObject));
                                }

                                secondField.setAccessible(acessibleSecond);
                                field.setAccessible(acessible);
                            }
                            catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                                logger.log(Level.SEVERE, e.getMessage(), e);
                            }

                        });

                        keyValue.put(leaf.getKey(), keyValueSecond);
                    }

                });

                content.add(keyValue);
            }

        }
        return content;

    }

    /**
     * Get the Entity Class from the given result, defaulting to the target class parameter of the {@link DemoiselleCrud} if it's present on the REST method.
     *
     * @param result The result object
     * @return The given entity class of the result or the {@link DemoiselleCrud} annotation.
     */
    private Class<?> getEntityClassFrom(Result result) {
        if (result.getEntityClass() != null) {
            return result.getEntityClass();
        } else if (drc.getEntityClass() != null) {
            return drc.getEntityClass();
        }
        return CrudUtilHelper.getTargetClass(resourceInfo);
    }

    /**
     * Invoke the field to get the value from the object
     *
     * @param targetClass Class that represent the object
     * @param fieldName Field name that will be invoked
     * @param object The actual object that has the value
     *
     * @return Value from field 
     *
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private Object getValueFromObjectField(Class<?> targetClass, String fieldName, Object object) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Object result;
        Method fieldGetter = CrudUtilHelper.getPropertyGetter(targetClass, fieldName);
        result = fieldGetter.invoke(object);
        return result;
    }

    /**
     * Retrieve the fields used to build the content.
     *
     * @return The fields from the DemoiselleCrudContext, if any, otherwise the fields from the annotation
     */
    private TreeNodeField<String, Set<String>> getFields() {

        if(drc.getFieldsContext().getFields() != null){
            return drc.getFieldsContext().getFields();
        }

        return CrudUtilHelper.extractSearchFieldsFromAnnotation(resourceInfo);

    }


}
