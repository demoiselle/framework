/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

import java.util.List;

import jakarta.validation.Valid;

/**
 *
 * @author SERPRO
 */
public interface Crud<T, I> {

    public T persist(@Valid T entity);

    public T mergeFull(@Valid T entity);

    public T mergeHalf(I id, T entity);

    public void remove(I id);

    public Result<T> find();

    public T find(I id);

    default boolean exists(I id) {
        return find(id) != null;
    }

    default long count() {
        Result<T> result = find();
        if (result == null) {
            return 0L;
        }
        List<T> content = result.getContent();
        return content == null ? 0L : content.size();
    }

    default List<T> findAll() {
        Result<T> result = find();
        if (result == null) {
            return List.of();
        }
        List<T> content = result.getContent();
        return content == null ? List.of() : content;
    }

}
