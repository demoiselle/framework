/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.demoiselle.jee.core.api.error.ErrorTreatmentInterface;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;
import org.demoiselle.jee.rest.exception.DemoiselleRestExceptionMessage;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

// TODO: logs dos erros (USER CDI.current..)
// TODO: flag de details
// TODO: encontrar alguma maneira de que o sistema desenvolvido possa SOBREESCREVER esse comportamento
// TODO: everride app
public class ErrorTreatment implements ErrorTreatmentInterface {

	// private Logger logger = CDI.current().select(Logger.class).get();

	private final String FIELDNAME_ERROR = "error";
	private final String FIELDNAME_ERROR_DESCRIPTION = "error_description";
	private final String FIELDNAME_ERROR_LINK = "error_link";

	public ErrorTreatment() {

	}

	@SuppressWarnings({ "rawtypes" })
	public Response getFormatedError(Exception exception, HttpServletRequest request) {

		final MediaType responseMediaType = MediaType.valueOf(request.getHeader("content-type"));

		// If the main cause of exception is Demoiselle Rest Exception
		if (exception.getCause() != null && exception.getCause() instanceof DemoiselleRestException) {
			exception = (Exception) exception.getCause();
		}

		ArrayList<Object> a = new ArrayList<Object>();

		/*
		 * Treatment of Beans Validation
		 * (Violations: @NotNull, @NotEmpty, @Size...)
		 */
		if (exception instanceof ConstraintViolationException) {
			ConstraintViolationException c = (ConstraintViolationException) exception;

			for (ConstraintViolation violation : c.getConstraintViolations()) {

				// Campo tem que ser entre 2 e 100 caracf..
				// System.out.println(violation.getMessage());

				// pesist.arg0.name
				// System.out.println(violation.getPropertyPath());

				// User
				// System.out.println(violation.getLeafBean().getClass().getSimpleName());

				String objectType = violation.getLeafBean().getClass().getSimpleName();
				String arg = "arg0";

				// TODO: Ver como fica com mais de um arg (arg0, arg1, arg2)
				// ANTES: pesist.arg0.name / DEPOIS: pesist.User.name
				String pathConverted = violation.getPropertyPath().toString().replaceAll(arg, objectType);

				HashMap<String, Object> object = new HashMap<String, Object>();
				object.put(FIELDNAME_ERROR, pathConverted);
				object.put(FIELDNAME_ERROR_DESCRIPTION, violation.getMessage());

				a.add(object);
			}

			// TODO: Verificar se o status code é 412 mesmo
			return buildResponse(a, responseMediaType, Status.PRECONDITION_FAILED);
		}

		/*
		 * Database errors
		 */
		if (exception instanceof PersistenceException) {
			HashMap<String, Object> object = new HashMap<String, Object>();
			object.put(FIELDNAME_ERROR_DESCRIPTION, unwrapException(exception));

			// TODO: messages
			object.put(FIELDNAME_ERROR, "Unhandled database exception");

			a.add(object);
			return buildResponse(a, responseMediaType, Status.INTERNAL_SERVER_ERROR);
		}

		if (exception instanceof SQLException) {

			HashMap<String, Object> sqlError = new HashMap<String, Object>();

			sqlError.put("SQLState", ((SQLException) exception).getSQLState());
			sqlError.put("Error Code", ((SQLException) exception).getErrorCode());
			sqlError.put("Message", exception.getMessage());

			HashMap<String, Object> object = new HashMap<String, Object>();
			object.put(FIELDNAME_ERROR_DESCRIPTION, sqlError);

			// TODO: messages
			object.put(FIELDNAME_ERROR, "Unhandled database exception");

			a.add(object);

			return buildResponse(a, responseMediaType, Status.INTERNAL_SERVER_ERROR);
		}

		/*
		 * Data format errors
		 */
		if (exception instanceof InvalidFormatException) {
			HashMap<String, Object> object = new HashMap<String, Object>();

			// TODO: messages
			object.put(FIELDNAME_ERROR, "Unhandled format exception exception");
			object.put(FIELDNAME_ERROR_DESCRIPTION, unwrapException(exception));

			a.add(object);
			return buildResponse(a, responseMediaType, Status.BAD_REQUEST);
		}

		/*
		 * Business errors
		 */
		if (exception instanceof DemoiselleRestException) {
			DemoiselleRestException e = (DemoiselleRestException) exception;

			if (!e.getMessage().isEmpty()) {
				HashMap<String, Object> object = new HashMap<String, Object>();
				object.put(FIELDNAME_ERROR, e.getMessage());
				object.put(FIELDNAME_ERROR_DESCRIPTION, unwrapException(exception));
				a.add(object);
			}

			for (DemoiselleRestExceptionMessage message : e.getMessages()) {

				HashMap<String, Object> object = new HashMap<String, Object>();
				object.put(FIELDNAME_ERROR, message.getError());
				object.put(FIELDNAME_ERROR_DESCRIPTION, message.getError_description());

				if (message.getError_link() != null && !message.getError_link().isEmpty()) {
					object.put(FIELDNAME_ERROR_LINK, message.getError_link());
				}

				a.add(object);
			}

			Status statusCode = Status.PRECONDITION_FAILED;

			if (e.getStatusCode() != 0) {
				statusCode = Status.fromStatusCode(e.getStatusCode());
			}

			return buildResponse(a, responseMediaType, statusCode);
		}

		/*
		 * Generic errors
		 */
		// TODO: add flag for detailed error on Response
		HashMap<String, Object> object = new HashMap<String, Object>();
		object.put(FIELDNAME_ERROR, unwrapException(exception));
		a.add(object);

		return buildResponse(a, responseMediaType, Status.INTERNAL_SERVER_ERROR);

	}

	protected Response buildResponse(Object entity, MediaType mediaType, Status status) {
		ResponseBuilder builder = Response.status(status).entity(entity);
		builder.type(mediaType);
		return builder.build();
	}

	protected ArrayList<String> unwrapException(Throwable t) {
		ArrayList<String> array = new ArrayList<String>();
		doUnwrapException(array, t);
		return array;
	}

	private void doUnwrapException(ArrayList<String> array, Throwable t) {
		if (t == null) {
			return;
		}

		array.add(t.getMessage());

		if (t.getCause() != null && t != t.getCause()) {
			doUnwrapException(array, t.getCause());
		}
	}

}
