/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.crud;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.demoiselle.jee.core.api.crud.Crud;
import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.rest.annotation.ValidatePayload;

/**
 * TODO JAVADOC
 *
 * @author SERPRO
 *
 * @param <T>
 * @param <I>
 */
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public abstract class AbstractREST<T, I> implements Crud<T, I> {

	@Inject
	protected AbstractBusiness<T, I> bc;

	@Context
	protected UriInfo uriInfo;

	@POST
	@Transactional
	@ValidatePayload
	// @ApiOperation(value = "persist entity")
	public T persist(@Valid T entity) {
		return bc.persist(entity);
	}

	@PUT
	@Transactional
	@ValidatePayload
	// @ApiOperation(value = "full update entity")
	public T merge(@Valid T entity) {
		return bc.merge(entity);
	}

	@DELETE
	@Path("{id}")
	@Transactional
	// @ApiOperation(value = "remove entity")
	public void remove(@PathParam("id") final I id) {
		bc.remove(id);
	}

	@GET
	@Transactional
	// @ApiOperation(value = "list all entities with pagination filter and
	// query")
	public Result find() {
		if (uriInfo.getQueryParameters().isEmpty()) {
			return bc.find();
		} else {
			return bc.find(uriInfo.getQueryParameters());
		}
	}

	@GET
	@Path("{id}")
	@Transactional
	// @ApiOperation(value = "find by ID")
	public T find(@PathParam("id") final I id) {
		return bc.find(id);
	}
}
