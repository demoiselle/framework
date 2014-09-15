package br.gov.frameworkdemoiselle.internal.implementation;

import static javax.ws.rs.core.MediaType.TEXT_HTML;

import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import br.gov.frameworkdemoiselle.NotFoundException;
import br.gov.frameworkdemoiselle.util.Metadata;

@Path("metadata")
public class MetadataREST {

	@Inject
	private ResourceBundle bundle;

	@GET
	@Path("demoiselle/version")
	@Produces("text/plain")
	public String getDemoiselleVersion() {
		return Metadata.getVersion();
	}

	@GET
	@Path("version")
	@Produces("text/plain")
	public String getAppVersion() {
		return bundle.getString("application.version");
	}

	@GET
	@Path("message/{key}")
	@Produces(TEXT_HTML)
	public String getMessage(@PathParam("key") String key) throws Exception {
		if (!bundle.containsKey(key)) {
			throw new NotFoundException();
		}

		return bundle.getString(key);
	}
}
