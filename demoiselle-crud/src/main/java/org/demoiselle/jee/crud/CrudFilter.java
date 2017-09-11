/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    
    private static final Logger logger = Logger.getLogger(CrudFilter.class.getName());

    public CrudFilter() {}

    public CrudFilter(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleRequestContext drc, PaginationHelper paginationHelper, SortHelper sortHelper, FilterHelper filterHelper, FieldHelper fieldHelper) {
        this.resourceInfo = resourceInfo;
        this.uriInfo = uriInfo;
        this.drc = drc;
        this.paginationHelper = paginationHelper;
        this.sortHelper = sortHelper;
        this.filterHelper = filterHelper;
        this.fieldHelper = fieldHelper;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (isRequestForCrud()) {
            try {
                paginationHelper.execute(resourceInfo, uriInfo);
                sortHelper.execute(resourceInfo, uriInfo);
                filterHelper.execute(resourceInfo, uriInfo);
                fieldHelper.execute(resourceInfo, uriInfo);
            } 
            catch (IllegalArgumentException e) {
                throw new BadRequestException(e.getMessage());
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext response) throws IOException {

        if (response.getEntity() instanceof Result) {

            buildHeaders(response);
            
            response.setEntity(buildContentBody(response));

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
                                        keyValue.put(field.getName(), getValueFromObjectField(targetClass, field, object));
                                    } 
                                    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
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
     * Invoke the field to get the value from the object
     * 
     * @param targetClass Class that represent the object
     * @param field Field that will be invoked
     * @param object The actual object that has the value
     * 
     * @return Value from field 
     * 
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private Object getValueFromObjectField(Class<?> targetClass, Field field, Object object) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
        Object result = null;
        Field actualField = targetClass.getDeclaredField(field.getName());
        boolean acessible = actualField.isAccessible();
        actualField.setAccessible(true);
        result = actualField.get(object);
        actualField.setAccessible(acessible);
        
        return result;
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
        
        if(resourceInfo.getResourceMethod().isAnnotationPresent(Search.class)){
            String fieldsAnnotation[] = resourceInfo.getResourceMethod().getAnnotation(Search.class).fields();
            
            if(fieldsAnnotation != null && !fieldsAnnotation[0].equals("*")){
            
                TreeNodeField<String, Set<String>> tnf = new TreeNodeField<>(CrudUtilHelper.getTargetClass(resourceInfo.getResourceClass()).getName(), ConcurrentHashMap.newKeySet(1));
                for(String field : fieldsAnnotation){
                    tnf.addChild(field, null);
                }
                
                return tnf;
            }
        }
        
        return null;

    }

}
