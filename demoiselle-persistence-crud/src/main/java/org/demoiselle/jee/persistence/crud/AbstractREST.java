/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.crud;

import io.swagger.annotations.ApiOperation;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import org.demoiselle.jee.core.api.persistence.Crud;
import org.demoiselle.jee.core.api.persistence.Result;
import org.demoiselle.jee.core.exception.DemoiselleException;

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public abstract class AbstractREST<T, I> implements Crud<T, I> {

    @Inject
    protected AbstractBusiness<T, I> bc;

    @POST
    @Transactional
    @ApiOperation(value = "Insere entidade no banco")
    public T persist(T entity) {
        return bc.persist(entity);
    }

    @PUT
    @Transactional
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
    @Path("{id}")
    @Transactional
    @ApiOperation(value = "Busca entidade a partir do ID")
    public T find(@PathParam("id") final I id) {
        return bc.find(id);
    }

    @GET
    @Transactional
    @ApiOperation(value = "Lista todos os registros registro")
    public Result find() {
        return bc.find();
    }

    @GET
    @Transactional
    @Path("{field}/{order}/{init}/{qtde}")
    @ApiOperation(value = "Lista com paginação no servidor")
    public Result find(@PathParam("field") String field,
            @PathParam("order") String order,
            @PathParam("init") int init,
            @PathParam("qtde") int qtde) {
        if ((order.equalsIgnoreCase("asc") || order.equalsIgnoreCase("desc"))) {
            return bc.find(field, order, init, qtde);
        }
        throw new DemoiselleException("A ordem deve ser (asc) ou (desc)");
    }

// FindByExample
//    @GET
//    @Path("query")
//    @Transactional
//    @ApiOperation(value = "Lista todos os registros registro")
//    public Result find(@Context UriInfo uriInfo, String content) {
//        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
//        return bc.find();
//    }
}
