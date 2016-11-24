/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.core.api.persistence;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author 70744416353
 */
public interface Crud<T, I> {

    public T persist(T entity);

    public T merge(T entity);

    public void remove(I id);

    public Result find();

    public T find(I id);

    public Result find(String field, String order, int init, int qtde);
}
