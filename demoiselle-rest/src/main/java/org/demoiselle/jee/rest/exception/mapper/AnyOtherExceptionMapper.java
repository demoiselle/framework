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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.core.exception.ExceptionTreatment;

/**
 * Any other exception is mapped by this class and when toResponse method is
 * called then sends to @ExceptionTreatment to treat error and return the
 * correct format in @Response object.
 * 
 * @author SERPRO
 * 
 */
@Provider
public class AnyOtherExceptionMapper implements ExceptionMapper<Throwable> {

	private static final Logger logger = Logger.getLogger(AnyOtherExceptionMapper.class.getName());

	@Context
	protected HttpServletRequest httpRequest;

	@Inject
	protected ExceptionTreatment exceptionTreatment;

	@Override
	public Response toResponse(Throwable exception) {
		logger.finest("Using AnyOtherExceptionMapper");
		return exceptionTreatment.getFormatedError(exception, httpRequest);
	}

}
