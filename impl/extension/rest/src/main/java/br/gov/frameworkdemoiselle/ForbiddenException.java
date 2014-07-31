package br.gov.frameworkdemoiselle;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import javax.xml.ws.http.HTTPException;

public class ForbiddenException extends HTTPException {

	private static final long serialVersionUID = 1L;

	public ForbiddenException() {
		super(SC_FORBIDDEN);
	}
}
