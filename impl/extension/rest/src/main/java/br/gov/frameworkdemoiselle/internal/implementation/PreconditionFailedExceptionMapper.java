package br.gov.frameworkdemoiselle.internal.implementation;

import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.frameworkdemoiselle.PreconditionFailedException;

@Provider
public class PreconditionFailedExceptionMapper implements ExceptionMapper<PreconditionFailedException> {

	@Override
	public Response toResponse(PreconditionFailedException exception) {
		return Response.status(PRECONDITION_FAILED).entity(exception.getViolations()).build();
	}
}
