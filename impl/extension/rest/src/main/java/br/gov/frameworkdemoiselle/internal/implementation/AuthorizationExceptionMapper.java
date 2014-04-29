package br.gov.frameworkdemoiselle.internal.implementation;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.frameworkdemoiselle.security.AuthorizationException;

@Provider
public class AuthorizationExceptionMapper implements ExceptionMapper<AuthorizationException> {

	@Override
	public Response toResponse(AuthorizationException exception) {
		return Response.status(SC_FORBIDDEN).build();
	}
}
