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

import java.sql.Connection;
import java.util.Collection;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.internal.producer.ConnectionProducer;
import br.gov.frameworkdemoiselle.internal.producer.ConnectionProducer.Status;
import br.gov.frameworkdemoiselle.util.Beans;


/**
 * Represents the strategy destinated to manage JDBC transactions.
 * 
 * @author SERPRO
 * @see Transaction
 */
@Priority(L2_PRIORITY)
public class JDBCTransaction implements Transaction {

	private static final long serialVersionUID = 1L;

	private ConnectionProducer producer;

	//private Map<Connection, Status> cache = Collections.synchronizedMap(new HashMap<Connection, Status>());
	//private List<ConnectionProxy> cache = Collections.synchronizedList(new ArrayList<ConnectionProxy>());

	private ConnectionProducer getProducer() {
		if (producer == null) {
			producer = Beans.getReference(ConnectionProducer.class);

			/*for (Connection connection : producer.getCache().values()) {
				if (!ConnectionProxy.class.isInstance(connection)) {
					continue;
				}
				
				if (!cache.contains(connection)) {
					cache.add((ConnectionProxy)connection);
				}
			}*/
		}

		return producer;
	}

	private Collection<Connection> getDelegate() {
		return getProducer().getCache().values();
	}

	@Override
	public void begin() {
		Status status;
		for (Connection connection : getDelegate()) {
			status = getProducer().getStatus(connection);
			status.setActive(true);
		}
	}

	/**
	 * @throws DemoiselleException
	 */
	@Override
	public void commit() {
		Status status;
		
		for (Connection connection : getDelegate()) {
			try {
				connection.commit();
				status = getProducer().getStatus(connection);
				status.setActive(false);
			} catch (Exception cause) {
				throw new DemoiselleException(cause);
			}
		}
	}

	/**
	 * @throws DemoiselleException
	 */
	@Override
	public void rollback() {
		Status status;
		
		for (Connection connection : getDelegate()) {
			try {
				connection.rollback();
				status = getProducer().getStatus(connection);
				status.setActive(false);
			} catch (Exception cause) {
				throw new DemoiselleException(cause);
			}
		}
	}

	@Override
	public void setRollbackOnly() {
		Status status;
		for (Connection connection : getDelegate()) {
			status = getProducer().getStatus(connection);
			status.setRollbackOnly(true);
		}
	}

	@Override
	public boolean isActive() {
		Status status;
		boolean result = true;

		for (Connection connection : getDelegate()) {
			status = getProducer().getStatus(connection);
			result = result && status.isActive();
		}

		return result;
	}

	@Override
	public boolean isMarkedRollback() {
		Status status;
		boolean result = true;

		for (Connection connection : getDelegate()) {
			status = getProducer().getStatus(connection);
			result = result && status.isMarkedRollback();
		}

		return result;
	}

	/*private static class Status implements Serializable {

		private static final long serialVersionUID = 1L;

		private boolean active = false;

		private boolean markedRollback = false;

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public boolean isMarkedRollback() {
			return markedRollback;
		}

		public void setRollbackOnly(boolean markedRollback) {
			this.markedRollback = markedRollback;
		}
	}*/
}
