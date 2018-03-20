package org.demoiselle.jee.crud.mocks

import sun.reflect.generics.reflectiveObjects.NotImplementedException

import javax.persistence.metamodel.Attribute
import javax.persistence.metamodel.CollectionAttribute
import javax.persistence.metamodel.EntityType
import javax.persistence.metamodel.IdentifiableType
import javax.persistence.metamodel.ListAttribute
import javax.persistence.metamodel.MapAttribute
import javax.persistence.metamodel.PluralAttribute
import javax.persistence.metamodel.SetAttribute
import javax.persistence.metamodel.SingularAttribute
import javax.persistence.metamodel.Type
import java.lang.reflect.Field
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

class ClassEntityType<T> implements EntityType<T> {

    Class<T> entityClass;

    ClassEntityType(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    String getName() {
        throw new NotImplementedException()
    }

    @Override
    BindableType getBindableType() {
        throw new NotImplementedException()
    }

    @Override
    Class<T> getBindableJavaType() {
        throw new NotImplementedException()
    }

    @Override
    def <Y> SingularAttribute<? super T, Y> getId(Class<Y> type) {
        throw new NotImplementedException()
    }

    @Override
    def <Y> SingularAttribute<T, Y> getDeclaredId(Class<Y> type) {
        throw new NotImplementedException()
    }

    @Override
    def <Y> SingularAttribute<? super T, Y> getVersion(Class<Y> type) {
        throw new NotImplementedException()
    }

    @Override
    def <Y> SingularAttribute<T, Y> getDeclaredVersion(Class<Y> type) {
        throw new NotImplementedException()
    }

    @Override
    IdentifiableType<? super T> getSupertype() {
        throw new NotImplementedException()
    }

    @Override
    boolean hasSingleIdAttribute() {
        return false
    }

    @Override
    boolean hasVersionAttribute() {
        return false
    }

    @Override
    Set<SingularAttribute<? super T, ?>> getIdClassAttributes() {
        throw new NotImplementedException()
    }

    @Override
    Type<?> getIdType() {
        throw new NotImplementedException()
    }

    @Override
    Set<Attribute<? super T, ?>> getAttributes() {
        return Arrays.asList(entityClass.getDeclaredFields())
                    .stream()
                    .map(new Function<Field, Object>() {
                        @Override
                        Object apply(Field field) {
                            return new ClassAttribute(field)
                        }
                    })
                    .collect(Collectors.toSet())
    }

    @Override
    Set<Attribute<T, ?>> getDeclaredAttributes() {
        throw new NotImplementedException()
    }

    @Override
    def <Y> SingularAttribute<? super T, Y> getSingularAttribute(String name, Class<Y> type) {
        throw new NotImplementedException()
    }

    @Override
    def <Y> SingularAttribute<T, Y> getDeclaredSingularAttribute(String name, Class<Y> type) {
        throw new NotImplementedException()
    }

    @Override
    Set<SingularAttribute<? super T, ?>> getSingularAttributes() {
        throw new NotImplementedException()
    }

    @Override
    Set<SingularAttribute<T, ?>> getDeclaredSingularAttributes() {
        throw new NotImplementedException()
    }

    @Override
    def <E> CollectionAttribute<? super T, E> getCollection(String name, Class<E> elementType) {
        throw new NotImplementedException()
    }

    @Override
    def <E> CollectionAttribute<T, E> getDeclaredCollection(String name, Class<E> elementType) {
        throw new NotImplementedException()
    }

    @Override
    def <E> SetAttribute<? super T, E> getSet(String name, Class<E> elementType) {
        throw new NotImplementedException()
    }

    @Override
    def <E> SetAttribute<T, E> getDeclaredSet(String name, Class<E> elementType) {
        throw new NotImplementedException()
    }

    @Override
    def <E> ListAttribute<? super T, E> getList(String name, Class<E> elementType) {
        throw new NotImplementedException()
    }

    @Override
    def <E> ListAttribute<T, E> getDeclaredList(String name, Class<E> elementType) {
        throw new NotImplementedException()
    }

    @Override
    def <K, V> MapAttribute<? super T, K, V> getMap(String name, Class<K> keyType, Class<V> valueType) {
        throw new NotImplementedException()
    }

    @Override
    def <K, V> MapAttribute<T, K, V> getDeclaredMap(String name, Class<K> keyType, Class<V> valueType) {
        throw new NotImplementedException()
    }

    @Override
    Set<PluralAttribute<? super T, ?, ?>> getPluralAttributes() {
        throw new NotImplementedException()
    }

    @Override
    Set<PluralAttribute<T, ?, ?>> getDeclaredPluralAttributes() {
        throw new NotImplementedException()
    }

    @Override
    Attribute<? super T, ?> getAttribute(String name) {
        return new ClassAttribute(entityClass.getDeclaredField(name));
    }

    @Override
    Attribute<T, ?> getDeclaredAttribute(String name) {
        throw new NotImplementedException()
    }

    @Override
    SingularAttribute<? super T, ?> getSingularAttribute(String name) {
        throw new NotImplementedException()
    }

    @Override
    SingularAttribute<T, ?> getDeclaredSingularAttribute(String name) {
        throw new NotImplementedException()
    }

    @Override
    CollectionAttribute<? super T, ?> getCollection(String name) {
        throw new NotImplementedException()
    }

    @Override
    CollectionAttribute<T, ?> getDeclaredCollection(String name) {
        throw new NotImplementedException()
    }

    @Override
    SetAttribute<? super T, ?> getSet(String name) {
        throw new NotImplementedException()
    }

    @Override
    SetAttribute<T, ?> getDeclaredSet(String name) {
        throw new NotImplementedException()
    }

    @Override
    ListAttribute<? super T, ?> getList(String name) {
        throw new NotImplementedException()
    }

    @Override
    ListAttribute<T, ?> getDeclaredList(String name) {
        throw new NotImplementedException()
    }

    @Override
    MapAttribute<? super T, ?, ?> getMap(String name) {
        throw new NotImplementedException()
    }

    @Override
    MapAttribute<T, ?, ?> getDeclaredMap(String name) {
        throw new NotImplementedException()
    }

    @Override
    PersistenceType getPersistenceType() {
        throw new NotImplementedException()
    }

    @Override
    Class<T> getJavaType() {
        return entityClass;
    }
}
