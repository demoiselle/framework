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
package br.gov.frameworkdemoiselle.internal.proxy;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.internal.producer.EntityManagerProducer;
import br.gov.frameworkdemoiselle.internal.producer.FakeEntityManager;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class })
public class EntityManagerProxyTest {

	private EntityManagerProxy entityManagerProxy;

	private EntityManager entityManager;

	private EntityManagerProducer entityManagerContext;

	@Before
	public void setUp() throws Exception {
		mockStatic(Beans.class);
		this.entityManager = EasyMock.createMock(EntityManager.class);
		this.entityManagerContext = EasyMock.createMock(EntityManagerProducer.class);

		expect(Beans.getReference(EntityManagerProducer.class)).andReturn(this.entityManagerContext).anyTimes();
		expect(this.entityManagerContext.getEntityManager("teste")).andReturn(this.entityManager).anyTimes();
		replay(this.entityManagerContext);
		replayAll();

		this.entityManagerProxy = new EntityManagerProxy("teste");

	}

	@Test
	public void testJoinTransactionIfNecessaryException() {
		this.entityManager.persist("teste");
		expect(this.entityManager.getTransaction()).andThrow(new IllegalStateException()).anyTimes();
		this.entityManager.joinTransaction();
		replay(this.entityManager);
		
		this.entityManagerProxy.persist("teste");
		verify(this.entityManager);
	}
	
	@Test
	public void testPersist() {
		this.entityManager.persist("teste");
		expect(this.entityManager.getTransaction()).andReturn(null);
		replay(this.entityManager);
		this.entityManagerProxy.persist("teste");
		verify(this.entityManager);
	}

	@Test
	public void testRemove() {
		this.entityManager.remove("teste");
		expect(this.entityManager.getTransaction()).andReturn(null);
		replay(this.entityManager);
		this.entityManagerProxy.remove("teste");
		verify(this.entityManager);
	}

	@Test
	public void testFlush() {
		this.entityManager.flush();
		replay(this.entityManager);
		this.entityManagerProxy.flush();
		verify(this.entityManager);
	}

	@Test
	public void testSetFlushMode() {
		FlushModeType flushModeType = null;
		this.entityManager.setFlushMode(flushModeType);
		replay(this.entityManager);
		this.entityManagerProxy.setFlushMode(flushModeType);
		verify(this.entityManager);
	}

	@Test
	public void testLockWithParamsStringAndLockModeType() {
		LockModeType lockMode = null;
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		this.entityManager.lock("teste", lockMode);
		replay(this.entityManager);
		this.entityManagerProxy.lock("teste", lockMode);
		verify(this.entityManager);
	}

	@Test
	public void testLockWithParamsStringLockModeTypeAndMap() {
		LockModeType lockMode = null;
		Map<String, Object> map = null;
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		this.entityManager.lock("teste", lockMode, map);
		replay(this.entityManager);
		this.entityManagerProxy.lock("teste", lockMode, map);
		verify(this.entityManager);
	}

	@Test
	public void testRefresh() {
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		this.entityManager.refresh("teste");
		replay(this.entityManager);
		this.entityManagerProxy.refresh("teste");
		verify(this.entityManager);
	}

	@Test
	public void testRefreshWithParamsStringAndMap() {
		Map<String, Object> map = null;
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		this.entityManager.refresh("teste", map);
		replay(this.entityManager);
		this.entityManagerProxy.refresh("teste", map);
		verify(this.entityManager);
	}

	@Test
	public void testRefreshWithParamsStringAndLockModeType() {
		LockModeType lockMode = null;
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		this.entityManager.refresh("teste", lockMode);
		replay(this.entityManager);
		this.entityManagerProxy.refresh("teste", lockMode);
		verify(this.entityManager);
	}

