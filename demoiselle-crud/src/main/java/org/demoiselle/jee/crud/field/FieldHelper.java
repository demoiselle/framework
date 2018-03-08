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
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.CrudMessage;
import org.demoiselle.jee.crud.CrudUtilHelper;
import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.ReservedKeyWords;
import org.demoiselle.jee.crud.TreeNodeField;
import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig;

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

    @Inject
    private DemoiselleCrudConfig crudConfig;

    public FieldHelper() {
    }

    public FieldHelper(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleCrudConfig crudConfig, DemoiselleRequestContext drc, FieldHelperMessage fieldHelperMessage, CrudMessage crudMessage) {
        this.uriInfo = uriInfo;
        this.resourceInfo = resourceInfo;
        this.crudConfig = crudConfig;
        this.drc = drc;
        this.fieldHelperMessage = fieldHelperMessage;
        this.crudMessage = crudMessage;
    }

    /**
     * Open the request query string to extract values from 'fields' parameter and 
     * fill the {@link TreeNodeField} object and set the fields object on {@link DemoiselleRequestContext#getFieldsContext()}.
     * 
     * @param resourceInfo ResourceInfo
     * @param uriInfo UriInfo
     */
    public void execute(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;

        drc.getFieldsContext().setFieldsEnabled(isFilterFieldsEnabled());
        if (drc.getFieldsContext().isFieldsEnabled()) {
            /*
             * Populate the fields
             *
             * Ex:
             *      The request 'url?fields=field1,field2,field3(fieldA,fieldB),field4'
             *      It will be parse to ["field1", "field2", "field3(fieldA,fieldB)", "field4"]
             *
             */
            TreeNodeField<String, Set<String>> fields = null;
            if (drc.getEntityClass() != null) {
                TreeNodeField<String, Set<String>> searchFields = CrudUtilHelper.extractSearchFieldsFromAnnotation(this.resourceInfo);
                List<String> queryStringFields = extractQueryStringFieldsFromMap(uriInfo.getQueryParameters());
                Class fieldsClass = drc.getResultClass();
                if (fieldsClass == Object.class || fieldsClass == null) {
                    fieldsClass = drc.getEntityClass();
                }
                fields = extractFieldsFromParameter(fieldsClass, queryStringFields, searchFields);
            }
            drc.getFieldsContext().setFields(fields);
        }

    }

    private boolean isFilterFieldsEnabled() {
        boolean globalFilterFields = crudConfig.isFilterFields();
        boolean abstractRestRequest = drc.isAbstractRestRequest();
        boolean isAnnotationPresent = drc.getDemoiselleResultAnnotation() != null && drc.getDemoiselleResultAnnotation().enableFilterFields();
        return globalFilterFields && (abstractRestRequest || isAnnotationPresent);
    }

    public static List<String> extractQueryStringFieldsFromMap(MultivaluedMap<String, String> parameterMap) {
        List<String> queryStringFields = new LinkedList<>();
        parameterMap.forEach((key, values) -> {
            if (ReservedKeyWords.DEFAULT_FIELD_KEY.getKey().equalsIgnoreCase(key)) {
                values.forEach(value -> {
                    queryStringFields.addAll(extractFields(value));
                });
            }
        });
        return queryStringFields;
    }

    public static TreeNodeField<String, Set<String>> extractFieldsFromParameter(Class<?> entityClass,
                                                                                List<String> queryStringFields,
                                                                                TreeNodeField<String, Set<String>> searchFields) {


        CrudMessage crudMessage = CDI.current().select(CrudMessage.class).get();
        TreeNodeField<String, Set<String>> tnf = new TreeNodeField<>(entityClass.getName(), ConcurrentHashMap.newKeySet(1));

        if(!queryStringFields.isEmpty()) {
            queryStringFields.forEach((field) -> {
                CrudUtilHelper.fillLeafTreeNodeField(tnf, field, null);
            });


            CrudUtilHelper.validateFields(tnf, searchFields, crudMessage, entityClass);

            // Remove fields not declared in @Search.fields property

            if(searchFields != null && !searchFields.getChildren().isEmpty()) {

                searchFields.getChildren().stream()
                        .filter( (it) -> !it.getChildren().isEmpty())
                        .filter( (it) -> tnf.containsKey(it.getKey()))
                        .forEach( (item) -> {
                            if(tnf.getChildByKey(item.getKey()).getChildren() == null
                                    || tnf.getChildByKey(item.getKey()).getChildren().isEmpty()) {
                                item.getChildren().forEach( ch -> {
                                    tnf.getChildByKey(item.getKey()).addChild(ch.getKey(), ch.getValue());
                                });
                            }
                        });
            }
        }
        return tnf;
    }


    private static List<String> extractFields(String fields){

        FieldHelperMessage fieldHelperMessage = CDI.current().select(FieldHelperMessage.class).get();
        try{
            return CrudUtilHelper.extractFields(fields);
        }
        catch(IllegalArgumentException e){
            throw new IllegalArgumentException(fieldHelperMessage.fieldRequestMalFormed(ReservedKeyWords.DEFAULT_FIELD_KEY.getKey(), fields));
        }
    }
    

}
