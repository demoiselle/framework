package br.gov.frameworkdemoiselle.internal.implementation;

import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.frameworkdemoiselle.HttpViolationException;
import br.gov.frameworkdemoiselle.HttpViolationException.Violation;

@Provider
public class HttpViolationExceptionMapper implements ExceptionMapper<HttpViolationException> {

	@Override
	public Response toResponse(HttpViolationException exception) {
		Set<Violation> violations = exception.getViolations();
		violations = violations.isEmpty() ? null : violations;

		return Response.status(exception.getStatusCode()).entity(violations).build();
	}
}
