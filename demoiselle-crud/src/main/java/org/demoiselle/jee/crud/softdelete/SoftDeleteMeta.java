package org.demoiselle.jee.crud.softdelete;

public record SoftDeleteMeta(String fieldName, Class<?> fieldType) {
    public boolean isBoolean() {
        return fieldType == Boolean.class || fieldType == boolean.class;
    }
}
