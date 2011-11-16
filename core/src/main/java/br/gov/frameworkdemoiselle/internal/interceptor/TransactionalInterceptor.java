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

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.exception.ApplicationException;
import br.gov.frameworkdemoiselle.internal.implementation.TransactionInfo;
import br.gov.frameworkdemoiselle.transaction.Transaction;
import br.gov.frameworkdemoiselle.transaction.Transactional;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@Interceptor
@Transactional
public class TransactionalInterceptor implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Instance<Transaction> transaction;

	private final Logger logger;

	private final ResourceBundle bundle;

	private final Instance<TransactionInfo> transactionInfo;

	@Inject
	public TransactionalInterceptor(Instance<Transaction> transaction, Instance<TransactionInfo> transactionInfo,
			Logger logger, @Name("demoiselle-core-bundle") ResourceBundle bundle) {
		this.transaction = transaction;
		this.transactionInfo = transactionInfo;
		this.logger = logger;
		this.bundle = bundle;

	}

	@AroundInvoke
	public Object manage(final InvocationContext ic) throws Exception {
		initiate(ic);

		Object result = null;
		try {
			this.logger.debug(bundle.getString("transactional-execution", ic.getMethod().toGenericString()));
			transactionInfo.get().incrementCounter();

			result = ic.proceed();

		} catch (Exception cause) {
			handleException(cause);
			throw cause;

		} finally {
			transactionInfo.get().decrementCounter();
			complete(ic);
		}

		return result;
	}

	private void initiate(final InvocationContext ic) {
		Transaction tx = this.transaction.get();
		TransactionInfo ctx = this.transactionInfo.get();

		if (!tx.isActive()) {
			tx.begin();
			ctx.markAsOwner();
			this.logger.info(bundle.getString("begin-transaction"));
		}
	}

	private void handleException(final Exception cause) {
		Transaction tx = this.transaction.get();

		if (!tx.isMarkedRollback()) {
			boolean rollback = false;
			ApplicationException annotation = cause.getClass().getAnnotation(ApplicationException.class);

			if (annotation == null || annotation.rollback()) {
				rollback = true;
			}

			if (rollback) {
				tx.setRollbackOnly();
				this.logger.info(bundle.getString("transaction-marked-rollback", cause.getMessage()));
			}
		}
	}

	private void complete(final InvocationContext ic) {
		Transaction tx = this.transaction.get();
		TransactionInfo ctx = this.transactionInfo.get();

		if (ctx.getCounter() == 0 && tx.isActive()) {

			if (ctx.isOwner()) {
				if (tx.isMarkedRollback()) {
					tx.rollback();
					this.logger.info(bundle.getString("transaction-rolledback"));
				} else {
					tx.commit();
					this.logger.info(bundle.getString("transaction-commited"));
				}
			}

		} else if (ctx.getCounter() == 0 && !tx.isActive()) {
			this.logger.info(bundle.getString("transaction-already-finalized"));
		}
	}
}
