/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.mapper;

import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.core.api.error.ErrorTreatmentInterface;

/**
 * {@link ExceptionMapper} for {@link ValidationException}.
 * <p>
 * Send a {@link ViolationReport} in {@link Response} in addition to HTTP
 * 400/500 status code. Supported media types are: {@code application/json} /
 * {@code application/xml} (if appropriate provider is registered on server).
 * </p>
 *
 * @see org.jboss.resteasy.api.validation.ResteasyViolationExceptionMapper The
 *      original WildFly class:
 *      {@code org.jboss.resteasy.api.validation.ResteasyViolationExceptionMapper}
 *      
 */

/**
 * When an exception is thrown, JAX-RS will first try to find an ExceptionMapper
 * for that exception’s type. If it cannot find one, it will look for a mapper
 * that can handle the exception’s superclass. It will continue this process
 * until there are no more superclasses to match against.
 * 
 * @link https://dennis-xlc.gitbooks.io/restful-java-with-jax-rs-2-0-2rd-edition/content/en/part1/chapter7/exception_handling.html
 * 
 * @author 00968514901
 *
 */

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

	private Logger logger = CDI.current().select(Logger.class).get();

	@Context
	protected HttpServletRequest httpRequest;

	@Inject
	protected ErrorTreatmentInterface errorTreatment;

	@Override
	public Response toResponse(ValidationException exception) {

		logger.fine("Using ValidationExceptionMapper");

		// exception.printStackTrace();
		//
		// System.out.println("============ FRAMEWORK");
		//
		// if (exception instanceof ConstraintDefinitionException) {
		// return buildResponse(unwrapException(exception),
		// MediaType.TEXT_PLAIN, Status.INTERNAL_SERVER_ERROR);
		// }
		// if (exception instanceof ConstraintDeclarationException) {
		// return buildResponse(unwrapException(exception),
		// MediaType.TEXT_PLAIN, Status.INTERNAL_SERVER_ERROR);
		// }
		// if (exception instanceof GroupDefinitionException) {
		// return buildResponse(unwrapException(exception),
		// MediaType.TEXT_PLAIN, Status.INTERNAL_SERVER_ERROR);
		// }
		//
		// // ValidatorFactory dfv = buildDefaultValidatorFactory();
		// // Validator validator = dfv.getValidator();
		// if (exception instanceof ConstraintViolationException) {
		// ConstraintViolationException c = (ConstraintViolationException)
		// exception;
		//
		// for (ConstraintViolation violation : c.getConstraintViolations()) {
		// System.out.println(violation.getMessage()); // Campo tem que ser
		// // entre 2 e 100
		// // caracf..
		// System.out.println(violation.getPropertyPath()); // pesist.arg0.name
		// System.out.println(violation.getLeafBean().getClass().getSimpleName());
		// // User
		//
		// String objectType =
		// violation.getLeafBean().getClass().getSimpleName();
		// String arg = "arg0";
		//
		// // ANTES: pesist.arg0.name / DEPOIS: pesist.User.name
		// System.out.println(violation.getPropertyPath().toString().replaceAll(arg,
		// objectType));
		// }
		//
		// }

		// if (exception instanceof ResteasyViolationException) {
		// ResteasyViolationException resteasyViolationException =
		// ResteasyViolationException.class.cast(exception);
		// Exception e = resteasyViolationException.getException();
		// if (e != null) {
		// return buildResponse(unwrapException(e), MediaType.TEXT_PLAIN,
		// Status.INTERNAL_SERVER_ERROR);
		// } else if
		// (resteasyViolationException.getReturnValueViolations().size() == 0) {
		// return buildViolationReportResponse(resteasyViolationException,
		// Status.BAD_REQUEST);
		// } else {
		// return buildViolationReportResponse(resteasyViolationException,
		// Status.INTERNAL_SERVER_ERROR);
		// }
		// }

		// throw new DemoiselleException(exception);

		// return buildResponse(unwrapException(exception),
		// MediaType.TEXT_PLAIN, Status.INTERNAL_SERVER_ERROR);

		return errorTreatment.getFormatedError(exception, httpRequest);
	}

	protected Response buildResponse(Object entity, String mediaType, Status status) {
		ResponseBuilder builder = Response.status(status).entity(entity);
		builder.type(MediaType.valueOf(mediaType));
		// builder.header(Validation.VALIDATION_HEADER, "true");
		return builder.build();
	}

	// protected Response
	// buildViolationReportResponse(ResteasyViolationException exception, Status
	// status) {
	// ResponseBuilder builder = Response.status(status);
	// // builder.header(Validation.VALIDATION_HEADER, "true");
	//
	// // Check standard media types.
	// MediaType mediaType = getAcceptMediaType(exception.getAccept());
	// if (mediaType != null) {ExceptionMapper
	// builder.type(mediaType);
	// // builder.entity(new ViolationReport(exception));
	// return builder.build();
	// }
	//
	// // Default media type.
	// builder.type(MediaType.TEXT_PLAIN);
	// builder.entity(exception.toString());
	// return builder.build();
	// }

	protected String unwrapException(Throwable t) {
		StringBuffer sb = new StringBuffer();
		doUnwrapException(sb, t);
		return sb.toString();
	}

	private void doUnwrapException(StringBuffer sb, Throwable t) {
		if (t == null) {
			return;
		}
		sb.append(t.toString());
		if (t.getCause() != null && t != t.getCause()) {
			sb.append('[');
			doUnwrapException(sb, t.getCause());
			sb.append(']');
		}
	}
	//
	// private MediaType getAcceptMediaType(List<MediaType> accept) {
	// Iterator<MediaType> it = accept.iterator();
	// while (it.hasNext()) {
	// MediaType mt = it.next();
	// /*
	// * application/xml media type causes an exception:
	// * org.jboss.resteasy.core.NoMessageBodyWriterFoundFailure: Could
	// * not find MessageBodyWriter for response object of type:
	// * org.jboss.resteasy.api.validation.ViolationReport of media type:
	// * application/xml
	// */
	// /*
	// * if (MediaType.APPLICATION_XML_TYPE.getType().equals(mt.getType())
	// * &&
	// * MediaType.APPLICATION_XML_TYPE.getSubtype().equals(mt.getSubtype(
	// * ))) { return MediaType.APPLICATION_XML_TYPE; }
	// */
	// if (MediaType.APPLICATION_JSON_TYPE.getType().equals(mt.getType())
	// && MediaType.APPLICATION_JSON_TYPE.getSubtype().equals(mt.getSubtype()))
	// {
	// return MediaType.APPLICATION_JSON_TYPE;
	// }
	// }
	// return null;
	// }
}