package br.gov.frameworkdemoiselle.internal.implementation;

import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

import java.util.Iterator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.frameworkdemoiselle.PreconditionFailedException;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

	@Override
	public Response toResponse(ConstraintViolationException exception) {
		PreconditionFailedException failed = new PreconditionFailedException();

		for (Iterator<ConstraintViolation<?>> iter = exception.getConstraintViolations().iterator(); iter.hasNext();) {
			ConstraintViolation<?> violation = iter.next();
			failed.addViolation(violation.getPropertyPath().toString(), violation.getMessage());
		}

		return Response.status(PRECONDITION_FAILED).entity(failed.getViolations()).build();
	}
}
