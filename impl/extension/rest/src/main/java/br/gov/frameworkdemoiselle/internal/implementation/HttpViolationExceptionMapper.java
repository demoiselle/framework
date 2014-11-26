package br.gov.frameworkdemoiselle.internal.implementation;

import static java.util.logging.Level.FINE;

import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.frameworkdemoiselle.HttpViolationException;
import br.gov.frameworkdemoiselle.HttpViolationException.Violation;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@Provider
public class HttpViolationExceptionMapper implements ExceptionMapper<HttpViolationException> {

	private transient ResourceBundle bundle;

	private transient Logger logger;

	@Override
	public Response toResponse(HttpViolationException exception) {
		Set<Violation> violations = exception.getViolations();
		int status = exception.getStatusCode();

		if (violations.isEmpty()) {
			violations = null;
		} else {
			getLogger().log(FINE, getBundle().getString("mapping-violations", status), exception);
		}

		return Response.status(status).entity(violations).build();
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
