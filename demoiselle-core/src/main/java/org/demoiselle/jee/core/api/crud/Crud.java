/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

/**
 *
 * @author SERPRO
 */
public interface Crud<T, I> {

    public T persist(T entity);

    public T merge(T entity);

    public void remove(I id);

    public Result find();

    public T find(I id);

    //TODO nao usar
    public Result find(String field, String order, int init, int qtde);
}
