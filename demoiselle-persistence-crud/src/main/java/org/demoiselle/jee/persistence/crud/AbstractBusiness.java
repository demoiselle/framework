/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.crud;

import javax.inject.Inject;
import org.demoiselle.jee.core.api.persistence.Crud;
import org.demoiselle.jee.core.api.persistence.Result;

public abstract class AbstractBusiness<T, I> implements Crud<T, I> {

    @Inject
    protected AbstractDAO<T, I> dao;

    public T persist(T entity) {
        return dao.persist(entity);
    }

    public T merge(T entity) {
        return dao.merge(entity);
    }

    public void remove(I id) {
        dao.remove(id);
    }

    public Result find() {
        return dao.find();
    }

    public T find(I id) {
        return dao.find(id);
    }

    public Result find(String field, String order, int init, int qtde) {
        return dao.find(field, order, init, qtde);
    }

}
