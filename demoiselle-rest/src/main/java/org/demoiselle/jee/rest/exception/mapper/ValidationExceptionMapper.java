/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.mapper;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.core.exception.ExceptionTreatment;

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
 * { @link
 * https://dennis-xlc.gitbooks.io/restful-java-with-jax-rs-2-0-2rd-edition/content/en/part1/chapter7/exception_handling.html
 * }
 * 
 * @author SERPRO
 *
 */

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

	private static final Logger logger = Logger.getLogger(AnyOtherExceptionMapper.class.getName());

	@Context
	protected HttpServletRequest httpRequest;

	@Inject
	protected ExceptionTreatment errorTreatment;

	@Override
	public Response toResponse(ValidationException exception) {
		logger.info("Using ValidationExceptionMapper");
		return errorTreatment.getFormatedError(exception, httpRequest);
	}

}