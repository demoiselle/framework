package br.gov.frameworkdemoiselle.internal.implementation;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.frameworkdemoiselle.security.NotLoggedInException;

@Provider
public class NotLoggedInExceptionMapper implements ExceptionMapper<NotLoggedInException> {

	@Override
	public Response toResponse(NotLoggedInException exception) {
		// HttpServletRequest request = Beans.getReference(HttpServletRequest.class);
		// String path = request.getRequestURI().substring(request.getContextPath().length());
		//
		// Response response;
		//
		// if (path.indexOf("/api") > -1) {
		// response = Response.status(SC_UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=default").build();
		// } else {
		// response = Response.status(SC_UNAUTHORIZED).build();
		// }
		//
		// return response;

		return Response.status(SC_UNAUTHORIZED).build();
	}
}
