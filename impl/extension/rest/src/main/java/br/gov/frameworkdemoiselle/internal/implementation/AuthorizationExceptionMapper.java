package br.gov.frameworkdemoiselle.internal.implementation;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.security.AuthorizationException;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;

@Provider
public class AuthorizationExceptionMapper implements ExceptionMapper<AuthorizationException> {

	private transient Logger logger;

	@Override
	public Response toResponse(AuthorizationException exception) {
		getLogger().info(exception.getMessage());
		return Response.status(SC_FORBIDDEN).build();
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = Beans.getReference(Logger.class, new NameQualifier(AuthorizationExceptionMapper.class.getName()));
		}

		return logger;
	}
}
