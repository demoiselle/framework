package br.gov.frameworkdemoiselle.internal.implementation;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.ws.http.HTTPException;

@Provider
public class HTTPExceptionMapper implements ExceptionMapper<HTTPException> {

	@Override
	public Response toResponse(HTTPException exception) {
		return Response.status(exception.getStatusCode()).build();
	}
}
