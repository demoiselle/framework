package br.gov.frameworkdemoiselle.internal.implementation;

import static java.util.logging.Level.FINE;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.io.EOFException;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@Provider
public class EOFExceptionMapper implements ExceptionMapper<EOFException> {

	private transient ResourceBundle bundle;

	private transient Logger logger;

	@Override
	public Response toResponse(EOFException exception) {
		int status = SC_BAD_REQUEST;
		String message = getBundle().getString("mapping-violations", status);
		getLogger().log(FINE, message, exception);

		return Response.status(status).build();
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-rest-bundle"));
		}

		return bundle;
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = Beans.getReference(Logger.class, new NameQualifier("br.gov.frameworkdemoiselle.exception"));
		}

		return logger;
	}
}
