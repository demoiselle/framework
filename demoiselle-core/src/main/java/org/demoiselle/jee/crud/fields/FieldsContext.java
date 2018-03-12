package org.demoiselle.jee.crud.fields;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.demoiselle.jee.crud.TreeNodeField;

public class FieldsContext {
    private static final FieldsContext DISABLED_FILTER = new FieldsContext(false, null, null);

    private boolean fieldsEnabled;
    private Set<String> flatFields;
    private TreeNodeField<String, Set<String>> fields;

    public FieldsContext(boolean fieldsEnabled, TreeNodeField<String, Set<String>> fields, Set<String> flatFields) {
        this.fieldsEnabled = fieldsEnabled;
        this.fields = fields;
        this.flatFields = flatFields;
    }

    public boolean isFieldsEnabled() {
        return fieldsEnabled;
    }

    public void setFieldsEnabled(boolean fieldsEnabled) {
        this.fieldsEnabled = fieldsEnabled;
    }

    public TreeNodeField<String, Set<String>> getFields() {
        return fields;
    }

    public void setFields(TreeNodeField<String, Set<String>> fields) {
        this.fields = fields;
    }

    public static final FieldsContext disabledFields() {
        return FieldsContext.DISABLED_FILTER;
    }

    public FieldsContext copy() {
        TreeNodeField<String, Set<String>> filtersCopy = null;
        if(this.fields != null) {
            filtersCopy = new TreeNodeField<>(fields.getKey(), new HashSet<>(fields.getValue()));
        }
        Set<String> flatFieldsCopy = null;
        if (flatFields != null) {
            flatFieldsCopy = new HashSet<>(flatFields);
        }
        return new FieldsContext(fieldsEnabled, filtersCopy, flatFieldsCopy);
    }
}
