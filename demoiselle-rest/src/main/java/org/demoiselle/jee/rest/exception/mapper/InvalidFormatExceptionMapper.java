package org.demoiselle.jee.rest.exception.mapper;

import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.core.api.error.ErrorTreatmentInterface;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@Provider
public class InvalidFormatExceptionMapper implements ExceptionMapper<InvalidFormatException> {

	private Logger logger = CDI.current().select(Logger.class).get();
	
	@Context
	protected HttpServletRequest httpRequest;

	@Inject
	protected ErrorTreatmentInterface errorTreatment;

	@Override
	public Response toResponse(InvalidFormatException exception) {
		logger.fine("Using InvalidFormatExceptionMapper");
		return errorTreatment.getFormatedError(exception, httpRequest);
	}

}