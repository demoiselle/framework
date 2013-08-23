package br.gov.frameworkdemoiselle.management;

import br.gov.frameworkdemoiselle.DemoiselleException;


public class ManagedInvokationException extends DemoiselleException {

	private static final long serialVersionUID = -1542365184737242152L;

	public ManagedInvokationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ManagedInvokationException(String message) {
		super(message);
	}

	public ManagedInvokationException(Throwable cause) {
		super(cause);
	}


}
