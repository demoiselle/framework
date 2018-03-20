package org.demoiselle.jee.crud.mocks

import sun.reflect.generics.reflectiveObjects.NotImplementedException

import javax.persistence.metamodel.EmbeddableType
import javax.persistence.metamodel.EntityType
import javax.persistence.metamodel.ManagedType
import javax.persistence.metamodel.Metamodel

class MetamodelMock implements Metamodel{
    @Override
    def <X> EntityType<X> entity(Class<X> cls) {
        return new ClassEntityType<X>(cls)
    }

    @Override
    def <X> ManagedType<X> managedType(Class<X> cls) {
        throw new NotImplementedException()
    }

    @Override
    def <X> EmbeddableType<X> embeddable(Class<X> cls) {
        throw new NotImplementedException()
    }

    @Override
    Set<ManagedType<?>> getManagedTypes() {
        throw new NotImplementedException()
    }

    @Override
    Set<EntityType<?>> getEntities() {
        throw new NotImplementedException()
    }

    @Override
    Set<EmbeddableType<?>> getEmbeddables() {
        throw new NotImplementedException()
    }
}
