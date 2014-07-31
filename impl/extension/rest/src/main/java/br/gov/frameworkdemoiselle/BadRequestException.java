package br.gov.frameworkdemoiselle;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class BadRequestException extends HttpViolationException {

	private static final long serialVersionUID = 1L;

	public BadRequestException() {
		super(SC_BAD_REQUEST);
	}
}
