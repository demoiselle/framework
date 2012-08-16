package br.gov.frameworkdemoiselle.internal.implementation;

import javax.inject.Named;

import br.gov.frameworkdemoiselle.transaction.Transaction;
import br.gov.frameworkdemoiselle.transaction.TransactionContext;

/**
 * This is the default implementation of {@link TransactionContext} interface.
 * 
 * @author SERPRO
 */
@Named("transactionContext")
public class TrancationContextImpl implements TransactionContext {

	private static final long serialVersionUID = 1L;

	@Override
	public Transaction currentTransaction() {
		return StrategySelector.getReference("frameworkdemoiselle.transaction.class", Transaction.class, DefaultTransaction.class);
	}

}
