package br.gov.frameworkdemoiselle;

import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

public class ServiceUnavailableException extends HttpViolationException {

	private static final long serialVersionUID = 1L;

	public ServiceUnavailableException() {
		super(SC_SERVICE_UNAVAILABLE);
	}
}
