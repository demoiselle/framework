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
import javax.persistence.Id;

import javax.ws.rs.container.ResourceInfo;

/**
 * Class used to support CRUD feature.
 *
 * @author SERPRO
 */
public class CrudUtilHelper {

    /**
     * Given a Class that extends {@link AbstractREST} this method will return
     * the target Class used on {@literal AbstractREST<TargetClass, I>}
     *
     * @param targetClass Target class
     *
     * @return Class used on {@literal AbstractREST<TargetClass, I>}
     */
    public static Class<?> getTargetClass(Class<?> targetClass) {
        if (AbstractREST.class.isAssignableFrom(targetClass)) {
            Class<?> type = (Class<?>) ((ParameterizedType) targetClass.getGenericSuperclass()).getActualTypeArguments()[0];
            return type;
        }
        return null;
    }

    /**
     * Check if the 'field' parameter exists on 'targetClass'
     *
     * @param targetClass The class
     * @param field Field to checked
     *
     * @throws IllegalArgumentException When the 'field' doesn't exists on
     * 'targetClass'
     */
    public static void checkIfExistField(Class<?> targetClass, String field) {
        if (targetClass != null) {
            do {
                Field[] fields = targetClass.getDeclaredFields();
                for (Field f : fields) {
                    if (f.getName().equalsIgnoreCase(field)) {
                        return;
                    }
                }
                targetClass = targetClass.getSuperclass();
            } while (targetClass != null);
            throw new IllegalArgumentException();
        }
    }

    /**
     * Given a string like 'field1,field2(subField1,subField2)' this method will
     * return a List with [field1, field2(subField1,subField2)] values
     *
     * @param fields String with field to be extracted
     *
     * @return List Parsed values
     */
    public static List<String> extractFields(String fields) {
        List<String> results = new LinkedList<>();

        int qtyOpenParentheses = 0;
        int qtyCloseParentheses = 0;

        int lastComma = 0;
        int lastPosition = 0;
        String subField = "";

        char fieldsArray[] = fields.toCharArray();

        for (int i = 0; i < fieldsArray.length; i++) {

            char letter = fieldsArray[i];

            // Find util the next ',' character
            if (letter == ',') {
                lastComma = i;

                subField = fields.substring(lastPosition, lastComma);

                // If the subset doesn't exists open parentheses add the field
                if (!subField.contains("(")) {
                    results.add(subField);
                    lastPosition = lastComma + 1;
                } else {
                    qtyCloseParentheses = 0;
                    qtyOpenParentheses = 0;
                    Boolean closeAllParentheses = Boolean.FALSE;

                    // Find the close parentheses
                    for (int j = lastPosition; j < fieldsArray.length; j++) {
                        char subLetter = fieldsArray[j];

                        if (subLetter == '(') {
                            qtyOpenParentheses++;
                        }

                        if (subLetter == ')') {
                            qtyCloseParentheses++;
                        }

                        if (qtyCloseParentheses > 0 && qtyOpenParentheses > 0
                                && qtyCloseParentheses == qtyOpenParentheses) {

                            subField = fields.substring(lastPosition, ++j);
                            results.add(subField);
                            closeAllParentheses = Boolean.TRUE;

                            lastPosition = j + 1;
                            i = j + 1;

                            break;
                        }

                    }

                    if (closeAllParentheses == Boolean.FALSE) {
                        throw new IllegalArgumentException();
                    }

                }
            }

        }

        if (lastPosition < fieldsArray.length) {
            subField = fields.substring(lastPosition, fieldsArray.length);
            results.add(subField);
        }

        return results;
    }

    /**
     * Fill the {@link TreeNodeField} object based on parameters.
     *
     * @param tnf TreeNodeField actual node
     * @param field String of field
     * @param value List with values
     */
    public static void fillLeafTreeNodeField(TreeNodeField<String, Set<String>> tnf, String field, Set<String> value) {

        String actualField = field;

        if (hasSubField(actualField)) {
            String masterField = actualField.substring(0, actualField.indexOf("("));

            TreeNodeField<String, Set<String>> masterTNF = getTreeNodeField(masterField, tnf);

            actualField = actualField.substring(actualField.indexOf("(") + 1, actualField.length() - 1);
            List<String> subFields = extractFields(actualField);
            for (String actualSubField : subFields) {

                if (!hasSubField(actualSubField)) {
                    masterTNF.addChild(actualSubField, value);
                } else {
                    fillLeafTreeNodeField(masterTNF, actualSubField, value);
                }
            }
        } else {
            tnf.addChild(actualField, value);
        }

    }

