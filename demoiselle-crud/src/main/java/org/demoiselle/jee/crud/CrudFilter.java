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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.field.FieldHelper;
import org.demoiselle.jee.crud.filter.FilterHelper;
import org.demoiselle.jee.crud.pagination.PaginationHelper;
import org.demoiselle.jee.crud.sort.SortHelper;

/**
 * TODO javadoc
 *
 * @author SERPRO
 *
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
    
    public CrudFilter(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleRequestContext drc, PaginationHelper paginationHelper, SortHelper sortHelper, FilterHelper filterHelper, FieldHelper fieldHelper){
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

	    if(isRequestForCrud()){
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

            paginationHelper.buildHeaders(resourceInfo, uriInfo).forEach((k, v) -> response.getHeaders().putSingle(k, v));

            response.setEntity(buildContent(response));

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
   
    private Boolean isRequestForCrud() {
        if(resourceInfo.getResourceClass().getSuperclass() != null
				&& resourceInfo.getResourceClass().getSuperclass().equals(AbstractREST.class)
                && resourceInfo.getResourceMethod().isAnnotationPresent(GET.class)){
            return Boolean.TRUE;
        }
        
        return Boolean.FALSE;
    }
    
    private Object buildContent(ContainerResponseContext response){
        
        @SuppressWarnings("unchecked")
        List<Object> content = (List<Object>) ((Result) response.getEntity()).getContent();
        
        try{
            
            List<String> searchFields = getFields();
            
            if(searchFields != null){
                content = new LinkedList<>();
                Class<?> targetClass = CrudUtilHelper.getTargetClass(resourceInfo.getResourceClass());
                Iterator<?> it = ((Result) response.getEntity()).getContent().iterator();
                while(it.hasNext()){
                    Object object = it.next();
                    Map<String, Object> keyValue = new LinkedHashMap<>();
                    
                    for(String searchField : searchFields){
                        // Check if searchField has a second level
                        Pattern pattern = Pattern.compile("\\([^)]*\\)");
                        Matcher matcher = pattern.matcher(searchField);
                        
                        if(matcher.find()){
                            String masterField = searchField.replaceAll("\\([^)]*\\)", "");
                            
                            Field field = targetClass.getDeclaredField(masterField); 
                            Class<?> fieldClazz = field.getType();
                                                    
                            Matcher m = Pattern.compile("\\(([^\\)]+)\\)").matcher(searchField);
                            if(m.find()){
                                String secondFields[] = m.group(1).split("\\,");
                                
                                Map<String, Object> keyValueSecond = new LinkedHashMap<>();
                                
                                for(String secondFieldStr : secondFields){
                                    Field secondField = fieldClazz.getDeclaredField(secondFieldStr);
                                    
                                    boolean acessible = field.isAccessible();
                                    boolean acessibleSecond = secondField.isAccessible();
                                    
                                    field.setAccessible(true);  
                                    secondField.setAccessible(true);       
                                    Object secondObject = (Object) field.get(object);                                
                                    keyValueSecond.put(secondField.getName(), secondField.get(secondObject)); 
                                    
                                    secondField.setAccessible(acessibleSecond);
                                    field.setAccessible(acessible);
                                }
                                
                                keyValue.put(masterField, keyValueSecond);
                            }
                            else{
                                if(masterField.contains(field.getName())){
                                    keyValue.put(field.getName(), getValueFromField(targetClass, field, object));
                                }
                            }                            
                        }
                        else{
                            for(Field field : object.getClass().getDeclaredFields()){
                                                                
                                if(searchFields.contains(field.getName())){
                                    keyValue.put(field.getName(), getValueFromField(targetClass, field, object));
                                }
                            
                            }
                        }
                    }
                    
                    content.add(keyValue);
                }
            }
            
        }
        catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e){
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        
        return content;
        
    }
    
    private Object getValueFromField(Class<?> targetClass, Field field, Object object) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
        Object result = null;
        Field actualField = targetClass.getDeclaredField(field.getName());                            
        boolean acessible = actualField.isAccessible();
        actualField.setAccessible(true);                                
        result = actualField.get(object);                                
        actualField.setAccessible(acessible);
        
        return result;
    }

    private List<String> getFields() {
        
        if(!drc.getFields().isEmpty()){
            return drc.getFields();
        }
        
        //Validate if fields exists on fields field from @Search annotation
        if(resourceInfo.getResourceMethod().isAnnotationPresent(Search.class)){
            Search search = resourceInfo.getResourceMethod().getAnnotation(Search.class);
            return Arrays.asList(search.fields());
        }
        
        return null;
    }

    
}
