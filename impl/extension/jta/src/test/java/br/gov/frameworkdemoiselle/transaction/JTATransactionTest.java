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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verify;
import static org.powermock.reflect.Whitebox.setInternalState;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class })
public class JTATransactionTest {

	private UserTransaction userTransaction;

	private JTATransaction jtaTransaction;

	@Before
	public void setUp() {
		userTransaction = createMock(UserTransaction.class);
		jtaTransaction = new JTATransaction();

		setInternalState(jtaTransaction, UserTransaction.class, userTransaction);
	}

	@Test
	public void testGetDElegateWithUserTransactionNull() throws SystemException {
		mockStatic(Beans.class);
		userTransaction = null;
		setInternalState(jtaTransaction, UserTransaction.class, userTransaction);
		userTransaction = createMock(UserTransaction.class);
		expect(Beans.getReference(UserTransaction.class)).andReturn(userTransaction);
		replayAll();

		// Assert.assertEquals(userTransaction, jtaTransaction.getDelegate());
	}

	@Test
	public void testGetDElegateWithUserTransactionIsNotNull() throws SystemException {
		// Assert.assertEquals(userTransaction, jtaTransaction.getDelegate());
	}

	@Test
	public void testIsActiveWithStatusEqualsActive() throws SystemException {
		expect(userTransaction.getStatus()).andReturn(Integer.valueOf(0));
		replay(userTransaction);
		Assert.assertTrue(this.jtaTransaction.isActive());
	}

	@Test
	public void testIsActiveWithStatusEqualsMarkedRollback() throws SystemException {
		expect(userTransaction.getStatus()).andReturn(Integer.valueOf(1)).times(2);
		replay(userTransaction);
		Assert.assertTrue(this.jtaTransaction.isActive());
	}

	@Test
	public void testIsMarkedRollback() throws SystemException {
		expect(userTransaction.getStatus()).andReturn(Integer.valueOf(1));
		replay(userTransaction);
		Assert.assertTrue(this.jtaTransaction.isMarkedRollback());
	}

	@Test
	public void testBegin() throws SystemException, NotSupportedException {
		userTransaction.begin();
		replay(userTransaction);
		this.jtaTransaction.begin();
		verify();
	}

	@Test
	public void testCommit() throws SystemException, NotSupportedException, SecurityException, IllegalStateException,
			RollbackException, HeuristicMixedException, HeuristicRollbackException {
		userTransaction.commit();
		replay(userTransaction);
		this.jtaTransaction.commit();
		verify();
	}

	@Test
	public void testRollback() throws SystemException, NotSupportedException {
		userTransaction.rollback();
		replay(userTransaction);
		this.jtaTransaction.rollback();
		verify();
	}

	@Test
	public void testSetRollbackOnly() throws SystemException, NotSupportedException {
		userTransaction.setRollbackOnly();
		replay(userTransaction);
		this.jtaTransaction.setRollbackOnly();
		verify();
	}

	@Test
	public void testIsActiveThrowsSystemException() throws SystemException {
		expect(userTransaction.getStatus()).andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.isActive();
			Assert.fail();
		} catch (DemoiselleException cause) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testIsMarkedRollbackThrowsSystemException() throws SystemException {
		expect(userTransaction.getStatus()).andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.isMarkedRollback();
			Assert.fail();
		} catch (DemoiselleException cause) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testBeginThrowsException() throws SystemException, NotSupportedException {
		userTransaction.begin();
		expectLastCall().andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.begin();
			Assert.fail();
		} catch (DemoiselleException cause) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testCommitThrowsException() throws SystemException, SecurityException, IllegalStateException,
			RollbackException, HeuristicMixedException, HeuristicRollbackException {
		userTransaction.commit();
		expectLastCall().andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.commit();
			Assert.fail();
		} catch (DemoiselleException cause) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testRollbackThrowsSystemException() throws SystemException {
		userTransaction.rollback();
		expectLastCall().andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.rollback();
			Assert.fail();
		} catch (DemoiselleException cause) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testSetRollbackOnlyThrowsSystemException() throws SystemException {
		userTransaction.setRollbackOnly();
		expectLastCall().andThrow(new SystemException());
		replay(userTransaction);
		try {
			this.jtaTransaction.setRollbackOnly();
			Assert.fail();
		} catch (DemoiselleException cause) {
			Assert.assertTrue(true);
		}
	}

}
