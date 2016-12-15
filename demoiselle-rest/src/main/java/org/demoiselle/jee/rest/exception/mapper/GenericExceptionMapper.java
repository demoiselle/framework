/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.mapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.PreMatching;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;
import org.demoiselle.jee.rest.exception.DemoiselleRestExceptionMessage;
/**
 * 
 * @author SERPRO
 *
 */
@Provider 
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Override
	public Response toResponse(Exception ex) {

		StringWriter errorStackTrace = new StringWriter();
		ex.printStackTrace(new PrintWriter(errorStackTrace));
		HashMap<String, String> entity = new HashMap<>();
	
		//is a validation PAYLOAD REST exception?
		if (ex instanceof DemoiselleRestException || (ex.getCause() != null && ex.getCause() instanceof DemoiselleRestException)) {
			DemoiselleRestException exDemoiselleREST = null;
		
			if (ex instanceof DemoiselleRestException) {
				exDemoiselleREST = (DemoiselleRestException) ex;
			} else {
				exDemoiselleREST = (DemoiselleRestException) ex.getCause();
			}
			
			if (!exDemoiselleREST.getMessages().isEmpty()) {								 									
				return status(exDemoiselleREST.getStatusCode()).entity(exDemoiselleREST.getMessages()).type(APPLICATION_JSON).build();
				
			} else if (exDemoiselleREST.getStatusCode() > 0) {						
				int code = exDemoiselleREST.getStatusCode();										
				String msg = Status.fromStatusCode(code).getReasonPhrase();

				return status(exDemoiselleREST.getStatusCode()).entity(
						new DemoiselleRestExceptionMessage("server_error", msg, null )).type(APPLICATION_JSON).build();
			}

		}

		// show the exception MESSAGE 
		if (ex.getMessage() != null) {					
			// Default SERVER ERROR , but try to find the RESPONSE status if is a WebApplicationException
			// http://docs.oracle.com/javaee/7/api/javax/ws/rs/WebApplicationException.html
			int responseCode = INTERNAL_SERVER_ERROR.getStatusCode();
			if (ex instanceof WebApplicationException) {
				responseCode = ((WebApplicationException) ex).getResponse().getStatus();
			}

			return status(responseCode).entity(new DemoiselleRestExceptionMessage("server_error", ex.getMessage(), null )).type(APPLICATION_JSON).build();
		}
		
		return status(INTERNAL_SERVER_ERROR.getStatusCode()).entity(new DemoiselleRestExceptionMessage("server_error","unknow server error", null )).type(APPLICATION_JSON).build();
	}

}
