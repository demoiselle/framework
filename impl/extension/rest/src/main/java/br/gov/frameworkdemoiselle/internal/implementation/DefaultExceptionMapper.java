//package br.gov.frameworkdemoiselle.internal.implementation;
//
//import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
//
//import java.util.ResourceBundle;
//
//import javax.ws.rs.core.Response;
//import javax.ws.rs.ext.ExceptionMapper;
//import javax.ws.rs.ext.Provider;
//
//import org.slf4j.Logger;
//
//import br.gov.frameworkdemoiselle.util.Beans;
//import br.gov.frameworkdemoiselle.util.NamedQualifier;
//
//@Provider
//public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {
//
//	@Override
//	public Response toResponse(Throwable exception) {
//		ResourceBundle bundle = Beans.getReference(ResourceBundle.class, new NamedQualifier("demoiselle-rest-bundle"));
//		Logger logger = Beans.getReference(Logger.class);
//
//		logger.error(exception.getMessage(), exception);
//
//		// throw new DemoiselleException(cause);
//
//		String message = bundle.getString("internal.server.error");
//		return Response.status(INTERNAL_SERVER_ERROR).entity(message).build();
//	}
//}
