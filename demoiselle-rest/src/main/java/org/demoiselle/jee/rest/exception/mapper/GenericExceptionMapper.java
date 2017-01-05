/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.mapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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

	private static final Logger logger = Logger.getLogger(GenericExceptionMapper.class.getName());

	private void logAllStackTrace(Exception ex) {
		// Get all messages from stacktrace
		int level = 1;
		while (ex.getCause() != null) {
			ex = (Exception) ex.getCause();
			if (ex != null && ex.getMessage() != null && !ex.getMessage().isEmpty()) {
				logger.severe("inner_cause_" + level + ":" + ex.getMessage());
			}
			level += 1;
		}
	}

	@Override
	public Response toResponse(Exception ex) {

		StringWriter errorStackTrace = new StringWriter();
		ex.printStackTrace(new PrintWriter(errorStackTrace));

		// is a validation PAYLOAD REST exception?
		if (ex instanceof DemoiselleRestException
				|| (ex.getCause() != null && ex.getCause() instanceof DemoiselleRestException)) {
			DemoiselleRestException exDemoiselleREST = null;

			if (ex instanceof DemoiselleRestException) {
				exDemoiselleREST = (DemoiselleRestException) ex;
			} else {
				exDemoiselleREST = (DemoiselleRestException) ex.getCause();
			}

			// All the validation Payload exceptions
			if (!exDemoiselleREST.getMessages().isEmpty()) {
				for (DemoiselleRestExceptionMessage excep : exDemoiselleREST.getMessages()) {
					logger.warning(excep.getError() + ":" + excep.getError_description());
				}
				return status(exDemoiselleREST.getStatusCode()).entity(exDemoiselleREST.getMessages())
						.type(APPLICATION_JSON).build();
				// DemoiselleRest exception with msg
			} else if (exDemoiselleREST.getStatusCode() > 0) {
				int code = exDemoiselleREST.getStatusCode();
				String msg = ex.getMessage();

				if (msg.isEmpty()) {
					msg = Status.fromStatusCode(code).getReasonPhrase();
				}

				logger.warning(msg);
				logAllStackTrace(exDemoiselleREST);
				return status(code).entity(new DemoiselleRestExceptionMessage("server_error", msg, null))
						.type(APPLICATION_JSON).build();
			}

		}
		// other exceptions with msg
		if (ex.getMessage() != null) {
			// Default SERVER ERROR , but try to find the RESPONSE status if is
			// a WebApplicationException
			// http://docs.oracle.com/javaee/7/api/javax/ws/rs/WebApplicationException.html
			int responseCode = INTERNAL_SERVER_ERROR.getStatusCode();
			if (ex instanceof WebApplicationException) {
				responseCode = ((WebApplicationException) ex).getResponse().getStatus();
			}
			logger.warning(ex.getMessage());
			logAllStackTrace(ex);
			return status(responseCode)
					.entity(new DemoiselleRestExceptionMessage("server_error", ex.getMessage(), null))
					.type(APPLICATION_JSON).build();
		}

		// other exceptions without msg
		logAllStackTrace(ex);
		return status(INTERNAL_SERVER_ERROR.getStatusCode())
				.entity(new DemoiselleRestExceptionMessage("server_error", "unknow server error", null))
				.type(APPLICATION_JSON).build();
	}

}
