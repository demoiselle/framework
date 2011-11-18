///*
// * Demoiselle Framework
// * Copyright (C) 2010 SERPRO
// * ----------------------------------------------------------------------------
// * This file is part of Demoiselle Framework.
// * 
// * Demoiselle Framework is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public License version 3
// * as published by the Free Software Foundation.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public License version 3
// * along with this program; if not,  see <http://www.gnu.org/licenses/>
// * or write to the Free Software Foundation, Inc., 51 Franklin Street,
// * Fifth Floor, Boston, MA  02110-1301, USA.
// * ----------------------------------------------------------------------------
// * Este arquivo é parte do Framework Demoiselle.
// * 
// * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
// * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
// * do Software Livre (FSF).
// * 
// * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
// * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
// * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
// * para maiores detalhes.
// * 
// * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
// * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
// * ou escreva para a Fundação do Software Livre (FSF) Inc.,
// * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
// */
//package br.gov.frameworkdemoiselle.transaction;
//
//import static junit.framework.Assert.assertEquals;
//import static junit.framework.Assert.assertFalse;
//import static junit.framework.Assert.assertTrue;
//import static junit.framework.Assert.fail;
//import static org.easymock.EasyMock.expect;
//import static org.easymock.EasyMock.replay;
//import static org.easymock.EasyMock.verify;
//import static org.powermock.reflect.Whitebox.setInternalState;
//
//import javax.naming.Context;
//import javax.naming.NamingException;
//import javax.transaction.HeuristicMixedException;
//import javax.transaction.HeuristicRollbackException;
//import javax.transaction.NotSupportedException;
//import javax.transaction.RollbackException;
//import javax.transaction.Status;
//import javax.transaction.SystemException;
//import javax.transaction.UserTransaction;
//
//import org.easymock.EasyMock;
//import org.junit.Before;
//import org.junit.Test;
//import org.powermock.api.easymock.PowerMock;
//
//import br.gov.frameworkdemoiselle.exception.DemoiselleException;
//import br.gov.frameworkdemoiselle.util.ResourceBundle;
//
//public class JTATransactionTest {
//
//	private UserTransaction userTransaction;
//	private JTATransaction jtaTransaction;
//	private Context context;
//	private ResourceBundle bundle;
//	
//	@Before
//	public void setUp() throws Exception {
//		this.jtaTransaction = new JTATransaction();
//		
//		this.userTransaction = EasyMock.createMock(UserTransaction.class);
//		
//		this.context = EasyMock.createMock(Context.class);
//		expect(this.context.lookup("UserTransaction")).andReturn(this.userTransaction);
//		replay(this.context);
//		setInternalState(this.jtaTransaction, "context", this.context);
//	}
//
//	@Test
//	public void testNamingException() throws Exception{
//		
//			this.context = EasyMock.createMock(Context.class);
//			expect(this.context.lookup("UserTransaction")).andThrow(new NamingException());
//			replay(this.context);
//			setInternalState(this.jtaTransaction, "context", this.context);
//
//			this.bundle = PowerMock.createMock(ResourceBundle.class);
//			EasyMock.expect(this.bundle.getString("user-transaction-lookup-fail","UserTransaction")).andReturn("teste");
//			PowerMock.replay(this.bundle);
//			setInternalState(this.jtaTransaction, "bundle", this.bundle);
//		
//		try {	
//			this.jtaTransaction.isMarkedRollback();
//			fail();
//		}catch(DemoiselleException cause) {
//			assertTrue(true);
//		}
//	}
//	
//	@Test
//	public void testIsMarkedRollback() throws SystemException {
//		expect(this.userTransaction.getStatus()).andReturn(Status.STATUS_MARKED_ROLLBACK);
//		replay(this.userTransaction);
//		assertTrue(this.jtaTransaction.isMarkedRollback());
//		verify(this.userTransaction);
//	}
//	
//	@Test
//	public void testIsNotMarkedRollback() throws SystemException {
//		expect(this.userTransaction.getStatus()).andReturn(2);
//		replay(this.userTransaction);
//		assertFalse(this.jtaTransaction.isMarkedRollback());
//		verify(this.userTransaction);
//	}
//	
//	@Test
//	public void testIsAtive() throws SystemException {
//		expect(this.userTransaction.getStatus()).andReturn(Status.STATUS_ACTIVE);
//		replay(this.userTransaction);
//		assertTrue(this.jtaTransaction.isActive());
//		verify(this.userTransaction);
//	}
//	
//	@Test
//	public void testIsNotAtiveButMarkedRollback() throws SystemException {
//		expect(this.userTransaction.getStatus()).andReturn(Status.STATUS_MARKED_ROLLBACK).times(2);
//		replay(this.userTransaction);
//		assertTrue(this.jtaTransaction.isActive());
//		verify(this.userTransaction);
//	}
//	
//	@Test
//	public void testBegin() throws NotSupportedException, SystemException {
//		this.userTransaction.begin();
//		replay(this.userTransaction);
//		this.jtaTransaction.begin();
//		verify(this.userTransaction);
//	}
//	
//	@Test
//	public void testCommit() throws SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
//		this.userTransaction.commit();
//		replay(this.userTransaction);
//		this.jtaTransaction.commit();
//		verify(this.userTransaction);
//	}
//	
//	@Test
//	public void testgetStatus() throws SystemException{
//		expect(this.userTransaction.getStatus()).andReturn(Status.STATUS_MARKED_ROLLBACK);
//		replay(this.userTransaction);
//		assertEquals(Status.STATUS_MARKED_ROLLBACK,this.jtaTransaction.getStatus());
//		verify(this.userTransaction);
//	}
//
//	@Test
//	public void testRollback() throws SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
//		this.userTransaction.rollback();
//		replay(this.userTransaction);
//		this.jtaTransaction.rollback();
//		verify(this.userTransaction);
//	}
//	
//	@Test
//	public void testSetRollbackOnly() throws SystemException{
//		this.userTransaction.setRollbackOnly();
//		replay(this.userTransaction);
//		this.jtaTransaction.setRollbackOnly();
//		verify(this.userTransaction);
//	}
//	
//	@Test
//	public void testSetTransactionTimeout() throws SystemException{
//		this.userTransaction.setTransactionTimeout(0);
//		replay(this.userTransaction);
//		this.jtaTransaction.setTransactionTimeout(0);
//		verify(this.userTransaction);
//	}
//}
