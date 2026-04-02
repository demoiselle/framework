/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;

import org.demoiselle.jee.core.exception.ExceptionTreatment;
import org.demoiselle.jee.rest.DemoiselleRestConfig;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;
import org.demoiselle.jee.rest.message.DemoiselleRESTMessage;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.demoiselle.jee.rest.exception.DemoiselleRestExceptionMessage;

/**
 * Default implementation of All Exception Treatments in Demoiselle Framework.
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
    private DemoiselleRESTMessage messages;

    @Inject
    private DemoiselleRestConfig config;
  
    public ExceptionTreatmentImpl() {

    }

    public Response getFormatedError(Throwable exception, HttpServletRequest request) {
        if (config.isRfc9457()) {
            return getFormatedErrorRfc9457(exception, request);
        }
        return getFormatedErrorLegacy(exception, request);
    }

    // ── RFC 9457 format ────────────────────────────────────────────

    private static final String PROBLEM_JSON = "application/problem+json";

    Response getFormatedErrorRfc9457(Throwable exception, HttpServletRequest request) {
        final boolean isShowErrorDetails = config.isShowErrorDetails();

        // Unwrap DemoiselleRestException from cause chain
        if (exception.getCause() != null && exception.getCause() instanceof DemoiselleRestException) {
            exception = (Exception) exception.getCause();
        }

        ProblemDetail pd = new ProblemDetail();

        // instance is always filled with request URI when request is not null
        if (request != null) {
            pd.setInstance(request.getRequestURI());
        }

        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException c = (ConstraintViolationException) exception;
            pd.setTitle("Validation Failed");
            pd.setStatus(412);

            List<Map<String, String>> violations = new ArrayList<>();
            c.getConstraintViolations().forEach(violation -> {
                Map<String, String> v = new LinkedHashMap<>();
                String objectType = violation.getLeafBean().getClass().getSimpleName();
                String arg = "arg0";
                String pathConverted = violation.getPropertyPath().toString().replaceAll(arg, objectType);
                v.put("field", pathConverted);
                v.put("message", violation.getMessage());
                violations.add(v);
            });
            pd.extension("violations", violations);

        } else if (getSQLExceptionInException(exception) != null) {
            SQLException sqlException = getSQLExceptionInException(exception);
            pd.setTitle("Database Error");
            pd.setStatus(500);
            if (isShowErrorDetails && sqlException.getMessage() != null) {
                pd.setDetail(sqlException.getMessage());
            }

        } else if (exception instanceof DemoiselleRestException) {
            DemoiselleRestException e = (DemoiselleRestException) exception;
            pd = mapToProblemDetail(e, isShowErrorDetails);
            if (request != null) {
                pd.setInstance(request.getRequestURI());
            }

        } else if (exception instanceof InvalidFormatException) {
            pd.setTitle("Malformed Input");
            pd.setStatus(400);
            if (isShowErrorDetails && exception.getMessage() != null) {
                pd.setDetail(exception.getMessage());
            }

        } else if (exception instanceof ClientErrorException) {
            ClientErrorException exClient = (ClientErrorException) exception;
            pd.setTitle("HTTP Error");
            pd.setStatus(exClient.getResponse().getStatus());
            if (isShowErrorDetails && exception.getMessage() != null) {
                pd.setDetail(exception.getMessage());
            }

        } else {
            // Generic exception
            pd.setTitle("Internal Server Error");
            pd.setStatus(500);
            if (isShowErrorDetails && exception.getMessage() != null) {
                pd.setDetail(exception.getMessage());
            }
        }

        pd.applyAboutBlankDefaults();

        return Response.status(pd.getStatus())
                .entity(pd)
                .type(PROBLEM_JSON)
                .build();
    }

    // ── DemoiselleRestException → ProblemDetail mapping ────────────

    /**
     * Maps a {@link DemoiselleRestException} to a {@link ProblemDetail}.
     *
     * <p>The first message's {@code error} → {@code title},
     * {@code errorDescription} → {@code detail} (when {@code showDetails} is true),
     * and non-empty {@code errorLink} → {@code type}.
     * All messages are included as the {@code "messages"} extension.</p>
     *
     * @param e           the exception to map
     * @param showDetails whether to include detail information
     * @return a populated ProblemDetail (without instance; caller sets it)
     */
    ProblemDetail mapToProblemDetail(DemoiselleRestException e, boolean showDetails) {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(e.getStatusCode() != 0 ? e.getStatusCode() : 500);

        if (!e.getMessages().isEmpty()) {
            var first = e.getMessages().iterator().next();
            pd.setTitle(first.error());
            if (showDetails) {
                pd.setDetail(first.errorDescription());
            }
            if (first.errorLink() != null && !first.errorLink().isBlank()) {
                pd.setType(first.errorLink());
            }
            pd.extension("messages", mapMessages(e.getMessages(), showDetails));
        } else {
            pd.setTitle(e.getMessage());
            if (showDetails) {
                pd.setDetail(e.getMessage());
            }
        }

        pd.applyAboutBlankDefaults();
        return pd;
    }

    /**
     * Converts a collection of {@link DemoiselleRestExceptionMessage} to a list
     * of maps suitable for the {@code "messages"} extension in ProblemDetail.
     */
    private List<Map<String, String>> mapMessages(
            Collection<DemoiselleRestExceptionMessage> messages, boolean showDetails) {
        List<Map<String, String>> result = new ArrayList<>();
        for (DemoiselleRestExceptionMessage msg : messages) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("error", msg.error());
            if (showDetails && msg.errorDescription() != null) {
                m.put("error_description", msg.errorDescription());
            }
            if (msg.errorLink() != null && !msg.errorLink().isEmpty()) {
                m.put("error_link", msg.errorLink());
            }
            result.add(m);
        }
        return result;
    }

    // ── Legacy format ──────────────────────────────────────────────

    Response getFormatedErrorLegacy(Throwable exception, HttpServletRequest request) {

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

        ArrayList<Object> arrayErrors = new ArrayList<>();

        /*
		 * Treatment of Beans Validation
		 * (Violations: @NotNull, @NotEmpty, @Size...)
         */
        if (exception instanceof ConstraintViolationException) {

            ConstraintViolationException c = (ConstraintViolationException) exception;

            c.getConstraintViolations().stream().forEach((violation) -> {
                String objectType = violation.getLeafBean().getClass().getSimpleName();

                // This is fixed because REST beans validations only accept ONE
                // parameter
                String arg = "arg0";

                // Before: pesist.arg0.name / After: pesist.User.name
                String pathConverted = violation.getPropertyPath().toString().replaceAll(arg, objectType);

                Map<String, Object> object = new ConcurrentHashMap<>();
                object.putIfAbsent(FIELDNAME_ERROR, pathConverted);
                object.putIfAbsent(FIELDNAME_ERROR_DESCRIPTION, violation.getMessage());

                logger.log(Level.FINEST, violation.getMessage());

                arrayErrors.add(object);
            });

            return buildResponse(arrayErrors, responseMediaType, Status.PRECONDITION_FAILED);
        }

        /*
		 * Database errors
         */
        SQLException sqlException = getSQLExceptionInException(exception);

        if (sqlException != null) {

            Map<String, Object> sqlError = new ConcurrentHashMap<>();

            Integer errorCode = ((SQLException) sqlException).getErrorCode();
            
            sqlError.putIfAbsent(DATABASE_SQL_STATE, ((SQLException) sqlException).getSQLState());
            sqlError.putIfAbsent(DATABASE_ERROR_CODE, errorCode);
            sqlError.putIfAbsent(DATABASE_MASSAGE, sqlException.getMessage());

            Map<String, Object> object = new ConcurrentHashMap<>();

            if (isShowErrorDetails) {
                object.putIfAbsent(FIELDNAME_ERROR_DESCRIPTION, sqlError);        
            }
            
            /*
    		 * First verify custom sqlError messages in demoiselle.properties file:
    		 * Ex : demoiselle.rest.sqlError.<errorCode>= the msg...
             */
            
            if( config.getSqlError().get(errorCode.toString()) != null ){
            	object.putIfAbsent(FIELDNAME_ERROR, config.getSqlError().get(errorCode.toString()) );
            }else {
	            if (exception.getMessage() != null && !exception.getMessage().isEmpty()) {
	                object.putIfAbsent(FIELDNAME_ERROR, exception.getMessage());
	            } else {
	                object.putIfAbsent(FIELDNAME_ERROR, messages.unhandledDatabaseException());
	            }
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
                Map<String, Object> object = new ConcurrentHashMap<>();
                object.putIfAbsent(FIELDNAME_ERROR, e.getMessage());
                object.putIfAbsent(FIELDNAME_ERROR_DESCRIPTION, unwrapException(exception));
                arrayErrors.add(object);
            }

            e.getMessages().stream().map((message) -> {
                Map<String, Object> object = new ConcurrentHashMap<>();
                object.putIfAbsent(FIELDNAME_ERROR, message.error());
                if (isShowErrorDetails) {
                    object.putIfAbsent(FIELDNAME_ERROR_DESCRIPTION, message.errorDescription());
                }
                if (message.errorLink() != null && !message.errorLink().isEmpty()) {
                    object.putIfAbsent(FIELDNAME_ERROR_LINK, message.errorLink());
                }
                return object;
            }).forEachOrdered((object) -> {
                arrayErrors.add(object);
            });

            Status statusCode = Status.PRECONDITION_FAILED;

            if (e.getStatusCode() != 0) {
                statusCode = Status.fromStatusCode(e.getStatusCode());
            }
            
            if(config.isShowErrorDetails()){
	            if(exception.getMessage() != null)
	    			logger.log(Level.WARNING, exception.getMessage());
	            
				if(exception.getCause() != null && exception.getCause().getMessage() != null)
					logger.log(Level.WARNING, exception.getCause().getMessage());	 
					
            }
            
            return buildResponse(arrayErrors, responseMediaType, statusCode);
        }

        /*
		 * If InvalidFormatException probably is malformed input or output
         */
        if (exception instanceof InvalidFormatException) {
            Map<String, Object> object = new ConcurrentHashMap<>();
            object.putIfAbsent(FIELDNAME_ERROR, messages.unhandledMalformedInputOutputException());
            if (isShowErrorDetails) {
                object.putIfAbsent(FIELDNAME_ERROR_DESCRIPTION, unwrapException(exception));
            }
            arrayErrors.add(object);

            return buildResponse(arrayErrors, responseMediaType, Status.BAD_REQUEST);
        }

        /*
		 * Treat HTTP error code Exceptions
         */
        if (exception instanceof ClientErrorException) {

            ClientErrorException exClient = (ClientErrorException) exception;

            Map<String, Object> object = new ConcurrentHashMap<>();
            object.putIfAbsent(FIELDNAME_ERROR, messages.httpException());
            if (isShowErrorDetails) {
                object.putIfAbsent(FIELDNAME_ERROR_DESCRIPTION, unwrapException(exception));
            }
            arrayErrors.add(object);

            return buildResponse(arrayErrors, responseMediaType, (Status) exClient.getResponse().getStatusInfo());
        }

        /*
		 * Generic errors
         */
        Map<String, Object> object = new ConcurrentHashMap<>();
        object.putIfAbsent(FIELDNAME_ERROR, messages.unhandledServerException());
        if (isShowErrorDetails) {
            object.putIfAbsent(FIELDNAME_ERROR_DESCRIPTION, unwrapException(exception));
        }
        arrayErrors.add(object);

        return buildResponse(arrayErrors, responseMediaType, Status.INTERNAL_SERVER_ERROR);

    }

    /**
     * This method return SQL Exception in stack of Exceptions (if exists), or
     * null.
     *
     * @param ex Exception
     * @return SQLException or null
     */
    private SQLException getSQLExceptionInException(Throwable ex) {
        Throwable current = ex;
        do {
            if (current instanceof SQLException) {
                return (SQLException) current;
            }
            current = current.getCause();
        } while (current != null);
        return null;
    }

    /**
     * Method that return @Response object with media type and status code.
     *
     * @param entity Entity inside a response
     * @param mediaType Media Type of response
     * @param status Status code of response
     * @return Created response
     */
    private Response buildResponse(Object entity, MediaType mediaType, Status status) {
        ResponseBuilder builder = Response.status(status).entity(entity);
        builder.type(mediaType);
        return builder.build();
    }

    private ArrayList<String> unwrapException(Throwable t) {
        ArrayList<String> array = new ArrayList<>();
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
