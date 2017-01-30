/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.CrudUtilHelper;
import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.ReservedKeyWords;
import org.demoiselle.jee.crud.Search;

/**
 * @author SERPRO
 *
 */
@RequestScoped
public class FilterHelper {
    
    private UriInfo uriInfo;
    
    private ResourceInfo resourceInfo;
    
    @Inject
    private DemoiselleRequestContext drc;
    
    @Inject
    private FilterHelperMessage message;
    
    public FilterHelper(){}
    
    public FilterHelper(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleRequestContext drc, FilterHelperMessage message){
        this.uriInfo = uriInfo;
        this.resourceInfo = resourceInfo;
        this.drc = drc;
        this.message = message;
    }
    
    public void execute(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;
        
        uriInfo.getQueryParameters().forEach((key, values) ->{
            if(!isReservedKey(key)){
                Set<String> paramValues = new HashSet<>();
                
                values.forEach(value -> {
                    String[] paramValueSplit = value.split("\\,");
                    paramValues.addAll(Arrays.asList(paramValueSplit));
                });
                
                drc.getFilters().put(key, paramValues);
            }
        });
        
        //Valida if fields exists on fields field from @Search annotation
        if(resourceInfo.getResourceMethod().isAnnotationPresent(Search.class)){
            Search search = resourceInfo.getResourceMethod().getAnnotation(Search.class);
            List<String> searchFields = Arrays.asList(search.fields());
            for(String field : drc.getFilters().keySet()){
                if(!searchFields.contains(field)){
                    throw new BadRequestException(message.filterFieldRequestNotExistsOnSearchField(field));
                }
            }
        }
        
        // Validade if fields exists on model
        for(String field : drc.getFilters().keySet()){
            CrudUtilHelper.checkIfExistField(CrudUtilHelper.getTargetClass(resourceInfo.getResourceClass()), field);
        }
    }

    private Boolean isReservedKey(String key) {
        return key.equalsIgnoreCase(ReservedKeyWords.DEFAULT_RANGE_KEY.getKey()) 
                || key.equalsIgnoreCase(ReservedKeyWords.DEFAULT_SORT_DESC_KEY.getKey()) 
                || key.equalsIgnoreCase(ReservedKeyWords.DEFAULT_SORT_KEY.getKey())
                || key.equalsIgnoreCase(ReservedKeyWords.DEFAULT_FIELD_KEY.getKey());
    }
}
