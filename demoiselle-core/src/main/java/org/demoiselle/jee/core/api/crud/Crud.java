/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

import javax.validation.Valid;

/**
 *
 * @author SERPRO
 */
public interface Crud<T, I> {

    public T persist(@Valid T entity);

    public T mergeFull(@Valid T entity);

    public T mergeHalf(I id, T entity);

    public void remove(I id);

    public Result find();

    public T find(I id);

}