	@Test
	public void testRefreshWithParamsStringLockModeTypeAndMap() {
		LockModeType lockMode = null;
		Map<String, Object> map = null;
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		this.entityManager.refresh("teste", lockMode, map);
		replay(this.entityManager);
		this.entityManagerProxy.refresh("teste", lockMode, map);
		verify(this.entityManager);
	}

	@Test
	public void testClear() {
		this.entityManager.clear();
		replay(this.entityManager);
		this.entityManagerProxy.clear();
		verify(this.entityManager);
	}

	@Test
	public void testDetach() {
		this.entityManager.detach("teste");
		replay(this.entityManager);
		this.entityManagerProxy.detach("teste");
		verify(this.entityManager);
	}

	@Test
	public void testSetProperty() {
		this.entityManager.setProperty("teste", "teste");
		replay(this.entityManager);
		this.entityManagerProxy.setProperty("teste", "teste");
		verify(this.entityManager);
	}

	@Test
	public void testJoinTransaction() {
		this.entityManager.joinTransaction();
		replay(this.entityManager);
		this.entityManagerProxy.joinTransaction();
		verify(this.entityManager);
	}

	@Test
	public void testClose() {
		this.entityManager.close();
		replay(this.entityManager);
		this.entityManagerProxy.close();
		verify(this.entityManager);
	}

	@Test
	public void testMerge() {
		expect(this.entityManager.merge("teste")).andReturn("xxx");
		expect(this.entityManager.getTransaction()).andReturn(null);
		replay(this.entityManager);
		assertEquals("xxx", this.entityManagerProxy.merge("teste"));
		verify(this.entityManager);
	}

	@Test
	public void testFindWithParamsClassAndObject() {
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		expect(this.entityManager.find(String.class, "teste")).andReturn("retorno");
		replay(this.entityManager);
		assertEquals("retorno", this.entityManagerProxy.find(String.class, "teste"));
		verify(this.entityManager);
	}

	@Test
	public void testFindWithParamsClassObjectAndMap() {
		Map<String, Object> map = null;
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		expect(this.entityManager.find(String.class, "teste", map)).andReturn("retorno");
		replay(this.entityManager);
		assertEquals("retorno", this.entityManagerProxy.find(String.class, "teste", map));
		verify(this.entityManager);
	}

	@Test
	public void testFindWithParamsClassObjectAndLockModeType() {
		LockModeType lock = null;
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		expect(this.entityManager.find(String.class, "teste", lock)).andReturn("retorno");
		replay(this.entityManager);
		assertEquals("retorno", this.entityManagerProxy.find(String.class, "teste", lock));
		verify(this.entityManager);
	}

	@Test
	public void testFindWithParamsClassObjectLockModeTypeAndMap() {
		Map<String, Object> map = null;
		LockModeType lock = null;
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		expect(this.entityManager.find(String.class, "teste", lock, map)).andReturn("retorno");
		replay(this.entityManager);
		assertEquals("retorno", this.entityManagerProxy.find(String.class, "teste", lock, map));
		verify(this.entityManager);
	}

	@Test
	public void testGetReference() {
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		expect(this.entityManager.getReference(String.class, "teste")).andReturn("retorno");
		replay(this.entityManager);
		assertEquals("retorno", this.entityManagerProxy.getReference(String.class, "teste"));
		verify(this.entityManager);
	}

	@Test
	public void testGetFlushMode() {
		FlushModeType flushModeType = null;
		expect(this.entityManager.getFlushMode()).andReturn(flushModeType);
		replay(this.entityManager);
		assertEquals(flushModeType, this.entityManagerProxy.getFlushMode());
		verify(this.entityManager);
	}

	@Test
	public void testContains() {
		expect(this.entityManager.contains("teste")).andReturn(true);
		replay(this.entityManager);
		assertTrue(this.entityManagerProxy.contains("teste"));
		verify(this.entityManager);
	}

