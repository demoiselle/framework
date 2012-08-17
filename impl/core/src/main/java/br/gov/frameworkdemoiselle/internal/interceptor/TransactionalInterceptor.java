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
package br.gov.frameworkdemoiselle.internal.interceptor;

import java.io.Serializable;

import javax.enterprise.context.ContextNotActiveException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.exception.ApplicationException;
import br.gov.frameworkdemoiselle.internal.implementation.TransactionInfo;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.transaction.Transaction;
import br.gov.frameworkdemoiselle.transaction.TransactionContext;
import br.gov.frameworkdemoiselle.transaction.Transactional;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@Interceptor
@Transactional
public class TransactionalInterceptor implements Serializable {

	private static final long serialVersionUID = 1L;

	private TransactionContext transactionContext;

	private TransactionInfo transactionInfo;

	private static ResourceBundle bundle;

	private static Logger logger;

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

		} catch (ContextNotActiveException cause) {
			instance = new TransactionInfo() {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isOwner() {
					return false;
				}
			};
		}

		return instance;
	}

	private TransactionInfo getTransactionInfo() {
		if (this.transactionInfo == null) {
			this.transactionInfo = newTransactionInfo();
		}

		return this.transactionInfo;
	}

	@AroundInvoke
	public Object manage(final InvocationContext ic) throws Exception {
		initiate(ic);

		Object result = null;
		try {
			getLogger().debug(getBundle().getString("transactional-execution", ic.getMethod().toGenericString()));
			result = ic.proceed();

		} catch (Exception cause) {
			handleException(cause);
			throw cause;

		} finally {
			complete(ic);
		}

		return result;
	}

	private void initiate(final InvocationContext ic) {
		Transaction transaction = getTransactionContext().getCurrentTransaction();
		TransactionInfo transactionInfo = getTransactionInfo();

		if (!transaction.isActive()) {
			transaction.begin();
			transactionInfo.markAsOwner();
			getLogger().info(getBundle().getString("begin-transaction"));
		}

		transactionInfo.incrementCounter();
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
				transaction.setRollbackOnly();
				getLogger().info(getBundle().getString("transaction-marked-rollback", cause.getMessage()));
			}
		}
	}

	private void complete(final InvocationContext ic) {
		Transaction transaction = getTransactionContext().getCurrentTransaction();
		TransactionInfo transactionInfo = getTransactionInfo();
		transactionInfo.decrementCounter();

		if (transactionInfo.getCounter() == 0 && transaction.isActive()) {

			if (transactionInfo.isOwner()) {
				if (transaction.isMarkedRollback()) {
					transaction.rollback();
					getLogger().info(getBundle().getString("transaction-rolledback"));
				} else {
					transaction.commit();
					getLogger().info(getBundle().getString("transaction-commited"));
				}
			}

		} else if (transactionInfo.getCounter() == 0 && !transaction.isActive()) {
			getLogger().info(getBundle().getString("transaction-already-finalized"));
		}
	}

	private static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = ResourceBundleProducer.create("demoiselle-core-bundle");
		}

		return bundle;
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = LoggerProducer.create(TransactionalInterceptor.class);
		}

		return logger;
	}
}
