/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import javax.inject.Inject;

import org.demoiselle.jee.core.api.crud.Crud;
import org.demoiselle.jee.core.api.crud.Result;

public abstract class AbstractBusiness<T, I> implements Crud<T, I> {

    @Inject
    protected AbstractDAO<T, I> dao;

    @Override
    public T persist(T entity) {
        return dao.persist(entity);
    }

    @Override
    public T mergeFull(T entity) {
        return dao.mergeFull(entity);
    }

    @Override
    public T mergeHalf(I id, T entity) {
        return dao.mergeHalf(id, entity);
    }

    @Override
    public void remove(I id) {
        dao.remove(id);
    }

    @Override
    public Result find() {
        return dao.find();
    }

    @Override
    public T find(I id) {
        return dao.find(id);
    }

    public void cancelar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
