/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response.Status;

import org.demoiselle.jee.core.api.crud.Crud;
import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;

import io.swagger.annotations.ApiOperation;
import io.swagger.jaxrs.PATCH;

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
    @ApiOperation(value = "persist entity")
    @Override
    public T persist(@Valid T entity) {
        return bc.persist(entity);
    }

    @PUT
    @Transactional
    @ApiOperation(value = "full update entity")
    @Override
    public T mergeFull(@Valid T entity) {
        return bc.mergeFull(entity);
    }

    @PATCH
    @Path("{id}")
    @Transactional
    @ApiOperation(value = "partial update entity")
    @Override
    public T mergeHalf(@PathParam("id") final I id, T entity) {
	return bc.mergeHalf(id, entity);
    }

    @DELETE
    @Path("{id}")
    @Transactional
    @ApiOperation(value = "remove entity")
    @Override
    public void remove(@PathParam("id") final I id) {
        bc.remove(id);
    }

    @GET
    @Path("{id}")
    @Transactional
    @ApiOperation(value = "find by ID")
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
