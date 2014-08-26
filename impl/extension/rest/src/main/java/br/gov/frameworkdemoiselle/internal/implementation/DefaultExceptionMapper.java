package br.gov.frameworkdemoiselle.internal.implementation;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.util.ResourceBundle;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

	private transient ResourceBundle bundle;

	private transient Logger logger;

	@Override
	public Response toResponse(Throwable exception) {
		String message = getBundle().getString("internal.server.error");
		getLogger().error(message, exception);

		return Response.status(INTERNAL_SERVER_ERROR).entity(message).build();
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-rest-bundle"));
		}

		return bundle;
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = Beans.getReference(Logger.class, new NameQualifier(DefaultExceptionMapper.class.getName()));
		}

		return logger;
	}
}
