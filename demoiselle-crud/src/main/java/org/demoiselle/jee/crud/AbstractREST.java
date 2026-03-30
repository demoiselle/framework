/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response.Status;

import org.demoiselle.jee.core.api.crud.Crud;
import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;

/**
 * TODO CLF JAVADOC
 *
 * @author SERPRO
 *
 */
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public abstract class AbstractREST<T, I> implements Crud<T, I> {

    @Inject
    protected AbstractBusiness<T, I> bc;

    @Inject
    private CrudMessage crudMessage;

    @POST
    @Transactional
    @Override
    public T persist(@Valid T entity) {
        return bc.persist(entity);
    }

    @PUT
    @Transactional
    @Override
    public T mergeFull(@Valid T entity) {
        return bc.mergeFull(entity);
    }

    @PATCH
    @Path("{id}")
    @Transactional
    @Override
    public T mergeHalf(@PathParam("id") final I id, T entity) {
	return bc.mergeHalf(id, entity);
    }

    @DELETE
    @Path("{id}")
    @Transactional
    @Override
    public void remove(@PathParam("id") final I id) {
        bc.remove(id);
    }

    @GET
    @Path("{id}")
    @Transactional
    @Override
    public T find(@PathParam("id") final I id) {
        return bc.find(id);
    }

    @GET
    @Transactional
    @Override
    public Result find() {
        /*
	     * For security reasons we opted to throw the exception below so that the developer who is 
	     * extending this class overrides its own find() method using the @Search annotation (...) 
	     * defining the field fields.
	     * 
	     * TODO CLF definir link de documentação
         */
        throw new DemoiselleRestException(crudMessage.methodFindNotImplemented(), Status.NOT_IMPLEMENTED.getStatusCode());
    }
}
