package org.demoiselle.jee.crud.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.demoiselle.jee.crud.TreeNodeField;

public class FieldsContext {
    private static final FieldsContext DISABLED_FILTER = new FieldsContext(false, null, null);

    private boolean fieldsEnabled;
    private List<String> allowedFields;
    private List<String> flatFields;

    public FieldsContext(boolean fieldsEnabled, List<String> flatFields, List<String> allowedFields) {
        this.fieldsEnabled = fieldsEnabled;
        this.flatFields = flatFields;
        this.allowedFields = allowedFields;
    }

    public boolean isFieldsEnabled() {
        return fieldsEnabled;
    }

    public void setFieldsEnabled(boolean fieldsEnabled) {
        this.fieldsEnabled = fieldsEnabled;
    }

    public static final FieldsContext disabledFields() {
        return FieldsContext.DISABLED_FILTER;
    }

    public List<String> getFlatFields() {
        return flatFields;
    }

    public void setFlatFields(List<String> flatFields) {
        this.flatFields = flatFields;
    }

    public List<String> getAllowedFields() {
        return allowedFields;
    }

    public void setAllowedFields(String... allowedFields) {
        setAllowedFields(Arrays.asList(allowedFields));
    }

    public void setAllowedFields(List<String> allowedFields) {
        this.allowedFields = allowedFields;
    }

    public FieldsContext copy() {
        TreeNodeField<String, Set<String>> filtersCopy = null;
        List<String> flatFieldsCopy = null;
        if (flatFields != null) {
            flatFieldsCopy = new ArrayList<>(flatFields);
        }
        List<String> allowedFieldsCopy = null;
        if(allowedFields != null) {
            allowedFieldsCopy = new ArrayList<>(allowedFields);
        }
        return new FieldsContext(fieldsEnabled, flatFieldsCopy, allowedFieldsCopy);
    }

}
