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

/**
 * This interface improves UserTransaction by defining additional methods to it, allowing an application to explicitly
 * manage transaction boundaries.
 * 
 * @author SERPRO
 * @see UserTransaction
 */
public interface Transaction extends Serializable {

	/**
	 * Indicates whether the given transaction is still active.
	 * 
	 * @return a boolean
	 */
	boolean isActive();

	/**
	 * Indicates whether the given transaction is already marked to be rolled back.
	 * 
	 * @return a boolean
	 */
	boolean isMarkedRollback();

	/**
	 * Create a new transaction and associate it with the current thread.
	 */
	void begin();

	/**
	 * Complete the transaction associated with the current thread. When this method completes, the thread is no longer
	 * associated with a transaction.
	 */
	void commit();

	/**
	 * Roll back the transaction associated with the current thread. When this method completes, the thread is no longer
	 * associated with a transaction.
	 */
	void rollback();

	/**
	 * Modify the transaction associated with the current thread such that the only possible outcome of the transaction
	 * is to roll back the transaction.
	 */
	void setRollbackOnly();
}
