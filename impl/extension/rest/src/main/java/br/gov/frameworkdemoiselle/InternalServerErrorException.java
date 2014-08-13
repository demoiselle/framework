package br.gov.frameworkdemoiselle;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class InternalServerErrorException extends HttpViolationException {

	private static final long serialVersionUID = 1L;

	public InternalServerErrorException() {
		super(SC_INTERNAL_SERVER_ERROR);
	}
}
