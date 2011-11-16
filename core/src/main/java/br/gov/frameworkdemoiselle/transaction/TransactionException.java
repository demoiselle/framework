package br.gov.frameworkdemoiselle.transaction;

import br.gov.frameworkdemoiselle.DemoiselleException;

public class TransactionException extends DemoiselleException {

	private static final long serialVersionUID = 1L;

	public TransactionException(Throwable cause) {
		super(cause);
	}
}
