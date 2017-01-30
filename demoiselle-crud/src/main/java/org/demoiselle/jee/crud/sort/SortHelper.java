/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.sort;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.CrudUtilHelper;
import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.ReservedKeyWords;

/**
 * @author SERPRO
 *
 */
@RequestScoped
public class SortHelper {
    
    private ResourceInfo resourceInfo;
    
    private UriInfo uriInfo;
    
    @Inject
    private DemoiselleRequestContext drc;
    
    public SortHelper(){}
    
    public SortHelper(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleRequestContext drc){
        this.resourceInfo = resourceInfo;
        this.uriInfo = uriInfo;
        this.drc = drc;
    }

    public void execute(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;
        
        String url = uriInfo.getRequestUri().toString();
        Pattern pattern = Pattern.compile("[\\?&]([^&=]+)=*([^&=]+)");
        Matcher matcher = pattern.matcher(url);
        
        Set<String> ascList = new LinkedHashSet<>();
        Set<String> descList = new LinkedHashSet<>();
        Boolean descAll = Boolean.FALSE;
        
        while(matcher.find()){
            String group = matcher.group().substring(1);
            String keyValue[] = group.split("=");
            if(keyValue != null && keyValue.length > 0){
                
                if(ReservedKeyWords.DEFAULT_SORT_DESC_KEY.getKey().equalsIgnoreCase(keyValue[0]) 
                        || ReservedKeyWords.DEFAULT_SORT_KEY.getKey().equalsIgnoreCase(keyValue[0])){
                    
                    if(keyValue.length == 2){
                        String[] paramValueSplit = keyValue[1].split("\\,");
                        
                        if(ReservedKeyWords.DEFAULT_SORT_DESC_KEY.getKey().equalsIgnoreCase(keyValue[0])){
                            descList.addAll(Arrays.asList(paramValueSplit));
                        }
                        else{
                            ascList.addAll(Arrays.asList(paramValueSplit));
                        }
                    }
                    else{
                        if(ReservedKeyWords.DEFAULT_SORT_DESC_KEY.getKey().equalsIgnoreCase(keyValue[0])){
                            descAll = Boolean.TRUE;
                        }
                    }
                }
            }
            
        }
        
        // Values that exists on DESC list should removed from ASC list
        descList.forEach( (field) -> {
            ascList.remove(field);
        });
        
        drc.getSorts().clear();
        
        if(!descAll){
            drc.getSorts().put(CrudSort.ASC, ascList);
            drc.getSorts().put(CrudSort.DESC, descList);
        }
        else{
            drc.getSorts().put(CrudSort.DESC, ascList);
        }
        
        // Validate if the fields are valid
        drc.getSorts().forEach( (key, values) -> {
            values.forEach( (field) -> {
                CrudUtilHelper.checkIfExistField(CrudUtilHelper.getTargetClass(resourceInfo.getResourceClass()), field);
            });
        });
        
    }
}
