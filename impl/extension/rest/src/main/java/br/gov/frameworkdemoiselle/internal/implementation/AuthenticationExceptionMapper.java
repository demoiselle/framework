package br.gov.frameworkdemoiselle.internal.implementation;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.frameworkdemoiselle.security.AuthenticationException;
@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

	@Override
	public Response toResponse(AuthenticationException exception) {
		return Response.status(SC_UNAUTHORIZED).entity(exception.getMessage()).build();
	}
}
