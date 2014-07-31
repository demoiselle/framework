package br.gov.frameworkdemoiselle.internal.implementation;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.frameworkdemoiselle.HttpViolationException;

@Provider
public class HttpViolationExceptionMapper implements ExceptionMapper<HttpViolationException> {

	@Override
	public Response toResponse(HttpViolationException exception) {
		return Response.status(exception.getStatusCode()).entity(exception.getViolations()).build();
	}
}
