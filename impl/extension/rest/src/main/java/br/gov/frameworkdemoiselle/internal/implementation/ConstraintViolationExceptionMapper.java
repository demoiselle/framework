package br.gov.frameworkdemoiselle.internal.implementation;

import java.util.Iterator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.UnprocessableEntityException;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

	private transient ResourceBundle bundle;

	private transient Logger logger;

	@Override
	public Response toResponse(ConstraintViolationException exception) {
		UnprocessableEntityException failed = new UnprocessableEntityException();
		int status = new UnprocessableEntityException().getStatusCode();

		for (Iterator<ConstraintViolation<?>> iter = exception.getConstraintViolations().iterator(); iter.hasNext();) {
			ConstraintViolation<?> violation = iter.next();
			failed.addViolation(violation.getPropertyPath().toString(), violation.getMessage());
		}

		getLogger().debug(getBundle().getString("mapping-violations", status, failed.getViolations().toString()));
		return Response.status(status).entity(failed.getViolations()).build();
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
