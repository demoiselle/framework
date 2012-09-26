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

import static br.gov.frameworkdemoiselle.internal.implementation.StrategySelector.EXTENSIONS_L1_PRIORITY;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.internal.producer.EntityManagerProducer;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * Represents the strategy destinated to manage JPA transactions.
 * 
 * @author SERPRO
 * @see Transaction
 */
@Priority(EXTENSIONS_L1_PRIORITY)
public class JPATransaction implements Transaction {

	private static final long serialVersionUID = 1L;

	private EntityManagerProducer producer;

	private EntityManagerProducer getProducer() {
		if (producer == null) {
			producer = Beans.getReference(EntityManagerProducer.class);
		}

		return producer;
	}

	public Collection<EntityManager> getDelegate() {
		return getProducer().getCache().values();
	}

	@Override
	public void begin() {
		EntityTransaction transaction;
		for (EntityManager entityManager : getDelegate()) {
			transaction = entityManager.getTransaction();

			if (!transaction.isActive()) {
				transaction.begin();
			}
		}
	}

	@Override
	public void commit() {
		EntityTransaction transaction;
		for (EntityManager entityManager : getDelegate()) {
			transaction = entityManager.getTransaction();

			if (transaction.isActive()) {
				transaction.commit();
			}
		}
	}

	@Override
	public void rollback() {
		EntityTransaction transaction;
		for (EntityManager entityManager : getDelegate()) {
			transaction = entityManager.getTransaction();

			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	@Override
	public void setRollbackOnly() {
		EntityTransaction transaction;
		for (EntityManager entityManager : getDelegate()) {
			transaction = entityManager.getTransaction();

			if (transaction.isActive()) {
				transaction.setRollbackOnly();
			}
		}
	}

	@Override
	public boolean isActive() {
		boolean active = false;

		for (EntityManager entityManager : getDelegate()) {
			if (entityManager.getTransaction().isActive()) {
				active = true;
				break;
			}
		}

		return active;
	}

	@Override
	public boolean isMarkedRollback() {
		boolean rollbackOnly = false;

		EntityTransaction transaction;
		for (EntityManager entityManager : getDelegate()) {
			transaction = entityManager.getTransaction();

			if (transaction.isActive() && transaction.getRollbackOnly()) {
				rollbackOnly = true;
				break;
			}
		}

		return rollbackOnly;
	}
}
