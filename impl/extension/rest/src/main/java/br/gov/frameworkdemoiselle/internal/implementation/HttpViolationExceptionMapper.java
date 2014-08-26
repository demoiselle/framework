package br.gov.frameworkdemoiselle.internal.implementation;

import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

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
			getLogger().debug(getBundle().getString("mapping-violations", status, violations.toString()));
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
			logger = Beans.getReference(Logger.class, new NameQualifier(HttpViolationExceptionMapper.class.getName()));
		}

		return logger;
	}
}