	@Test
	public void testGetLockMode() {
		LockModeType lockModeType = null;
		expect(this.entityManager.getTransaction()).andReturn(null).anyTimes();
		expect(this.entityManager.getLockMode("teste")).andReturn(lockModeType);
		replay(this.entityManager);
		assertEquals(lockModeType, this.entityManagerProxy.getLockMode("teste"));
		verify(this.entityManager);
	}

	@Test
	public void testGetProperties() {
		Map<String, Object> map = null;
		expect(this.entityManager.getProperties()).andReturn(map);
		replay(this.entityManager);
		assertEquals(map, this.entityManagerProxy.getProperties());
		verify(this.entityManager);
	}

	@Test
	public void testCreateQuery() {
		Query query = null;
		expect(this.entityManager.createQuery("teste")).andReturn(query);
		replay(this.entityManager);
		assertEquals(QueryProxy.class, this.entityManagerProxy.createQuery("teste").getClass());
		verify(this.entityManager);
	}

	@Test
	public void testCreateQueryWithParamCriteria() {
		TypedQuery<Object> typedQuery = null;
		CriteriaQuery<Object> criteriaQuery = null;
		expect(this.entityManager.createQuery(criteriaQuery)).andReturn(typedQuery);
		replay(this.entityManager);
		assertEquals(TypedQueryProxy.class, this.entityManagerProxy.createQuery(criteriaQuery).getClass());
		verify(this.entityManager);
	}

	@Test
	public void testCreateQueryWithParamStringAndClass() {
		TypedQuery<String> typeQuery = null;
		expect(this.entityManager.createQuery("teste", String.class)).andReturn(typeQuery);
		replay(this.entityManager);
		assertEquals(TypedQueryProxy.class, this.entityManagerProxy.createQuery("teste", String.class).getClass());
		verify(this.entityManager);
	}

	@Test
	public void testCreateNamedQuery() {
		Query query = null;
		expect(this.entityManager.createNamedQuery("teste")).andReturn(query);
		replay(this.entityManager);
		assertEquals(QueryProxy.class, this.entityManagerProxy.createNamedQuery("teste").getClass());
		verify(this.entityManager);
	}

	@Test
	public void testCreateNamedQueryWithParamsStringAndClass() {
		TypedQuery<String> typedQuery = null;
		expect(this.entityManager.createNamedQuery("teste", String.class)).andReturn(typedQuery);
		replay(this.entityManager);
		assertEquals(typedQuery, this.entityManagerProxy.createNamedQuery("teste", String.class));
		verify(this.entityManager);
	}

	@Test
	public void testCreateNativeQuery() {
		Query query = null;
		expect(this.entityManager.createNativeQuery("teste")).andReturn(query);
		replay(this.entityManager);
		assertEquals(QueryProxy.class, this.entityManagerProxy.createNativeQuery("teste").getClass());
		verify(this.entityManager);
	}

	@Test
	public void testCreateNativeQueryWithParamsStringAndClass() {
		Query query = null;
		expect(this.entityManager.createNativeQuery("teste", String.class)).andReturn(query);
		replay(this.entityManager);
		assertEquals(QueryProxy.class, this.entityManagerProxy.createNativeQuery("teste", String.class).getClass());
		verify(this.entityManager);
	}

	@Test
	public void testCreateNativeQueryWithParamsStringAndString() {
		Query query = null;
		expect(this.entityManager.createNativeQuery("teste", "teste")).andReturn(query);
		replay(this.entityManager);
		assertEquals(QueryProxy.class, this.entityManagerProxy.createNativeQuery("teste", "teste").getClass());
		verify(this.entityManager);
	}

	@Test
	public void testUnwrap() {
		String query = null;
		expect(this.entityManager.unwrap(String.class)).andReturn(query);
		replay(this.entityManager);
		assertEquals(query, this.entityManagerProxy.unwrap(String.class));
		verify(this.entityManager);
	}

