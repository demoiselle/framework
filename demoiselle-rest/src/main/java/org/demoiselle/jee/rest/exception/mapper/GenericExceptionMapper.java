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
import java.util.HashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.rest.exception.DemoiselleRESTException;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

	public Response toResponse(Exception ex) {

		StringWriter errorStackTrace = new StringWriter();
		ex.printStackTrace(new PrintWriter(errorStackTrace));
		HashMap<String, String> entity = new HashMap<>();

		// Verifica se a exception é de validação de PAYLOAD do REST
		if (ex instanceof DemoiselleRESTException || (ex.getCause() != null && ex.getCause() instanceof DemoiselleRESTException)) {
			DemoiselleRESTException exDemoiselleREST = null;
		
			if (ex instanceof DemoiselleRESTException) {
				exDemoiselleREST = (DemoiselleRESTException) ex;
			} else {
				exDemoiselleREST = (DemoiselleRESTException) ex.getCause();
			}
			
			if (!exDemoiselleREST.getMessages().isEmpty()) {
				return status(exDemoiselleREST.getStatusCode()).entity(exDemoiselleREST.getMessages()).type(APPLICATION_JSON).build();
			} else if (exDemoiselleREST.getStatusCode() > 0) {
				entity.put("error", exDemoiselleREST.getMessage());
				return status(exDemoiselleREST.getStatusCode()).entity(entity).type(APPLICATION_JSON).build();
			}

		}

		// No caso de existir message ele mostra a MESSAGE da Exception
		if (ex.getMessage() != null) {
			entity.put("error", ex.getMessage());

			// Pega toda as mensagens da stacktrace
			int level = 1;
			while (ex.getCause() != null) {
				ex = (Exception) ex.getCause();
				if (ex != null && ex.getMessage() != null && !ex.getMessage().isEmpty()) {
					entity.put("inner_cause_" + level, ex.getMessage());
				}
				level += 1;
			}

			// Por padrão retorna SERVER ERROR, mas tenta encontrar o status do
			// RESPONSE se for WebApplicationException
			// http://docs.oracle.com/javaee/7/api/javax/ws/rs/WebApplicationException.html
			int responseCode = INTERNAL_SERVER_ERROR.getStatusCode();
			if (ex instanceof WebApplicationException) {
				responseCode = ((WebApplicationException) ex).getResponse().getStatus();
			}

			return status(responseCode).entity(entity).type(APPLICATION_JSON).build();
		}

		entity.put("error", "Erro interno desconhecido no servidor.");
		return status(INTERNAL_SERVER_ERROR.getStatusCode())
                        .entity(entity).type(APPLICATION_JSON).build();
	}

}
