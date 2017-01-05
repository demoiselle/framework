package org.demoiselle.jee.core.api.error;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

public interface ErrorTreatmentInterface {

	public Response getFormatedError(Exception exception, HttpServletRequest request);

}
