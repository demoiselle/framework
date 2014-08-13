package br.gov.frameworkdemoiselle;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

public class ForbiddenException extends HttpViolationException {

	private static final long serialVersionUID = 1L;

	public ForbiddenException() {
		super(SC_FORBIDDEN);
	}
}
