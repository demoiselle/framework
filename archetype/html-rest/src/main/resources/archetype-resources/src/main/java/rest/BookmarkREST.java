package ${package}.rest;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ${package}.business.BookmarkBC;
import ${package}.domain.Bookmark;
import br.gov.frameworkdemoiselle.BadRequestException;
import br.gov.frameworkdemoiselle.NotFoundException;
import br.gov.frameworkdemoiselle.transaction.Transactional;

@Path("bookmark")
public class BookmarkREST {

	@Inject
	private BookmarkBC bc;

	@GET
	@Produces("application/json")
	public List<Bookmark> find() throws Exception {
		return bc.findAll();
	}

	@GET
	@Path("{id}")
	@Produces("application/json")
	public Bookmark load(@PathParam("id") Long id) throws Exception {
		Bookmark result = bc.load(id);

		if (result == null) {
			throw new NotFoundException();
		}

		return result;
	}

	@POST
	@Transactional
	@Produces("text/plain")
	@Consumes("application/json")
	public Response insert(Bookmark entity, @Context UriInfo uriInfo) {
		if (entity.getId() != null) {
			throw new BadRequestException();
		}

		String id = bc.insert(entity).getId().toString();
		URI location = uriInfo.getRequestUriBuilder().path(id).build();

		return Response.created(location).entity(id).build();
	}

	@PUT
	@Path("{id}")
	@Transactional
	@Consumes("application/json")
	public void update(@PathParam("id") Long id, Bookmark entity) {
		if (entity.getId() != null) {
			throw new BadRequestException();
		}

		if (bc.load(id) == null) {
			throw new NotFoundException();
		}

		entity.setId(id);
		bc.update(entity);
	}
	
	@DELETE
	@Transactional
	@Consumes("application/json")
	public void delete(List<Long> ids) {
		bc.delete(ids);
	}

	@DELETE
	@Path("{id}")
	@Transactional
	public void delete(@PathParam("id") Long id) {
		bc.delete(id);
	}
}
