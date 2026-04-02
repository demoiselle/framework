/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configurable stub implementation of {@link Crud} for testing default methods.
 *
 * <p>Allows configuring:
 * <ul>
 *   <li>A map of entities by ID (for {@code find(id)})</li>
 *   <li>A {@link Result} to return from {@code find()} (or null)</li>
 * </ul>
 */
class StubCrud<T> implements Crud<T, String> {

    private final Map<String, T> entities = new HashMap<>();
    private Result<T> findAllResult;

    StubCrud() {
    }

    /** Configure an entity to be returned by {@code find(id)}. */
    void putEntity(String id, T entity) {
        entities.put(id, entity);
    }

    /** Configure the result returned by {@code find()} (no-arg). Set to null to simulate null return. */
    void setFindAllResult(Result<T> result) {
        this.findAllResult = result;
    }

    /** Configure find() to return a Result wrapping the given list. */
    void setFindAllContent(List<T> content) {
        this.findAllResult = new StubResult<>(content);
    }

    @Override
    public T persist(T entity) {
        throw new UnsupportedOperationException("Not needed for default method tests");
    }

    @Override
    public T mergeFull(T entity) {
        throw new UnsupportedOperationException("Not needed for default method tests");
    }

    @Override
    public T mergeHalf(String id, T entity) {
        throw new UnsupportedOperationException("Not needed for default method tests");
    }

    @Override
    public void remove(String id) {
        throw new UnsupportedOperationException("Not needed for default method tests");
    }

    @Override
    public Result<T> find() {
        return findAllResult;
    }

    @Override
    public T find(String id) {
        return entities.get(id);
    }
}
