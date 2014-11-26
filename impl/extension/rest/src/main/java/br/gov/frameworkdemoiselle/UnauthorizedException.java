package br.gov.frameworkdemoiselle;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public class UnauthorizedException extends HttpViolationException {
	
	private static final long serialVersionUID = 1L;

	public UnauthorizedException() {
		super(SC_UNAUTHORIZED);
	}

}
