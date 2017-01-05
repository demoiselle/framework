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
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.core.api.error.ErrorTreatmentInterface;

@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

	private Logger logger = CDI.current().select(Logger.class).get();
	
	@Context
	protected HttpServletRequest httpRequest;

	@Inject
	protected ErrorTreatmentInterface errorTreatment;

	@Override
	public Response toResponse(PersistenceException exception) {
		logger.fine("Using PersistenceExceptionMapper");
		return errorTreatment.getFormatedError(exception, httpRequest);
	}

}