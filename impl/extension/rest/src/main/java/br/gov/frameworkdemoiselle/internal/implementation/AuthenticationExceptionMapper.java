package br.gov.frameworkdemoiselle.internal.implementation;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.security.AuthenticationException;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

	private transient Logger logger;

	@Override
	public Response toResponse(AuthenticationException exception) {
		getLogger().info(exception.getMessage());
		return Response.status(SC_UNAUTHORIZED).entity(exception.getMessage()).type("text/plain").build();
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = Beans.getReference(Logger.class, new NameQualifier(AuthenticationExceptionMapper.class.getName()));
		}

		return logger;
	}
}
