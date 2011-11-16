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
package br.gov.frameworkdemoiselle.transaction;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.SystemException;

import br.gov.frameworkdemoiselle.internal.producer.EntityManagerProducer;

/**
 * @author SERPRO
 * @see JPATransaction
 */

public class JPATransactionTest {

	private JPATransaction tx;

	private EntityManager em;

	private EntityTransaction et;

	private Map<String, EntityManager> cache;

	private EntityManagerProducer producer;

	@Before
	public void setUp() {
		et = createMock(EntityTransaction.class);
		em = createMock(EntityManager.class);
		cache = new HashMap<String, EntityManager>();
		producer = EasyMock.createMock(EntityManagerProducer.class);

		tx = new JPATransaction();
		setInternalState(tx, EntityManagerProducer.class, producer);
	}

	@After
	public void tearDown() {
		tx = null;
		em = null;
		et = null;
		cache = null;
		producer = null;
	}

	@Test
	public void testBegin() throws SystemException {
		et.begin();
		expect(et.isActive()).andReturn(false);
		expect(em.getTransaction()).andReturn(et).anyTimes();
		cache.put("teste", em);
		expect(producer.getCache()).andReturn(cache);
		replay(producer);
		replayAll();
		replay(em);
		replay(et);

		tx.begin();
		verifyAll();
	}

	@Test
	public void testCommit() throws Exception {
		et.commit();
		expect(et.isActive()).andReturn(true);
		expect(em.getTransaction()).andReturn(et).anyTimes();
		cache.put("teste", em);
		expect(producer.getCache()).andReturn(cache);
		replay(producer);
		replayAll();
		replay(em);
		replay(et);

		tx.commit();
		verifyAll();
	}

	@Test
	public void testRollback() throws Exception {
		et.rollback();
		expect(et.isActive()).andReturn(true);
		expect(em.getTransaction()).andReturn(et).anyTimes();
		em.clear();
		cache.put("teste", em);
		expect(producer.getCache()).andReturn(cache);
		replay(producer);
		replayAll();
		replay(em);
		replay(et);

		tx.rollback();
		verifyAll();
	}

	@Test
	public void testSetRollbackOnly() throws Exception {
		et.setRollbackOnly();
		expect(et.isActive()).andReturn(true);
		expect(em.getTransaction()).andReturn(et).anyTimes();
		cache.put("teste", em);
		expect(producer.getCache()).andReturn(cache);
		replay(producer);
		replayAll();
		replay(em);
		replay(et);

		tx.setRollbackOnly();
		verifyAll();
	}

	@Test
	public void testIsActiveTrue() throws Exception {
		expect(et.isActive()).andReturn(true).anyTimes();
		expect(em.getTransaction()).andReturn(et).anyTimes();
		cache.put("teste", em);
		expect(producer.getCache()).andReturn(cache);
		replay(producer);
		replayAll();
		replay(em);
		replay(et);

		assertEquals(true, tx.isActive());
		verifyAll();
	}

	@Test
	public void testIsActiveFalse() throws Exception {
		expect(et.isActive()).andReturn(false).anyTimes();
		expect(em.getTransaction()).andReturn(et).anyTimes();
		cache.put("teste", em);
		expect(producer.getCache()).andReturn(cache);
		replay(producer);
		replayAll();
		replay(em);
		replay(et);

		assertEquals(false, tx.isActive());
		verifyAll();
	}

	@Test
	public void testIsMarkedRollbackTrue() throws Exception {
		expect(et.isActive()).andReturn(true);
		expect(et.getRollbackOnly()).andReturn(true).anyTimes();
		expect(em.getTransaction()).andReturn(et).anyTimes();
		cache.put("teste", em);
		expect(producer.getCache()).andReturn(cache);
		replay(producer);
		replayAll();
		replay(em);
		replay(et);

		assertEquals(true, tx.isMarkedRollback());
		verifyAll();
	}

	@Test
	public void testIsMarkedRollbackFalse() throws Exception {
		expect(et.isActive()).andReturn(true);
		expect(et.getRollbackOnly()).andReturn(false).anyTimes();
		expect(em.getTransaction()).andReturn(et).anyTimes();
		cache.put("teste", em);
		expect(producer.getCache()).andReturn(cache);
		replay(producer);
		replayAll();
		replay(em);
		replay(et);

		assertEquals(false, tx.isMarkedRollback());
		verifyAll();
	}
}
