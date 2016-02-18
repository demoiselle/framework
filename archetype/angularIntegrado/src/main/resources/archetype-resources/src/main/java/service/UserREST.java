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
import ${package}.business.UserBC;
import ${package}.entity.User;
import ${package}.security.Roles;
import ${package}.util.Util;
import br.gov.frameworkdemoiselle.util.ValidatePayload;
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


@Api(value = "user")
@Path("user")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class UserREST implements Serializable {

    private static final Logger LOG = Logger.getLogger(UserREST.class.getName());

    @Inject
    private UserBC bc;

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
    @LoggedIn
    @RequiredRole({Roles.ADMINISTRADOR})
    @ApiOperation(value = "Lista com paginação no servidor",
                  notes = "Informe o campo/ordem(asc/desc)/posição do primeiro registro/quantidade de registros",
                  response = User.class
    )
    public Response list(@PathParam("field") String field, @PathParam("order") String order, @PathParam("init") int init, @PathParam("qtde") int qtde) throws NotFoundException {
        if ((order.equalsIgnoreCase("asc") || order.equalsIgnoreCase("desc")) && (Util.fieldInClass(field, User.class))) {
            return Response.ok().entity(bc.list(field, order, init, qtde)).build();
        }
        return Response.ok().entity(null).build();
    }

    @GET
    @Path("{field}/{value}")
    @Transactional
    @LoggedIn
    @RequiredRole({Roles.ADMINISTRADOR})
    @ApiOperation(value = "Lista com onde é informado o campo e valor",
                  notes = "Informe o campo/valor do campo",
                  response = User.class
    )
    public Response list(@PathParam("field") final String campo, @PathParam("value") final String valor) {
        if ((Util.fieldInClass(campo, User.class))) {
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
    @LoggedIn
    @RequiredRole({Roles.ADMINISTRADOR})
    @ApiOperation(value = "Quantidade de registro",
                  notes = "Usado para trabalhar as tabelas com paginação no servidor",
                  response = Integer.class
    )
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
    @LoggedIn
    @RequiredRole({Roles.ADMINISTRADOR})
    @ApiOperation(value = "Remove entidade",
                  response = User.class,
                  authorizations = {
                      @Authorization(value = "JWT",
                                     scopes = {
                                         @AuthorizationScope(scope = "read:events", description = "Read your events")
                                     })
                  }
    )
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
    @LoggedIn
    @RequiredRole({Roles.ADMINISTRADOR})
    @ApiOperation(value = "Remove várias entidades a partir de um lista de IDs",
                  response = User.class,
                  authorizations = {
                      @Authorization(value = "JWT",
                                     scopes = {
                                         @AuthorizationScope(scope = "read:events", description = "Read your events")
                                     })
                  }
    )
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
    @ApiOperation(value = "Lista de todos os registros", response = User.class)
    @LoggedIn
    @RequiredRole({Roles.ADMINISTRADOR})
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
    @LoggedIn
    @RequiredRole({Roles.ADMINISTRADOR})
    @ApiOperation(value = "Insere entidade no banco",
                  response = User.class,
                  authorizations = {
                      @Authorization(value = "JWT",
                                     scopes = {
                                         @AuthorizationScope(scope = "read:events", description = "Read your events")
                                     })
                  }
    )
    public Response insert(final User bean) {
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
    @LoggedIn
    @RequiredRole({Roles.ADMINISTRADOR})
    @ApiOperation(value = "Busca entidade a partir do ID",
                  response = User.class,
                  authorizations = {
                      @Authorization(value = "JWT",
                                     scopes = {
                                         @AuthorizationScope(scope = "read:events", description = "Read your events")
                                     })
                  }
    )
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
    @LoggedIn
    @RequiredRole({Roles.ADMINISTRADOR})
    @ApiOperation(value = "Atualiza a entidade",
                  response = User.class,
                  authorizations = {
                      @Authorization(value = "JWT",
                                     scopes = {
                                         @AuthorizationScope(scope = "read:events", description = "Read your events")
                                     })
                  }
    )
    public Response update(final User bean) {
        return Response.ok().entity(bc.update(bean)).build();
    }

}
