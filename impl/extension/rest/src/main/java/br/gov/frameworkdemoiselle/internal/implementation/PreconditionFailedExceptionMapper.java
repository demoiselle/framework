package br.gov.frameworkdemoiselle.internal.implementation;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.frameworkdemoiselle.PreconditionFailedException;

@Provider
public class PreconditionFailedExceptionMapper implements ExceptionMapper<PreconditionFailedException> {

	@Override
	public Response toResponse(PreconditionFailedException exception) {
		return Response.status(exception.getStatusCode()).entity(exception.getViolations()).build();
	}
}
