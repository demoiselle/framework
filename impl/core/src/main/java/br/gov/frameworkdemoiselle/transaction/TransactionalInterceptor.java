/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.transaction;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import br.gov.frameworkdemoiselle.exception.ApplicationException;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@Interceptor
@Transactional
public class TransactionalInterceptor implements Serializable {

	private static final long serialVersionUID = 1L;

	private TransactionContext transactionContext;

	private TransactionInfo transactionInfo;

	private transient ResourceBundle bundle;

	private transient Logger logger;

	private TransactionContext getTransactionContext() {
		if (this.transactionContext == null) {
			this.transactionContext = Beans.getReference(TransactionContext.class);
		}

		return this.transactionContext;
	}

	private TransactionInfo newTransactionInfo() {
		TransactionInfo instance;

		try {
			instance = Beans.getReference(TransactionInfo.class);
			instance.getCounter();

		} catch (ContextNotActiveException cause) {
			instance = new VoidTransactionInfo();
		}

		return instance;
	}

	private TransactionInfo getTransactionInfo() {
		if (this.transactionInfo == null) {
			this.transactionInfo = newTransactionInfo();
		}

		return this.transactionInfo;
	}

	/**
	 * <p>
	 * Manages methods annotated with {@link Transactional}. If there's no active transaction the moment a
	 * {@link Transactional} method is called, this interceptor will activate one. If a {@link Transactional} method
	 * that activated a transaction returns, this method will commit the active transaction. If the
	 * {@link Transactional} method throws an exception caught outside of the method (or uncaught), this interceptor
	 * will mark the transaction for rollback.
	 * </p>
	 * <p>
	 * This method is not intended to be called directly, instead the CDI provider will call this method when a
	 * {@link Transactional} method is called.
	 * </p>
	 */
	@AroundInvoke
	public Object manage(final InvocationContext ic) throws Exception {
		Object result = null;

		try {
			initiate();
			getLogger().finer(getBundle().getString("transactional-execution", ic.getMethod().toGenericString()));
			result = ic.proceed();

		} catch (Exception cause) {
			handleException(cause);
			throw cause;

		} finally {
			complete();
		}

		return result;
	}

	private void initiate() {
		Transaction transaction = getTransactionContext().getCurrentTransaction();

		if (!transaction.isActive()) {
			transaction.begin();
			getTransactionInfo().markAsOwner();
			getLogger().fine(getBundle().getString("begin-transaction"));
			fireAfterTransactionBegin();
		}

		getTransactionInfo().incrementCounter();
	}

	private void handleException(final Exception cause) {
		Transaction transaction = getTransactionContext().getCurrentTransaction();

		if (!transaction.isMarkedRollback()) {
			boolean rollback = false;
			ApplicationException annotation = cause.getClass().getAnnotation(ApplicationException.class);

			if (annotation == null || annotation.rollback()) {
				rollback = true;
			}

			if (rollback) {
				setRollbackOnly(transaction, cause);
			}
		}
	}

	private void setRollbackOnly(Transaction transaction, Exception cause) {
		transaction.setRollbackOnly();
		getLogger().fine(getBundle().getString("transaction-marked-rollback", cause.getMessage()));
	}

	private void complete() throws Exception {
		Transaction transaction = getTransactionContext().getCurrentTransaction();
		getTransactionInfo().decrementCounter();

		if (getTransactionInfo().getCounter() == 0 && transaction.isActive()) {
			if (getTransactionInfo().isOwner()) {
				complete(transaction);
			}

		} else if (getTransactionInfo().getCounter() == 0 && !transaction.isActive()) {
			getLogger().fine(getBundle().getString("transaction-already-finalized"));
		}
	}

	private void complete(Transaction transaction) throws Exception {
		try {
			fireBeforeTransactionComplete(transaction.isMarkedRollback());

		} catch (Exception cause) {
			setRollbackOnly(transaction, cause);
			throw cause;

		} finally {
			if (transaction.isMarkedRollback()) {
				transaction.rollback();
				getTransactionInfo().clear();
				getLogger().fine(getBundle().getString("transaction-rolledback"));

			} else {
				transaction.commit();
				getTransactionInfo().clear();
				getLogger().fine(getBundle().getString("transaction-commited"));
			}
		}
	}

	private void fireAfterTransactionBegin() {
		Beans.getBeanManager().fireEvent(new AfterTransactionBegin() {

			private static final long serialVersionUID = 1L;
		});
	}

	private void fireBeforeTransactionComplete(final boolean markedRollback) {
		Beans.getBeanManager().fireEvent(new BeforeTransactionComplete() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isMarkedRollback() {
				return markedRollback;
			}
		});
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle"));
		}

		return bundle;
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = Beans.getReference(Logger.class, new NameQualifier("br.gov.frameworkdemoiselle.transaction"));
		}

		return logger;
	}

	@RequestScoped
	public static class TransactionInfo implements Serializable {

		private static final long serialVersionUID = 1L;

		private int counter = 0;

		private boolean owner;

		public TransactionInfo() {
			clear();
		}

		public void clear() {
			this.owner = false;
			this.counter = 0;
		}

		public int getCounter() {
			return counter;
		}

		public void incrementCounter() {
			this.counter++;
		}

		public void decrementCounter() {
			this.counter--;
		}

		public void markAsOwner() {
			this.owner = true;
		}

		public boolean isOwner() {
			return owner;
		}
	}

	@Any
	static class VoidTransactionInfo extends TransactionInfo {

		private static final long serialVersionUID = 1L;

		@Override
		public boolean isOwner() {
			return false;
		}
	}
}
