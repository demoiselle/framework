package org.demoiselle.jee.crud.mocks

import javax.persistence.metamodel.Attribute
import javax.persistence.metamodel.ManagedType
import java.lang.reflect.Field
import java.lang.reflect.Member;

class ClassAttribute implements Attribute {
    Field field;

    public ClassAttribute(Field field) {
        this.field = field;
    }

    @Override
    String getName() {
        return field.getName();
    }

    @Override
    PersistentAttributeType getPersistentAttributeType() {
        return null
    }

    @Override
    ManagedType getDeclaringType() {
        return field.getDeclaringClass()
    }

    @Override
    Class getJavaType() {
        return field.getType()
    }

    @Override
    Member getJavaMember() {
        return null
    }

    @Override
    boolean isAssociation() {
        return false
    }

    @Override
    boolean isCollection() {
        return false
    }
}
