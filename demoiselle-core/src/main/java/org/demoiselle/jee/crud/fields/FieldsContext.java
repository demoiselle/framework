package org.demoiselle.jee.crud.fields;

import java.util.HashSet;
import java.util.Set;

import org.demoiselle.jee.crud.TreeNodeField;

public class FieldsContext {
    private static final FieldsContext DISABLED_FILTER = new FieldsContext(false, null);

    private boolean fieldsEnabled;
    private TreeNodeField<String, Set<String>> fields;

    public FieldsContext(boolean fieldsEnabled, TreeNodeField<String, Set<String>> fields) {
        this.fieldsEnabled = fieldsEnabled;
        this.fields = fields;
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
        return new FieldsContext(fieldsEnabled, filtersCopy);
    }
}
