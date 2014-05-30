package br.gov.frameworkdemoiselle;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import javax.xml.ws.http.HTTPException;

public class NotFoundException extends HTTPException {

	private static final long serialVersionUID = 1L;

	public NotFoundException() {
		super(SC_NOT_FOUND);
	}
}
