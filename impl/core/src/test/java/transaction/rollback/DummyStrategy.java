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
package transaction.rollback;

import br.gov.frameworkdemoiselle.transaction.Transaction;

public class DummyStrategy implements Transaction {

	private static final long serialVersionUID = 1L;
	
	private boolean markedRollback = false;

	private boolean active = false;

	@Override
	public boolean isActive() {
		TransactionManager.setTransactionPassedInIsActiveMethod(true);
		return active;
	}

	@Override
	public boolean isMarkedRollback() {
		TransactionManager.setTransactionPassedInIsMarkedRollbackMethod(true);
		return markedRollback;
	}

	@Override
	public void begin() {
		TransactionManager.setTransactionPassedInBeginMethod(true);
		active = true;
		TransactionManager.setTransactionActive(true);
	}

	@Override
	public void commit() {
		TransactionManager.setTransactionPassedInCommitMethod(true);
		active = false;
		TransactionManager.setTransactionActive(false);
	}

	@Override
	public void rollback() {
		TransactionManager.setTransactionPassedInRollbackMethod(true);
		active = false;
		TransactionManager.setTransactionActive(false);
	}

	@Override
	public void setRollbackOnly() {
		TransactionManager.setTransactionPassedInSetRollbackOnlyMethod(true);
		markedRollback = true;
		TransactionManager.setTransactionMarkedRollback(true);
	}
}
