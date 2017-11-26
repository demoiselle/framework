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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.demoiselle.jee.core.exception.ExceptionTreatment;
import org.demoiselle.jee.rest.DemoiselleRestConfig;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;
import org.demoiselle.jee.rest.message.DemoiselleRESTMessage;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    @SuppressWarnings({"rawtypes"})
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
                object.putIfAbsent(FIELDNAME_ERROR, message.getError());
                if (isShowErrorDetails) {
                    object.putIfAbsent(FIELDNAME_ERROR_DESCRIPTION, message.getError_description());
                }
                if (message.getError_link() != null && !message.getError_link().isEmpty()) {
                    object.putIfAbsent(FIELDNAME_ERROR_LINK, message.getError_link());
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
