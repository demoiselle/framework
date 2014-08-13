package br.gov.frameworkdemoiselle.internal.implementation;

import java.util.Iterator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.frameworkdemoiselle.UnprocessableEntityException;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

	@Override
	public Response toResponse(ConstraintViolationException exception) {
		UnprocessableEntityException failed = new UnprocessableEntityException();

		for (Iterator<ConstraintViolation<?>> iter = exception.getConstraintViolations().iterator(); iter.hasNext();) {
			ConstraintViolation<?> violation = iter.next();
			failed.addViolation(violation.getPropertyPath().toString(), violation.getMessage());
		}

		int status = new UnprocessableEntityException().getStatusCode();
		return Response.status(status).entity(failed.getViolations()).build();
	}
}