    private static TreeNodeField<String, Set<String>> getTreeNodeField(String masterField, TreeNodeField<String, Set<String>> tnf) {
        TreeNodeField<String, Set<String>> tnfFinded = tnf.getChildren().stream()
                .filter(child -> child.getKey().equalsIgnoreCase(masterField))
                .findAny()
                .orElse(null);

        return tnfFinded == null ? tnf.addChild(masterField, null) : tnfFinded;
    }

    /**
     * Validate the fields based on {@link Search#fields()} or if field exists
     * on targetClass
     *
     * @param tnf TreeNodeField filled
     * @param resourceInfo ResourceInfo
     * @param crudMessage CrudMessage
     */
    public static void validateFields(TreeNodeField<String, Set<String>> tnf, ResourceInfo resourceInfo, CrudMessage crudMessage) {

        // Get fields from @Search.fields attribute
        List<String> fieldsFromAnnotation = new ArrayList<>();
        final TreeNodeField<String, Set<String>> searchFieldsTnf = new TreeNodeField<>(getTargetClass(resourceInfo.getResourceClass()).getName(), null);

        if (resourceInfo.getResourceMethod().isAnnotationPresent(Search.class)) {
            String fieldsAnnotation[] = resourceInfo.getResourceMethod().getAnnotation(Search.class).fields();

            if (fieldsAnnotation != null && !fieldsAnnotation[0].equals("*")) {

                fieldsFromAnnotation.addAll(Arrays.asList(fieldsAnnotation));
                if (!fieldsFromAnnotation.isEmpty()) {
                    // Transform fields from annotation into TreeNodeField                
                    fieldsFromAnnotation.stream().forEach(searchField -> {
                        fillLeafTreeNodeField(searchFieldsTnf, searchField, null);
                    });
                }
            }
        }

        //Validate fields
        tnf.getChildren().stream().forEach(leaf -> {

            if (!searchFieldsTnf.getChildren().isEmpty()) {

                try {
                    // 1st level
                    if (!searchFieldsTnf.containsKey(leaf.getKey())) {
                        throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnSearchField(leaf.getKey()));
                    }

                    if (!leaf.getChildren().isEmpty()) {
                        leaf.getChildren().stream().forEach(leafItem -> {

                            /*
                             * Given a @Search(fields={field1,field2}) and a request like a 'fields=field1,field2(subField1)' 
                             * the request is valid because de @Search.fields specified the root type (field2)
                             *
                             */
                            if (!searchFieldsTnf.getChildByKey(leafItem.getParent().getKey()).getChildren().isEmpty()) {

                                searchFieldsTnf.getChildByKey(leafItem.getParent().getKey()).getChildren().stream().forEach(sfTnf -> {
                                    if (!sfTnf.getParent().containsKey(leafItem.getKey())) {
                                        throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnSearchField(leafItem.getParent().getKey() + "(" + leafItem.getKey() + ")"));
                                    }
                                });

                            }
                        });
                    }
                } catch (IllegalArgumentException e) {
                    throw e;
                }
            }

            Class<?> targetClass = getTargetClass(resourceInfo.getResourceClass());

            if (!leaf.getChildren().isEmpty()) {
                Field fieldMaster;

                try {
                    fieldMaster = targetClass.getDeclaredField(leaf.getKey());
                } catch (SecurityException | NoSuchFieldException e) {
                    throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnObject(leaf.getKey(), targetClass.getName()));
                }

                Class<?> fieldClazz = fieldMaster.getType();

                leaf.getChildren().forEach((subLeaf) -> {
                    try {
                        CrudUtilHelper.checkIfExistField(fieldClazz, subLeaf.getKey());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnObject(subLeaf.getKey(), fieldClazz.getName()));
                    }
                });
            } else {
                try {
                    CrudUtilHelper.checkIfExistField(getTargetClass(resourceInfo.getResourceClass()), leaf.getKey());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnObject(leaf.getKey(), getTargetClass(resourceInfo.getResourceClass()).getName()));
                }
            }
        });

    }

    private static Boolean hasSubField(String field) {
        Pattern patternLevels = Pattern.compile("\\([^)]*\\)*");
        Matcher matcher = patternLevels.matcher(field);

        return matcher.find();
    }

    public static String getMethodAnnotatedWithID(Class<?> targetClass) {    	
    	String name = null;
    	for (Field field :  targetClass.getDeclaredFields() ) {
            if (field.isAnnotationPresent(Id.class)) {
            	name = field.getName();
            	break;
            }
    	}       
        return name;
    }
    
}
