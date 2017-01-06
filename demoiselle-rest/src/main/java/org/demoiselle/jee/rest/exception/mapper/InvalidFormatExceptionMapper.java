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

import org.demoiselle.jee.core.api.error.ErrorTreatment;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@Provider
public class InvalidFormatExceptionMapper implements ExceptionMapper<InvalidFormatException> {

	private static final Logger logger = Logger.getLogger(InvalidFormatExceptionMapper.class.getName());

	@Context
	protected HttpServletRequest httpRequest;

	@Inject
	protected ErrorTreatment errorTreatment;

	@Override
	public Response toResponse(InvalidFormatException exception) {
		logger.info("Using InvalidFormatExceptionMapper");
		return errorTreatment.getFormatedError(exception, httpRequest);
	}

}