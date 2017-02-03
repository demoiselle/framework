/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.field;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

    public FieldHelper(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleRequestContext drc, FieldHelperMessage message) {
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
                
                values.forEach(value -> {
                    String[] paramValueSplit = value.split(",(?![^(]*\\))");
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
            String fieldStr = field.replaceAll("\\([^)]*\\)", "");
            if(!fieldsFromAnnotation.isEmpty()){
                if(!fieldsFromAnnotation.contains(fieldStr)){
                    throw new IllegalArgumentException(message.fieldRequestDoesNotExistsOnSearchField(fieldStr));
                }
            }
            
            Class<?> targetClass = CrudUtilHelper.getTargetClass(this.resourceInfo.getResourceClass());
                
            // Check if searchField has a second level
            Pattern pattern = Pattern.compile("\\([^)]*\\)");
            Matcher matcher = pattern.matcher(field);            
            
            if(matcher.find()){
                String masterField = field.replaceAll("\\([^)]*\\)", "");
                
                Field fieldMaster;
                
                try {
                    fieldMaster = targetClass.getDeclaredField(masterField);
                }
                catch (SecurityException | NoSuchFieldException e) {
                    throw new IllegalArgumentException(this.message.fieldRequestDoesNotExistsOnObject(field, targetClass.getName()));
                }
                
                Class<?> fieldClazz = fieldMaster.getType();
                                        
                Matcher m = Pattern.compile("\\(([^\\)]+)\\)").matcher(field);
                if(m.find()){
                    String secondFields[] = m.group(1).split("\\,");
                    for(String secondFieldStr : secondFields){
                        try{
                            CrudUtilHelper.checkIfExistField(fieldClazz, secondFieldStr);
                        }
                        catch(IllegalArgumentException e){
                            throw new IllegalArgumentException(this.message.fieldRequestDoesNotExistsOnObject(secondFieldStr, fieldClazz.getName()));
                        }
                    }
                }
                else{
                    try{
                        CrudUtilHelper.checkIfExistField(CrudUtilHelper.getTargetClass(this.resourceInfo.getResourceClass()), masterField);
                    }
                    catch(IllegalArgumentException e){
                        throw new IllegalArgumentException(this.message.fieldRequestDoesNotExistsOnObject(masterField, CrudUtilHelper.getTargetClass(this.resourceInfo.getResourceClass()).getName()));
                    }
                }
                
            }
            else{
                try{
                    CrudUtilHelper.checkIfExistField(CrudUtilHelper.getTargetClass(this.resourceInfo.getResourceClass()), field);
                }
                catch(IllegalArgumentException e){
                    throw new IllegalArgumentException(this.message.fieldRequestDoesNotExistsOnObject(field, CrudUtilHelper.getTargetClass(this.resourceInfo.getResourceClass()).getName()));
                }
            }
        });
    }

}
