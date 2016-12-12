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

import org.demoiselle.jee.core.api.persistence.Crud;
import org.demoiselle.jee.core.api.persistence.Result;
import org.demoiselle.jee.core.exception.DemoiselleException;
import org.demoiselle.jee.rest.annotation.ValidatePayload;

import io.swagger.annotations.ApiOperation;

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
    @ApiOperation(value = "Insere entidade no banco")
    public T persist(T entity) {
        return bc.persist(entity);
    }

    @PUT
    @Transactional
    @ValidatePayload
    @ApiOperation(value = "Atualiza a entidade", notes = "Atualiza")
    public T merge(T entity) {
        return bc.merge(entity);
    }

    @DELETE
    @Path("{id}")
    @Transactional
    @ApiOperation(value = "Remove entidade")
    public void remove(@PathParam("id") final I id) {	
        bc.remove(id);
    }

    @GET
    @Transactional
    @ApiOperation(value = "Lista todos os registros registro e filtra com QueryString")
    public Result find() {
//        if (uriInfo.getQueryParameters().isEmpty()) {
//            return bc.find();
//        } else {
//            return bc.find(uriInfo.getQueryParameters());
//        }
    	
    	return bc.find();
    }

    @GET
    @Path("{id}")
    @Transactional
    @ApiOperation(value = "Busca por ID")
    public T find(@PathParam("id") final I id) {
        return bc.find(id);
    }

    @GET
    @Transactional
    @Path("{field}/{order}/{init}/{qtde}")
    @ApiOperation(value = "Lista com paginação no servidor e filtra com queryString")
    public Result find(
            @PathParam("field") String field,
            @PathParam("order") String order,
            @PathParam("init") int init,
            @PathParam("qtde") int qtde) {
        if ((order.equalsIgnoreCase("asc") || order.equalsIgnoreCase("desc"))) {
            if (uriInfo.getQueryParameters().isEmpty()) {
                return bc.find(field, order, init, qtde);
            } else {
                return bc.find(uriInfo.getQueryParameters(), field, order, init, qtde);
            }
        }
        throw new DemoiselleException("A ordem deve ser (asc) ou (desc)");
    }
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////////
    
   /* @GET
    public List<?> findAll(){
    	bc.find()
    }*/
    

}
