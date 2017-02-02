/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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
public class FieldHelper {

    private UriInfo uriInfo;

    private ResourceInfo resourceInfo;

    @Inject
    private FieldHelperMessage message;
    
    @Inject
    private DemoiselleRequestContext drc;


    public FieldHelper() {
    }

    public FieldHelper(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleRequestContext drc) {
        this.uriInfo = uriInfo;
        this.resourceInfo = resourceInfo;
        this.drc = drc;
        this.message = message;
    }

    public void execute(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;

        uriInfo.getQueryParameters().forEach((key, values) -> {
            if (ReservedKeyWords.DEFAULT_FIELD_KEY.getKey().equalsIgnoreCase(key)) {
                Set<String> paramValues = new HashSet<>();

                values.stream().forEach(value -> {
                    String[] paramValueSplit = value.split("\\,");
                    paramValues.addAll(Arrays.asList(paramValueSplit));
                });

                drc.getFields().addAll(paramValues);
            }
        });
        
        List<String> fieldsFromAnnotation = new ArrayList<>();
        if(this.resourceInfo.getResourceMethod().isAnnotationPresent(Search.class)){
            String fieldsAnnotation[] = this.resourceInfo.getResourceMethod().getAnnotation(Search.class).fields();
            fieldsFromAnnotation.addAll(Arrays.asList(fieldsAnnotation));
        }
        
        //Validate fields
        drc.getFields().forEach( (field) -> {
            //Check if method is annotated with @Search
            if(!fieldsFromAnnotation.isEmpty()){
                if(!fieldsFromAnnotation.contains(field)){
                    throw new IllegalArgumentException(message.filterFieldRequestNotExistsOnSearchField(field));
                }
            }
            
            CrudUtilHelper.checkIfExistField(CrudUtilHelper.getTargetClass(this.resourceInfo.getResourceClass()), field);
        });
    }

}
