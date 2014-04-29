//package br.gov.frameworkdemoiselle.internal.implementation;
//
//import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//
//import java.util.ResourceBundle;
//
//import javax.inject.Inject;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.ext.ExceptionMapper;
//import javax.ws.rs.ext.Provider;
//
//import org.slf4j.Logger;
//
//import br.gov.frameworkdemoiselle.annotation.Name;
//
//@Provider
//public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {
//
//	@Inject
//	@Name("demoiselle-rest-bundle")
//	private ResourceBundle bundle;
//
//	@Inject
//	private Logger logger;
//
//	@Override
//	public Response toResponse(Throwable throwable) {
//		logger.error(throwable.getMessage(), throwable);
//
//		String message = bundle.getString("internal.server.error");
//		return Response.status(SC_INTERNAL_SERVER_ERROR).entity(message).build();
//	}
//}