	@Test
	public void testGetDelegate() {
		Object obj = null;
		expect(this.entityManager.getDelegate()).andReturn(obj);
		replay(this.entityManager);
		assertEquals(obj, this.entityManagerProxy.getDelegate());
		verify(this.entityManager);
	}

	@Test
	public void testIsOpen() {
		expect(this.entityManager.isOpen()).andReturn(true);
		replay(this.entityManager);
		assertTrue(this.entityManagerProxy.isOpen());
		verify(this.entityManager);
	}

	@Test
	public void testGetTransaction() {
		EntityTransaction entityTransaction = null;
		expect(this.entityManager.getTransaction()).andReturn(entityTransaction);
		replay(this.entityManager);
		assertEquals(entityTransaction, this.entityManagerProxy.getTransaction());
		verify(this.entityManager);
	}

	@Test
	public void testGetEntityManagerFactory() {
		EntityManagerFactory entityManagerFactory = null;
		expect(this.entityManager.getEntityManagerFactory()).andReturn(entityManagerFactory);
		replay(this.entityManager);
		assertEquals(entityManagerFactory, this.entityManagerProxy.getEntityManagerFactory());
		verify(this.entityManager);
	}

	@Test
	public void testGetCriteriaBuilder() {
		CriteriaBuilder criteriaBuilder = null;
		expect(this.entityManager.getCriteriaBuilder()).andReturn(criteriaBuilder);
		replay(this.entityManager);
		assertEquals(criteriaBuilder, this.entityManagerProxy.getCriteriaBuilder());
		verify(this.entityManager);
	}

	@Test
	public void testGetMetamodel() {
		Metamodel metamodel = null;
		expect(this.entityManager.getMetamodel()).andReturn(metamodel);
		replay(this.entityManager);
		assertEquals(metamodel, this.entityManagerProxy.getMetamodel());
		verify(this.entityManager);
	}

	@Test
	public void testEquals() {
		Object obj = null;

		mockStatic(Beans.class);
		// Method "equals" can't be mocked...
		EntityManager em = new FakeEntityManager();
		((FakeEntityManager) em).setEquals(true);

		this.entityManagerContext = EasyMock.createMock(EntityManagerProducer.class);
		expect(this.entityManagerContext.getEntityManager("teste")).andReturn(em).anyTimes();

		expect(Beans.getReference(EntityManagerProducer.class)).andReturn(this.entityManagerContext).anyTimes();
		replay(this.entityManagerContext);
		replayAll();

		EntityManagerProxy emp = new EntityManagerProxy("teste");

		assertTrue(emp.equals(obj));
	}

	@Test
	public void testHashCode() {
		mockStatic(Beans.class);
		// Method "hashCode" can't be mocked...
		EntityManager em = new FakeEntityManager();
		((FakeEntityManager) em).setHashCode(1);

		this.entityManagerContext = EasyMock.createMock(EntityManagerProducer.class);
		expect(this.entityManagerContext.getEntityManager("teste")).andReturn(em).anyTimes();

		expect(Beans.getReference(EntityManagerProducer.class)).andReturn(this.entityManagerContext).anyTimes();
		replay(this.entityManagerContext);
		replayAll();

		EntityManagerProxy emp = new EntityManagerProxy("teste");

		assertEquals(1, emp.hashCode());
	}

	@Test
	public void testToString() {
		mockStatic(Beans.class);
		// Method "toString" can't be mocked...
		EntityManager em = new FakeEntityManager();
		((FakeEntityManager) em).setToString("testing");

		this.entityManagerContext = EasyMock.createMock(EntityManagerProducer.class);
		expect(this.entityManagerContext.getEntityManager("teste")).andReturn(em).anyTimes();

		expect(Beans.getReference(EntityManagerProducer.class)).andReturn(this.entityManagerContext).anyTimes();
		replay(this.entityManagerContext);
		replayAll();

		EntityManagerProxy emp = new EntityManagerProxy("teste");

		assertEquals("testing", emp.toString());
	}
}
