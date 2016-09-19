package org.demoiselle.jee.ws.exception.mapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.ws.exception.DemoiselleRESTException;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

	public Response toResponse(Exception ex) {

		StringWriter errorStackTrace = new StringWriter();
		ex.printStackTrace(new PrintWriter(errorStackTrace));

		// Verifica se a exception é de validação de PAYLOAD do REST
		if (ex.getCause() instanceof DemoiselleRESTException) {
			DemoiselleRESTException exDemoiselleREST = (DemoiselleRESTException) ex.getCause();
			if (!exDemoiselleREST.getMessages().isEmpty()) {
				return Response.status(exDemoiselleREST.getStatusCode()).entity(exDemoiselleREST.getMessages())
						.type(MediaType.APPLICATION_JSON).build();
			}
		}

		HashMap<String, String> entity = new HashMap<String, String>();

		// No caso de existir message ele mostra a MESSAGE da Exception
		if (ex.getMessage() != null) {
			entity.put("error", ex.getMessage());			

			// Pega toda as mensagens da stacktrace
			int level = 1;
			while (ex.getCause() != null) {
				ex = (Exception) ex.getCause();
				if (!ex.getMessage().isEmpty()) {
					entity.put("inner_cause_" + level, ex.getMessage());
				}
				level += 1;
			}
			
			// Por padrão retorna SERVER ERROR, mas tenta encontrar o status do RESPONSE se for WebApplicationException
			// http://docs.oracle.com/javaee/7/api/javax/ws/rs/WebApplicationException.html
			int responseCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
			if (ex instanceof WebApplicationException) {
				responseCode = ((WebApplicationException)ex).getResponse().getStatus();
			}

			return Response.status(responseCode).entity(entity).type(MediaType.APPLICATION_JSON).build();
		}

		entity.put("error", "Erro interno desconhecido no servidor.");
		return Response.status(500).entity(entity).type(MediaType.APPLICATION_JSON).build();
	}

}
