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

import static br.gov.frameworkdemoiselle.annotation.Priority.L2_PRIORITY;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.internal.producer.EntityManagerProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Represents the strategy destinated to manage JPA transactions.
 * 
 * @author SERPRO
 * @see Transaction
 */
@Priority(L2_PRIORITY)
public class JPATransaction implements Transaction {

	private static final long serialVersionUID = 1L;

	private EntityManagerProducer producer;
	
	private ResourceBundle bundle;

	private EntityManagerProducer getProducer() {
		if (producer == null) {
			producer = Beans.getReference(EntityManagerProducer.class);
		}

		return producer;
	}
	
	private ResourceBundle getBundle() {
		if (bundle==null) {
			bundle = Beans.getReference(ResourceBundle.class , new NameQualifier("demoiselle-jpa-bundle"));
		}
		
		return bundle;
	}

	public Collection<EntityManager> getDelegate() {
		return getProducer().getCache().values();
	}

	@Override
	public void begin() {
		EntityTransaction transaction;
		
		try {
			for (EntityManager entityManager : getDelegate()) {
				transaction = entityManager.getTransaction();
	
				if (!transaction.isActive()) {
					transaction.begin();
				}
			}
		}
		catch(Exception e) {
			/*
			Precisamos marcar para rollback todos os EntityManagers que conseguimos iniciar
			antes da exceção ser disparada.
			*/
			setRollbackOnly();
			
			throw new TransactionException(e);
		}
	}

	@Override
	public void commit() {
		EntityTransaction transaction;
		
		int commitedEntityManagers = 0;
		try {
			for (EntityManager entityManager : getDelegate()) {
				transaction = entityManager.getTransaction();
	
				if (transaction.isActive()) {
					transaction.commit();
					commitedEntityManagers++;
				}
			}
		}
		catch(Exception e) {
			/*
			Precisamos marcar para rollback todos os EntityManagers que conseguimos iniciar
			antes da exceção ser disparada.
			*/
			setRollbackOnly();
			
			/*
			Esse erro pode ser bastante problemático, pois EntityManagers já encerrados com commit
			não podem ser revertidos. Por isso anexamos uma mensagem recomendando ao usuário que considere o uso de JTA em sua aplicação.
			*/
			if (commitedEntityManagers>0) {
				throw new TransactionException(getBundle().getString("partial-rollback-problem"),e);
			}
			else {
				throw new TransactionException(e);
			}
		}
	}

	@Override
	public void rollback() {
		EntityTransaction transaction;
		
		try {
			for (EntityManager entityManager : getDelegate()) {
				transaction = entityManager.getTransaction();
	
				if (transaction.isActive()) {
					transaction.rollback();
				}
			}
		}
		catch(Exception e) {
			throw new TransactionException(e);
		}
	}

	@Override
	public void setRollbackOnly() {
		EntityTransaction transaction;
		
		try {
			for (EntityManager entityManager : getDelegate()) {
				transaction = entityManager.getTransaction();
	
				if (transaction.isActive()) {
					transaction.setRollbackOnly();
				}
			}
		}
		catch(Exception e) {
			throw new TransactionException(e);
		}
	}

	@Override
	public boolean isActive() {
		boolean active = false;

		EntityTransaction transaction;
		try {
			for (EntityManager entityManager : getDelegate()) {
				transaction = entityManager.getTransaction();
	
				if (transaction.isActive()) {
					active = true;
					break;
				}
			}
		}
		catch (Exception e) {
			throw new TransactionException(e);
		}

		return active;
	}

	@Override
	public boolean isMarkedRollback() {
		boolean rollbackOnly = false;

		EntityTransaction transaction;
		try {
			for (EntityManager entityManager : getDelegate()) {
				transaction = entityManager.getTransaction();
	
				if (transaction.isActive() && transaction.getRollbackOnly()) {
					rollbackOnly = true;
					break;
				}
			}
		}
		catch(Exception e) {
			throw new TransactionException(e);
		}

		return rollbackOnly;
	}
}
