/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.demoiselle.jee.core.exception.ExceptionTreatment;
import org.demoiselle.jee.rest.DemoiselleRestConfig;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;
import org.demoiselle.jee.rest.exception.DemoiselleRestExceptionMessage;

/**
 * Default implementation of All Exception Treatments iun Demoiselle Framework.
 * 
 * This implementation is based on the RFC 6749 (The OAuth 2.0 Authorization
 * Framework) { @link https://tools.ietf.org/html/rfc6749#page-45 }
 * 
 * @author SERPRO
 *
 */
public class ExceptionTreatmentImpl implements ExceptionTreatment {

	private static final Logger logger = Logger.getLogger(ExceptionTreatmentImpl.class.getName());

	private final String FIELDNAME_ERROR = "error";
	private final String FIELDNAME_ERROR_DESCRIPTION = "error_description";
	private final String FIELDNAME_ERROR_LINK = "error_link";

	private final String DATABASE_SQL_STATE = "sql_state";
	private final String DATABASE_MASSAGE = "error_message";
	private final String DATABASE_ERROR_CODE = "error_code";

	@Inject
	private DemoiselleRestConfig config;

	public ExceptionTreatmentImpl() {

	}

	@SuppressWarnings({ "rawtypes" })
	public Response getFormatedError(Throwable exception, HttpServletRequest request) {

		// Variable to enable to show datails of errors
		final boolean isShowErrorDetails = config.isShowErrorDetails();

		MediaType responseMediaType = MediaType.APPLICATION_JSON_TYPE;

		if (request.getHeader("content-type") != null) {
			responseMediaType = MediaType.valueOf(request.getHeader("content-type"));
		}

		// If the main cause of exception is Demoiselle Rest Exception
		if (exception.getCause() != null && exception.getCause() instanceof DemoiselleRestException) {
			exception = (Exception) exception.getCause();
		}

		ArrayList<Object> arrayErrors = new ArrayList<Object>();

		/*
		 * Treatment of Beans Validation
		 * (Violations: @NotNull, @NotEmpty, @Size...)
		 */
		if (exception instanceof ConstraintViolationException) {

			ConstraintViolationException c = (ConstraintViolationException) exception;

			for (ConstraintViolation violation : c.getConstraintViolations()) {

				String objectType = violation.getLeafBean().getClass().getSimpleName();
				String arg = "arg0";

				// TODO: Ver como fica com mais de um arg (arg0, arg1, arg2)
				// ANTES: pesist.arg0.name / DEPOIS: pesist.User.name
				String pathConverted = violation.getPropertyPath().toString().replaceAll(arg, objectType);

				HashMap<String, Object> object = new HashMap<String, Object>();
				object.put(FIELDNAME_ERROR, pathConverted);
				object.put(FIELDNAME_ERROR_DESCRIPTION, violation.getMessage());

				logger.log(Level.FINEST, violation.getMessage());

				arrayErrors.add(object);
			}

			// TODO: Verificar se o status code é 412 mesmo
			return buildResponse(arrayErrors, responseMediaType, Status.PRECONDITION_FAILED);
		}

		/*
		 * Database errors
		 */
		SQLException sqlException = getSQLExceptionInException(exception);

		if (sqlException != null) {

			HashMap<String, Object> sqlError = new HashMap<String, Object>();

			sqlError.put(DATABASE_SQL_STATE, ((SQLException) sqlException).getSQLState());
			sqlError.put(DATABASE_ERROR_CODE, ((SQLException) sqlException).getErrorCode());
			sqlError.put(DATABASE_MASSAGE, sqlException.getMessage());

			HashMap<String, Object> object = new HashMap<String, Object>();

			if (isShowErrorDetails)
				object.put(FIELDNAME_ERROR_DESCRIPTION, sqlError);

			// TODO: messages
			if (exception.getMessage() != null && !exception.getMessage().isEmpty()) {
				object.put(FIELDNAME_ERROR, exception.getMessage());
			} else {
				object.put(FIELDNAME_ERROR, "Unhandled database exception");
			}

			arrayErrors.add(object);

			return buildResponse(arrayErrors, responseMediaType, Status.INTERNAL_SERVER_ERROR);
		}

		/*
		 * Demoiselle errors
		 */
		if (exception instanceof DemoiselleRestException) {
			DemoiselleRestException e = (DemoiselleRestException) exception;

			if (e.getMessage() != null && !e.getMessage().isEmpty()) {
				HashMap<String, Object> object = new HashMap<String, Object>();
				object.put(FIELDNAME_ERROR, e.getMessage());
				object.put(FIELDNAME_ERROR_DESCRIPTION, unwrapException(exception));
				arrayErrors.add(object);
			}

			for (DemoiselleRestExceptionMessage message : e.getMessages()) {

				HashMap<String, Object> object = new HashMap<String, Object>();
				object.put(FIELDNAME_ERROR, message.getError());

				if (isShowErrorDetails)
					object.put(FIELDNAME_ERROR_DESCRIPTION, message.getError_description());

				if (message.getError_link() != null && !message.getError_link().isEmpty()) {
					object.put(FIELDNAME_ERROR_LINK, message.getError_link());
				}

				arrayErrors.add(object);
			}

			Status statusCode = Status.PRECONDITION_FAILED;

			if (e.getStatusCode() != 0) {
				statusCode = Status.fromStatusCode(e.getStatusCode());
			}

			return buildResponse(arrayErrors, responseMediaType, statusCode);
		}

		/*
		 * If IO exception probably is malformed input
		 */
		if (exception instanceof IOException) {
			HashMap<String, Object> object = new HashMap<String, Object>();
			object.put(FIELDNAME_ERROR, "Unhandled malformed input/output exception");
			if (isShowErrorDetails)
				object.put(FIELDNAME_ERROR_DESCRIPTION, unwrapException(exception));
			arrayErrors.add(object);

			return buildResponse(arrayErrors, responseMediaType, Status.BAD_REQUEST);
		}

		/*
		 * Generic errors
		 */
		HashMap<String, Object> object = new HashMap<String, Object>();
		object.put(FIELDNAME_ERROR, "Unhandled server exception");
		if (isShowErrorDetails)
			object.put(FIELDNAME_ERROR_DESCRIPTION, unwrapException(exception));
		arrayErrors.add(object);

		return buildResponse(arrayErrors, responseMediaType, Status.INTERNAL_SERVER_ERROR);

	}

	/**
	 * This method return SQL Exception in stack of Exceptions (if exists), or
	 * null.
	 * 
	 * @param ex
	 *            Exception
	 * @return SQLException or null
	 */
	private SQLException getSQLExceptionInException(Throwable ex) {
		Throwable current = ex;
		do {
			if (current instanceof SQLException) {
				return (SQLException) current;
			}
			current = current.getCause();
			// TODO: e se ela estiver dentro dela mesma?
		} while (current != null);
		return null;
	}

	private Response buildResponse(Object entity, MediaType mediaType, Status status) {
		ResponseBuilder builder = Response.status(status).entity(entity);
		builder.type(mediaType);
		return builder.build();
	}

	private ArrayList<String> unwrapException(Throwable t) {
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
