/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ${package}.service;

import br.gov.frameworkdemoiselle.NotFoundException;
import br.gov.frameworkdemoiselle.security.LoggedIn;
import br.gov.frameworkdemoiselle.security.RequiredRole;
import br.gov.frameworkdemoiselle.transaction.Transactional;
import ${package}.business.BookmarkBC;
import ${package}.entity.Bookmark;
import ${package}.security.Roles;
import br.gov.frameworkdemoiselle.util.ValidatePayload;
import ${package}.util.Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import java.io.Serializable;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.Response;

/**
 *
 * @author 70744416353
 */
@Api(value = "bookmark")
@Path("bookmark")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class BookmarkREST implements Serializable {

    private static final Logger LOG = Logger.getLogger(BookmarkREST.class.getName());

    @Inject
    private BookmarkBC bc;

    /**
     *
     * @param field
     * @param order
     * @param init
     * @param qtde
     * @return
     * @throws NotFoundException
     */
    @GET
    @Path("list/{field}/{order}/{init}/{qtde}")
    @Transactional
    public Response list(@PathParam("field") String field, @PathParam("order") String order, @PathParam("init") int init, @PathParam("qtde") int qtde) throws NotFoundException {
        if ((order.equalsIgnoreCase("asc") || order.equalsIgnoreCase("desc")) && (Util.fieldInClass(field, Bookmark.class))) {
            return Response.ok().entity(bc.list(field, order, init, qtde)).build();
        }
        return Response.ok().entity(null).build();
    }

    @GET
    @Path("{field}/{value}")
    @Transactional
     public Response list(@PathParam("field") final String campo, @PathParam("value") final String valor) {
        if ((Util.fieldInClass(campo, Bookmark.class))) {
            return Response.ok().entity(bc.list(campo, valor)).build();
        }
        return Response.ok().entity(null).build();
    }

    /**
     *
     * @return @throws NotFoundException
     */
    @GET
    @Path("count")
    @Transactional
     public Response count() throws NotFoundException {
        return Response.ok().entity(bc.count()).build();
    }

    /**
     * Removes a instance from delegate.
     *
     * @param id Entity with the given identifier
     */
    @DELETE
    @Path("{id}")
    @Transactional
    public void delete(@PathParam("id") final Long id) {
        bc.delete(id);
    }

    /**
     * Removes a list of instances from delegate.
     *
     * @param ids List of entities identifiers
     */
    @DELETE
    @Path("{ids}")
    @Transactional
    public void delete(@PathParam("ids") final List<Long> ids) {
        ListIterator<Long> iter = ids.listIterator();

        while (iter.hasNext()) {
            this.delete(iter.next());
        }
    }

    /**
     * Gets the results from delegate.
     *
     * @return The list of matched query results.
     */
    @GET
    public Response findAll() {
        return Response.ok().entity(bc.findAll()).build();
    }

    /**
     * Delegates the insert operation of the given instance.
     *
     * @param bean A entity to be inserted by the delegate
     */
    @POST
    @Transactional
    @ValidatePayload
    public Response insert(final Bookmark bean) {
        return Response.ok().entity(bc.insert(bean)).build();
    }

    /**
     * Returns the instance of the given entity with the given identifier
     *
     * @return The instance
     */
    @GET
    @Path("{id}")
    @Transactional
    public Response load(@PathParam("id") final Long id) {
        return Response.ok().entity(bc.load(id)).build();
    }

    /**
     * Delegates the update operation of the given instance.
     *
     * @param bean The instance containing the updated state.
     */
    @PUT
    @Transactional
    @ValidatePayload
    public Response update(final Bookmark bean) {
        return Response.ok().entity(bc.update(bean)).build();
    }

}
