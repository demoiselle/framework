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

public class TransactionManager {

	private static boolean transactionMarkedRollback;

	private static boolean transactionActive;

	private static boolean transactionPassedInIsActiveMethod;

	private static boolean transactionPassedInIsMarkedRollbackMethod;

	private static boolean transactionPassedInBeginMethod;

	private static boolean transactionPassedInCommitMethod;

	private static boolean transactionPassedInRollbackMethod;

	private static boolean transactionPassedInSetRollbackOnlyMethod;

	public void clean() {
		setTransactionMarkedRollback(false);
		setTransactionActive(false);
		setTransactionPassedInIsActiveMethod(false);
		setTransactionPassedInIsMarkedRollbackMethod(false);
		setTransactionPassedInBeginMethod(false);
		setTransactionPassedInCommitMethod(false);
		setTransactionPassedInRollbackMethod(false);
		setTransactionPassedInSetRollbackOnlyMethod(false);
	}

	public boolean isTransactionMarkedRollback() {
		return transactionMarkedRollback;
	}

	public boolean isTransactionActive() {
		return transactionActive;
	}

	public boolean isTransactionPassedInIsActiveMethod() {
		return transactionPassedInIsActiveMethod;
	}

	public boolean isTransactionPassedInIsMarkedRollbackMethod() {
		return transactionPassedInIsMarkedRollbackMethod;
	}

	public boolean isTransactionPassedInBeginMethod() {
		return transactionPassedInBeginMethod;
	}

	public boolean isTransactionPassedInCommitMethod() {
		return transactionPassedInCommitMethod;
	}

	public boolean isTransactionPassedInRollbackMethod() {
		return transactionPassedInRollbackMethod;
	}

	public boolean isTransactionPassedInSetRollbackOnlyMethod() {
		return transactionPassedInSetRollbackOnlyMethod;
	}

	public static void setTransactionMarkedRollback(final boolean markedRollback) {
		transactionMarkedRollback = markedRollback;
	}

	public static void setTransactionActive(final boolean active) {
		transactionActive = active;
	}

	public static void setTransactionPassedInIsActiveMethod(final boolean passedInIsActiveMethod) {
		transactionPassedInIsActiveMethod = passedInIsActiveMethod;
	}

	public static void setTransactionPassedInIsMarkedRollbackMethod(final boolean passedInIsMarkedRollbackMethod) {
		transactionPassedInIsMarkedRollbackMethod = passedInIsMarkedRollbackMethod;
	}

	public static void setTransactionPassedInBeginMethod(final boolean passedInBeginMethod) {
		transactionPassedInBeginMethod = passedInBeginMethod;
	}

	public static void setTransactionPassedInCommitMethod(final boolean passedInCommitMethod) {
		transactionPassedInCommitMethod = passedInCommitMethod;
	}

	public static void setTransactionPassedInRollbackMethod(final boolean passedInRollbackMethod) {
		transactionPassedInRollbackMethod = passedInRollbackMethod;
	}

	public static void setTransactionPassedInSetRollbackOnlyMethod(final boolean passedInSetRollbackOnlyMethod) {
		transactionPassedInSetRollbackOnlyMethod = passedInSetRollbackOnlyMethod;
	}
}
