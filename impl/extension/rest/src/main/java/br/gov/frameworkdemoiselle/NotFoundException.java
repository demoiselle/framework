package br.gov.frameworkdemoiselle;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class NotFoundException extends HttpViolationException {

	private static final long serialVersionUID = 1L;

	public NotFoundException() {
		super(SC_NOT_FOUND);
	}
}
