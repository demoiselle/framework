/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.container.ResourceInfo;

import org.demoiselle.jee.crud.field.TreeNodeField;

/**
 * @author SERPRO
 *
 */
public class CrudUtilHelper {

    public static Class<?> getTargetClass(Class<?> targetClass) {
        if (targetClass.getSuperclass().equals(AbstractREST.class)) {
            Class<?> type = (Class<?>) ((ParameterizedType) targetClass.getGenericSuperclass()).getActualTypeArguments()[0];
            return type;
        }
        return null;
    }

    public static void checkIfExistField(Class<?> targetClass, String field) {
        if (targetClass != null) {
            try {
                targetClass.getDeclaredField(field);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
    
    public static List<String> extractFields(String fields) {
        List<String> results = new LinkedList<>();
        
        int qtyOpenParentheses = 0;
        int qtyCloseParentheses = 0;

        int lastComma = 0;
        int lastPosition = 0;
        String subField = "";

        char fieldsArray[] = fields.toCharArray();

        for(int i = 0 ; i < fieldsArray.length ; i++){

            char letter = fieldsArray[i];
                
            // Find util the next ',' character
            if( letter == ',' ) {
                lastComma = i;
                
                subField = fields.substring(lastPosition, lastComma);
                
                // If the subset doesn't exists open parentheses add the field
                if(!subField.contains("(")){
                    results.add(subField);
                    lastPosition = lastComma + 1;
                }
                else{
                    qtyCloseParentheses = 0;
                    qtyOpenParentheses = 0;
                    Boolean closeAllParentheses = Boolean.FALSE;
                    
                    // Find the close parentheses
                    for(int j = lastPosition ; j < fieldsArray.length ; j++){
                        char subLetter = fieldsArray[j];
                        
                        if(subLetter == '(') {
                            qtyOpenParentheses++;
                        }
                        
                        if(subLetter == ')') {
                            qtyCloseParentheses++;
                        }
                        
                        if(qtyCloseParentheses > 0 && qtyOpenParentheses > 0 
                            && qtyCloseParentheses == qtyOpenParentheses) { 
                                                              
                            subField = fields.substring(lastPosition, ++j);
                            results.add(subField);
                            closeAllParentheses = Boolean.TRUE;
                            
                            lastPosition = j+1;
                            i = j+1;          

                            break;
                        }
                        
                    }
                    
                    if(closeAllParentheses == Boolean.FALSE){
                        throw new IllegalArgumentException();
                    }
                    
                }
            }

        }
        
        if(lastPosition < fieldsArray.length){
            subField = fields.substring(lastPosition, fieldsArray.length);
            results.add(subField);
        }
        
        return results;
    }
    
    public static void fillLeafTreeNodeField(TreeNodeField<String, Set<String>> tnf, String field, Set<String> value) {
        
        String actualField = field;
        
        if(hasSubField(actualField)){
            String masterField = actualField.substring(0, actualField.indexOf("("));
            
            TreeNodeField<String, Set<String>> masterTNF = getTreeNodeField(masterField, tnf);
           
            actualField = actualField.substring(actualField.indexOf("(") + 1, actualField.length() - 1);
            List<String> subFields = extractFields(actualField);
            for(String actualSubField : subFields){
            
                if(!hasSubField(actualSubField)){
                    masterTNF.addChild(actualSubField, value);
                }
                else{
                    fillLeafTreeNodeField(masterTNF, actualSubField, value);
                }
            }
        }
        else{
            tnf.addChild(actualField, value);
        }
        
    }
    
    private static TreeNodeField<String, Set<String>> getTreeNodeField(String masterField, TreeNodeField<String, Set<String>> tnf) {
        TreeNodeField<String, Set<String>> tnfFinded = tnf.getChildren().stream()
                                                                .filter( child -> child.getKey().equalsIgnoreCase(masterField))
                                                                .findAny()
                                                                .orElse(null);
        
        return tnfFinded == null ? tnf.addChild(masterField, null) : tnfFinded;
    }

    public static void validateFields(TreeNodeField<String, Set<String>> tnf, ResourceInfo resourceInfo, CrudMessage crudMessage) {
        
        // Get fields from @Search.fields attribute
        List<String> fieldsFromAnnotation = new ArrayList<>();
        if(resourceInfo.getResourceMethod().isAnnotationPresent(Search.class)){
            String fieldsAnnotation[] = resourceInfo.getResourceMethod().getAnnotation(Search.class).fields();
            fieldsFromAnnotation.addAll(Arrays.asList(fieldsAnnotation));
        }
        
        //Validate fields
        tnf.getChildren().stream().forEach( (leaf) -> {
            
            if(!fieldsFromAnnotation.isEmpty()){
                if(!fieldsFromAnnotation.contains(leaf.getKey())){
                    throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnSearchField(leaf.getKey()));                                                               
                }
            }
            
            Class<?> targetClass = getTargetClass(resourceInfo.getResourceClass());
            
            if(!leaf.getChildren().isEmpty()){
                Field fieldMaster;
                
                try {
                    fieldMaster = targetClass.getDeclaredField(leaf.getKey());
                }
                catch (SecurityException | NoSuchFieldException e) {
                    throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnObject(leaf.getKey(), targetClass.getName()));
                }
                
                Class<?> fieldClazz = fieldMaster.getType();
                                        
                leaf.getChildren().forEach( (subLeaf) -> {
                    try{
                        CrudUtilHelper.checkIfExistField(fieldClazz, subLeaf.getKey());
                    }
                    catch(IllegalArgumentException e){
                        throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnObject(subLeaf.getKey(), fieldClazz.getName()));
                    }
                });
            }
            else{
                try{
                    CrudUtilHelper.checkIfExistField(getTargetClass(resourceInfo.getResourceClass()), leaf.getKey());
                }
                catch(IllegalArgumentException e){
                    throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnObject(leaf.getKey(), getTargetClass(resourceInfo.getResourceClass()).getName()));
                }
            }
        });
        
    }
    
    private static Boolean hasSubField(String field){
        Pattern patternLevels = Pattern.compile("\\([^)]*\\)*");
        Matcher matcher = patternLevels.matcher(field);
        
        return matcher.find();
    }

    public static String getMethodAnnotatedWithID(Class<?> targetClass) {
        return "id";
    }


}
