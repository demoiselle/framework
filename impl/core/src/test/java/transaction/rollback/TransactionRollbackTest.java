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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.context.RequestContext;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class TransactionRollbackTest {

	@Inject
	private TransactionManagerWithDefaultRollback managerWithDefaultRollback;

	@Inject
	private TransactionManagerWithRollback managerWithRollback;

	@Inject
	private TransactionManagerWithoutRollback managerWithoutRollback;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(TransactionRollbackTest.class);
		return deployment;
	}

	@Before
	public void activeContext() {
		RequestContext ctx = Beans.getReference(RequestContext.class);
		ctx.activate();
	}

	@After
	public void deactiveContext() {
		RequestContext ctx = Beans.getReference(RequestContext.class);
		ctx.deactivate();
	}

	@Test
	public void transactionWithDefaultRollback() {
		try {
			managerWithDefaultRollback.clean();
			managerWithDefaultRollback.insert();
			fail();
		} catch (TransactionExceptionWithDefaultRollback exception) {
			assertTrue(managerWithDefaultRollback.isTransactionPassedInIsActiveMethod());
			assertTrue(managerWithDefaultRollback.isTransactionPassedInBeginMethod());
			assertTrue(managerWithDefaultRollback.isTransactionMarkedRollback());
			assertTrue(managerWithDefaultRollback.isTransactionPassedInIsMarkedRollbackMethod());
			assertTrue(managerWithDefaultRollback.isTransactionPassedInSetRollbackOnlyMethod());
			assertTrue(managerWithDefaultRollback.isTransactionPassedInRollbackMethod());
			assertFalse(managerWithDefaultRollback.isTransactionPassedInCommitMethod());
			assertFalse(managerWithDefaultRollback.isTransactionActive());
		}
	}

	@Test
	public void transactionWithRollback() {
		try {
			managerWithRollback.clean();
			managerWithRollback.insert();
			fail();
		} catch (TransactionExceptionWithRollback exception) {
			assertTrue(managerWithRollback.isTransactionPassedInIsActiveMethod());
			assertTrue(managerWithRollback.isTransactionPassedInBeginMethod());
			assertTrue(managerWithRollback.isTransactionMarkedRollback());
			assertTrue(managerWithRollback.isTransactionPassedInIsMarkedRollbackMethod());
			assertTrue(managerWithRollback.isTransactionPassedInSetRollbackOnlyMethod());
			assertTrue(managerWithRollback.isTransactionPassedInRollbackMethod());
			assertFalse(managerWithRollback.isTransactionPassedInCommitMethod());
			assertFalse(managerWithRollback.isTransactionActive());
		}
	}

	@Test
	public void transactionWithoutRollback() {
		try {
			managerWithoutRollback.clean();
			managerWithoutRollback.insert();
			fail();
		} catch (TransactionExceptionWithoutRollback exception) {
			assertTrue(managerWithRollback.isTransactionPassedInIsActiveMethod());
			assertTrue(managerWithRollback.isTransactionPassedInBeginMethod());
			assertFalse(managerWithRollback.isTransactionMarkedRollback());
			assertTrue(managerWithRollback.isTransactionPassedInIsMarkedRollbackMethod());
			assertFalse(managerWithRollback.isTransactionPassedInSetRollbackOnlyMethod());
			assertFalse(managerWithRollback.isTransactionPassedInRollbackMethod());
			assertTrue(managerWithRollback.isTransactionPassedInCommitMethod());
			assertFalse(managerWithRollback.isTransactionActive());
		}
	}
}
