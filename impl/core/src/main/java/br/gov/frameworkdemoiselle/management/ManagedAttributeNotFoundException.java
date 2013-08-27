package br.gov.frameworkdemoiselle.management;

import br.gov.frameworkdemoiselle.DemoiselleException;

/**
 * 
 * Thrown when a management client tries to read or write a property, but the
 * management engine has no knowledge of an attribute with the given name. 
 * 
 * @author serpro
 *
 */
public class ManagedAttributeNotFoundException extends DemoiselleException {

	private static final long serialVersionUID = 2554101387574235418L;

	public ManagedAttributeNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ManagedAttributeNotFoundException(String message) {
		super(message);
	}

	public ManagedAttributeNotFoundException(Throwable cause) {
		super(cause);
	}

}
