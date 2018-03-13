/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.inject.spi.CDI;
import javax.persistence.Id;

import javax.ws.rs.container.ResourceInfo;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

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
     * @param resourceInfo Resource Info of the request
     *
     * @return Class used on {@literal AbstractREST<TargetClass, I>}
     */
    public static Class<?> getEntityClass(ResourceInfo resourceInfo) {
        DemoiselleResult annotation  = getDemoiselleResultAnnotation(resourceInfo);
        if (annotation != null) {
            return annotation.entityClass();
        }
        return getAbstractRestTargetClass(resourceInfo);
    }

    /**
     * Given a Class that extends {@link AbstractREST} this method will return
     * the target Class used on {@literal AbstractREST<TargetClass, I>}
     *
     * @param resourceInfo Resource Info of the request
     *
     * @return Class used on {@literal AbstractREST<TargetClass, I>}
     */
    public static Class<?> getResultClass(ResourceInfo resourceInfo) {
        DemoiselleResult annotation  = getDemoiselleResultAnnotation(resourceInfo);
        if (annotation != null) {
            if (annotation.resultClass() == Object.class) {
                return annotation.entityClass();
            } else {
                return annotation.resultClass();
            }
        }
        return Object.class;
    }

    public static Class<?> getAbstractRestTargetClass(ResourceInfo resourceInfo) {
        Class<?> resourceClass = resourceInfo.getResourceClass();
        if (AbstractREST.class.isAssignableFrom(resourceClass)) {
            Class<?> type = (Class<?>) ((ParameterizedType) resourceClass.getGenericSuperclass()).getActualTypeArguments()[0];
            return type;
        }
        return null;
    }

    public static DemoiselleResult getDemoiselleResultAnnotation(ResourceInfo resourceInfo) {
        if (resourceInfo == null) {
            return null;
        }
        if (resourceInfo.getResourceMethod() != null
                && resourceInfo.getResourceMethod().isAnnotationPresent(DemoiselleResult.class)) {
            return resourceInfo.getResourceMethod().getAnnotation(DemoiselleResult.class);
        }
        if (resourceInfo.getResourceClass() != null
                && resourceInfo.getResourceClass().isAnnotationPresent(DemoiselleResult.class)) {
            return resourceInfo.getResourceClass().getAnnotation(DemoiselleResult.class);
        }
        Class<?> targetClass = resourceInfo.getResourceClass();
        DemoiselleResult annotation = getTargetClassAnnotation(targetClass);
        return annotation;
    }

    /**
     * Check if the 'field' parameter exists on 'targetClass'. First, it checks if a getter for
     * the property exists. Then. it checks if a Field exists.
     *
     *
     * @param targetClass The class
     * @param field Field to checked
     *
     * @throws IllegalArgumentException When the 'field' doesn't exists on
     * 'targetClass'
     */
    public static void checkIfExistField(Class<?> targetClass, String field) {
        try {
            checkIfExistProperty(targetClass, field);
        } catch(IllegalArgumentException e) {
            getInstanceField(targetClass, field);
        }
    }

    public static Class<?> getFieldOrPropertyClass(Class<?> targetClass, String fieldOrProperty) {
        try {
            return getPropertyGetter(targetClass, fieldOrProperty).getReturnType();
        } catch(NoSuchMethodException exc) {
            return getInstanceField(targetClass, fieldOrProperty).getType();
        }
    }

    private static Field getInstanceField(Class<?> targetClass, String fieldName) {
        Field field = FieldUtils.getField(targetClass, fieldName);
        if (field == null) {
            String message = CDI.current().select(CrudMessage.class).get().fieldRequestDoesNotExistsOnObject(fieldName, targetClass.getName());
            throw new IllegalArgumentException(message);
        }
        return field;
    }

    private static void checkIfExistProperty(Class<?> targetClass, String field) {
        try {
            getPropertyGetter(targetClass, field);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Method getPropertyGetter(Class<?> targetClass, String field) throws NoSuchMethodException {
        String getterMethodName = "get"+StringUtils.capitalize(field);
        return targetClass.getMethod(getterMethodName);
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
     *
     * Extract fields from {@link DemoiselleResult} annotation to fill the {@link TreeNodeField} object
     *
     * @param resourceInfo ResourceInfo
     * @return TreeNodeField filled with fields
     */
    public static TreeNodeField<String, Set<String>> extractSearchFieldsFromAnnotation(ResourceInfo resourceInfo) {
        DemoiselleResult annotation = CrudUtilHelper.getDemoiselleResultAnnotation(resourceInfo);
        if (annotation != null) {
            return extractSearchFieldsFromAnnotation(annotation, getEntityClass(resourceInfo));
        }

        return null;
    }

    public static TreeNodeField<String, Set<String>> extractSearchFieldsFromAnnotation(DemoiselleResult annotation, Class<?> entityClass) {
        if (annotation == null) {
            return null;
        }
        String fieldsAnnotation[] = annotation.searchFields();
        return convertFieldsStringToTree(fieldsAnnotation, entityClass);
    }

    private static TreeNodeField<String, Set<String>> convertFieldsStringToTree(String[] fieldsAnnotation, Class<?> clazz) {

        if (fieldsAnnotation != null && fieldsAnnotation.length > 0 && !fieldsAnnotation[0].equals("*")) {
            List<String> fieldsFromAnnotation = new ArrayList<>();
            fieldsFromAnnotation.addAll(Arrays.asList(fieldsAnnotation));
            if (!fieldsFromAnnotation.isEmpty()) {
                final TreeNodeField<String, Set<String>> searchFieldsTnf = new TreeNodeField<>(clazz.getName(), null);
                // Transform fields from annotation into TreeNodeField
                fieldsFromAnnotation.stream().forEach(searchField -> {
                    fillLeafTreeNodeField(searchFieldsTnf, searchField, null);
                });

                return searchFieldsTnf;
            }
        }
        return null;

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
        }
        else {
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

    public static void validateFlatFields(List<String> flatFields, List<String> allowedFields, Class resultClass) {
        CrudMessage crudMessage = CDI.current().select(CrudMessage.class).get();
        for (String fullPath : flatFields) {
            if (!isAllowedField(fullPath, allowedFields)) {
                String message = crudMessage.fieldRequestDoesNotExistsOnDemoiselleResultField(fullPath);
                throw new IllegalArgumentException(message);
            }
            validateFieldPath(fullPath, resultClass);
        }
    }

    private static boolean isAllowedField(String fullPath, List<String> allowedGlobs) {
        return allowedGlobs.stream().anyMatch(glob -> FilenameUtils.wildcardMatch(fullPath, glob));
    }


    public static void main(String[] args) {

    }

    private static void validateFieldPath(String fullPath, Class resultClass) {
        Class currClass = resultClass;
        String pathPart = fullPath;
        while (pathPart.contains(".")) {
            String currField = pathPart.substring(0, pathPart.indexOf("."));
            currClass = getFieldOrPropertyClass(currClass, currField);
            pathPart = pathPart.substring(pathPart.indexOf(".")+1);
        }
        checkIfExistField(currClass, pathPart);
    }

    public static void validateFields(TreeNodeField<String, Set<String>> requestedFieldsTnf,
                                      TreeNodeField<String, Set<String>> defaultFieldsTnf,
                                      CrudMessage crudMessage,
                                      Class<?> targetClass) {
        // Get fields from @Search.fields attribute
        //Validate fields
        requestedFieldsTnf.getChildren().stream().forEach(leaf -> {

            if (defaultFieldsTnf != null && !defaultFieldsTnf.getChildren().isEmpty()) {

                try {
                    // 1st level
                    if (!defaultFieldsTnf.containsKey(leaf.getKey())) {
                        throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnDemoiselleResultField(leaf.getKey()));
                    }

                    if (!leaf.getChildren().isEmpty()) {
                        leaf.getChildren().stream().forEach(leafItem -> {

                            /*
                             * Given a @Search(fields={field1,field2}) and a request like a 'fields=field1,field2(subField1)'
                             * the request is valid because the @Search.fields specified the root type (field2)
                             *
                             */
                            if (!defaultFieldsTnf.getChildByKey(leafItem.getParent().getKey()).getChildren().isEmpty()) {

                                defaultFieldsTnf.getChildByKey(leafItem.getParent().getKey()).getChildren().stream().forEach(sfTnf -> {
                                    if (!sfTnf.getParent().containsKey(leafItem.getKey())) {
                                        throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnDemoiselleResultField(leafItem.getParent().getKey() + "(" + leafItem.getKey() + ")"));
                                    }
                                });

                            }
                        });
                    }

                }
                catch (IllegalArgumentException e) {
                    throw e;
                }
            }


            if (!leaf.getChildren().isEmpty()) {

                Class<?> fieldClazz;
                try {
                    fieldClazz = PropertyUtils.getPropertyType(targetClass, leaf.getKey());
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnObject(leaf.getKey(), targetClass.getName()));
                }

                leaf.getChildren().forEach((subLeaf) -> {
                    try {
                        CrudUtilHelper.checkIfExistField(fieldClazz, subLeaf.getKey());
                    }
                    catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnObject(subLeaf.getKey(), fieldClazz.getName()));
                    }
                });
            }
            else {
                try {
                    CrudUtilHelper.checkIfExistField(targetClass, leaf.getKey());
                }
                catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(crudMessage.fieldRequestDoesNotExistsOnObject(leaf.getKey(), targetClass.getName()));
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

    /**
     * Get the {@link DemoiselleResult} annotation present on the given resourceClass or any parent class of its hierarchy.
     *
     * @param resourceClass The resource class that may be annotated.
     * @return The annotation present on the class or its hierarchy, if any.
     */
    public static DemoiselleResult getTargetClassAnnotation(Class<?> resourceClass) {
        DemoiselleResult annotation;
        if(resourceClass == null) {
            return null;
        }
        annotation = resourceClass.getAnnotation(DemoiselleResult.class);
        Class<?> currClass = resourceClass;
        while (annotation != null && currClass != null && !currClass.equals(Object.class)) {
            annotation = resourceClass.getAnnotation(DemoiselleResult.class);
            currClass = currClass.getSuperclass();
        }
        return annotation;
    }



}
