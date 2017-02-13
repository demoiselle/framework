/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.field;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.CrudMessage;
import org.demoiselle.jee.crud.CrudUtilHelper;
import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.ReservedKeyWords;

/**
 * @author SERPRO
 *
 */
@RequestScoped
public class FieldHelper {

    private UriInfo uriInfo;

    private ResourceInfo resourceInfo;

    @Inject
    private FieldHelperMessage fieldHelperMessage;
    
    @Inject
    private CrudMessage crudMessage;
    
    @Inject
    private DemoiselleRequestContext drc;

    public FieldHelper() {
    }

    public FieldHelper(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleRequestContext drc, FieldHelperMessage fieldHelperMessage, CrudMessage crudMessage) {
        this.uriInfo = uriInfo;
        this.resourceInfo = resourceInfo;
        this.drc = drc;
        this.fieldHelperMessage = fieldHelperMessage;
        this.crudMessage = crudMessage;
    }

    public void execute(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;

        /* 
         * Populate the fields
         * 
         * Ex: 
         *      The request 'url?field1,field2,field3(fieldA,fieldB),field4'
         *      It will be parse to ["field1", "field2", "field3(fieldA,fieldB)", "field4"]
         * 
         */
        
        List<String> queryStringFields = new LinkedList<>();
        uriInfo.getQueryParameters().forEach((key, values) -> {
            if (ReservedKeyWords.DEFAULT_FIELD_KEY.getKey().equalsIgnoreCase(key)) {
                values.forEach(value -> {
                    queryStringFields.addAll(extractFields(value));
                });
            }
        });
        
        TreeNodeField<String, Set<String>> tnf = new TreeNodeField<>(CrudUtilHelper.getTargetClass(this.resourceInfo.getResourceClass()).getName(), ConcurrentHashMap.newKeySet(1));
        
        if(!queryStringFields.isEmpty()) {
            queryStringFields.forEach((field) -> {
                CrudUtilHelper.fillLeafTreeNodeField(tnf, field, null);
            });
            
            CrudUtilHelper.validateFields(tnf, this.resourceInfo, this.crudMessage);
            
            drc.setFields(tnf);
        }
        
    }
    

    private List<String> extractFields(String fields){

        try{
            return CrudUtilHelper.extractFields(fields);
        }
        catch(IllegalArgumentException e){
            throw new IllegalArgumentException(this.fieldHelperMessage.fieldRequestMalFormed(ReservedKeyWords.DEFAULT_FIELD_KEY.getKey(), fields));
        }
    }
    

}
