package br.gov.frameworkdemoiselle;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import javax.xml.ws.http.HTTPException;

public class BadRequestException extends HTTPException {

	private static final long serialVersionUID = 1L;

	public BadRequestException() {
		super(SC_BAD_REQUEST);
	}
}
