package org.demoiselle.jee.crud.cache;

/**
 * CDI event fired when an entity is modified (persisted, merged, or removed).
 * Used by the cache invalidation mechanism to keep query caches consistent.
 *
 * @param entityClass the class of the modified entity
 * @param action      the type of modification performed
 * @param entity      the entity instance or its ID (for REMOVE operations)
 * @param <T>         the entity type
 */
public record EntityModifiedEvent<T>(
    Class<T> entityClass,
    Action action,
    Object entity
) {
    /**
     * The type of entity modification.
     */
    public enum Action { PERSIST, MERGE, REMOVE }
}
