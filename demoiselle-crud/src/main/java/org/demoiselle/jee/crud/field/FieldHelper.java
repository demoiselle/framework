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
import org.demoiselle.jee.crud.TreeNodeField;

/**
 * Class responsible for managing the 'fields' parameter comes from Url Query String.
 * 
 * Ex:
 * 
 * Given a request
 * <pre>
 * GET {@literal http://localhost:8080/api/users?fields=field1,field2,field3(subField1,subField2)}
 * </pre>
 * 
 * This class will processing the request above and parse the 'fields=...' parameter to 
 * a {@link org.demoiselle.jee.crud.TreeNodeField} object.
 * 
 * @author SERPRO
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

    /**
     * Open the request query string to extract values from 'fields' parameter and 
     * fill the {@link TreeNodeField} object and set the result object on {@link DemoiselleRequestContext#setFields(TreeNodeField)} object.
     * 
     * @param resourceInfo ResourceInfo
     * @param uriInfo UriInfo
     */
    public void execute(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;

        /* 
         * Populate the fields
         * 
         * Ex: 
         *      The request 'url?fields=field1,field2,field3(fieldA,fieldB),field4'
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
