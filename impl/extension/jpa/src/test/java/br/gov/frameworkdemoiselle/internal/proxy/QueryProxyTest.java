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

import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.gov.frameworkdemoiselle.domain.Client;
import br.gov.frameworkdemoiselle.internal.producer.EntityManagerProducer;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * Test the proxied {@link Query} class, {@link QueryProxy}.
 * 
 * @author 81986912515
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class })
public class QueryProxyTest {

	private EntityManager manager;

	private EntityManagerProducer producer;

	private QueryProxy queryProxy;

	@Before
	public void setUp() {

		Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");

		configOverrides.put("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:.");
		configOverrides.put("javax.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
		configOverrides.put("javax.persistence.jdbc.user", "sa");
		configOverrides.put("javax.persistence.jdbc.password", "");

		configOverrides.put("hibernate.show_sql", "true");
		configOverrides.put("hibernate.hbm2ddl.auto", "create-drop");

		EntityManagerFactory factory = Persistence.createEntityManagerFactory("pu1", configOverrides);
		EntityManager delegate = factory.createEntityManager();

		Map<String, EntityManager> cache = Collections.synchronizedMap(new HashMap<String, EntityManager>());
		cache.put("pu1", delegate);

		producer = new EntityManagerProducer();
		Whitebox.setInternalState(producer, "cache", cache);

		PowerMock.mockStatic(Beans.class);
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).anyTimes();
		PowerMock.replayAll();

		manager = new EntityManagerProxy("pu1");
	}

	@Test
	public void testGetResultList() {
		Query queryDelegate = PowerMock.createMock(Query.class);
		List<String> result = new ArrayList<String>();
		result.add("x");
		EasyMock.expect(queryDelegate.getResultList()).andReturn(result).anyTimes();

		replay(queryDelegate);

		queryProxy = new QueryProxy(queryDelegate, (EntityManagerProxy) manager);
		queryProxy.getResultList();
		assertEquals(queryProxy.getResultList().size(), queryDelegate.getResultList().size());

		verifyAll();
	}

	@Test
	public void testSingleResult() {
		Query queryDelegate = PowerMock.createMock(Query.class);
		String result = "Resultado";
		EasyMock.expect(queryDelegate.getSingleResult()).andReturn(result).anyTimes();

		replay(queryDelegate);

		queryProxy = new QueryProxy(queryDelegate, (EntityManagerProxy) manager);
		assertEquals(queryProxy.getSingleResult(), queryDelegate.getSingleResult());

		verifyAll();
	}

	@Test
	public void testExecuteUpdate() {
		Query queryDelegate = PowerMock.createMock(Query.class);
		EasyMock.expect(queryDelegate.executeUpdate()).andReturn(1).anyTimes();

		replay(queryDelegate);

		queryProxy = new QueryProxy(queryDelegate, (EntityManagerProxy) manager);
		assertEquals(queryProxy.executeUpdate(), 1);

		verifyAll();
	}

	@Test
	public void testPagination() {
		Query queryDelegate = PowerMock.createMock(Query.class);
		expect(queryDelegate.getMaxResults()).andReturn(4).times(2);
		expect(queryDelegate.getFirstResult()).andReturn(2).times(2);
		expect(queryDelegate.setMaxResults(EasyMock.anyInt())).andReturn(queryDelegate);
		expect(queryDelegate.setFirstResult(EasyMock.anyInt())).andReturn(queryDelegate);
		replay(queryDelegate);

		queryProxy = new QueryProxy(queryDelegate, (EntityManagerProxy) manager);
		queryProxy.setMaxResults(4);
		queryProxy.setFirstResult(2);
		assertEquals(queryProxy.getMaxResults(), queryDelegate.getMaxResults());
		assertEquals(queryProxy.getFirstResult(), queryDelegate.getFirstResult());

		verifyAll();
	}

	@Test
	public void testHint() {
		Query queryDelegate = PowerMock.createMock(Query.class);
		Map<String, Object> map = new HashMap<String, Object>();
		Client client = new Client();
		map.put("1", client);
		expect(queryDelegate.getHints()).andReturn(map).times(2);
		expect(queryDelegate.setHint(EasyMock.anyObject(String.class), EasyMock.anyObject())).andReturn(queryDelegate);
		replay(queryDelegate);

		queryProxy = new QueryProxy(queryDelegate, (EntityManagerProxy) manager);
		queryProxy.setHint("1", client);
		assertEquals(queryProxy.getHints(), queryDelegate.getHints());

		verifyAll();
	}

	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	@Test
	public void testParameters() {
		Query queryDelegate = PowerMock.createMock(Query.class);
		Date dateValue = new Date();
		Calendar calendar = PowerMock.createMock(Calendar.class);
		Class<Date> type = Date.class;
		Parameter parameter = PowerMock.createMock(Parameter.class);

		expect(queryDelegate.setParameter(EasyMock.anyObject(String.class), EasyMock.anyObject(String.class)))
				.andReturn(queryDelegate);
		expect(queryDelegate.getParameterValue(EasyMock.anyObject(String.class))).andReturn("Named Parameter")
				.anyTimes();
		expect(queryDelegate.setParameter(EasyMock.anyInt(), EasyMock.anyObject(String.class)))
				.andReturn(queryDelegate);
		expect(queryDelegate.getParameterValue(EasyMock.anyInt())).andReturn("Client").anyTimes();
		expect(
				queryDelegate.setParameter(EasyMock.anyObject(String.class), EasyMock.anyObject(Date.class),
						EasyMock.anyObject(TemporalType.class))).andReturn(queryDelegate);
		expect(
				queryDelegate.setParameter(EasyMock.anyInt(), EasyMock.anyObject(Date.class),
						EasyMock.anyObject(TemporalType.class))).andReturn(queryDelegate);
		expect(queryDelegate.setParameter(EasyMock.anyObject(Parameter.class), EasyMock.anyObject())).andReturn(
				queryDelegate);
		expect(queryDelegate.getParameterValue(EasyMock.anyObject(Parameter.class))).andReturn(parameter).anyTimes();
		expect(
				queryDelegate.setParameter(EasyMock.anyObject(String.class), EasyMock.anyObject(Calendar.class),
						EasyMock.anyObject(TemporalType.class))).andReturn(queryDelegate);
		expect(
				queryDelegate.setParameter(EasyMock.anyInt(), EasyMock.anyObject(Calendar.class),
						EasyMock.anyObject(TemporalType.class))).andReturn(queryDelegate);

		replay(queryDelegate, parameter, calendar);

		queryProxy = new QueryProxy(queryDelegate, (EntityManagerProxy) manager);
		queryProxy.setParameter("name", "Named Parameter");
		assertEquals(queryProxy.getParameterValue("name"), queryDelegate.getParameterValue("name"));
		queryProxy.setParameter(1, "Client");
		assertEquals(queryProxy.getParameterValue("1"), queryDelegate.getParameterValue("1"));
		queryProxy.setParameter("dateName", dateValue, TemporalType.DATE);
		queryProxy.setParameter(2, dateValue, TemporalType.DATE);
		queryProxy.setParameter(parameter, "X");
		queryProxy.getParameterValue(parameter);
		assertEquals(queryProxy.getParameterValue(parameter), parameter);
		queryProxy.setParameter("dateName", calendar, TemporalType.DATE);
		queryProxy.setParameter(2, calendar, TemporalType.DATE);

		verifyAll();
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testIsBound() {
		Query queryDelegate = PowerMock.createMock(Query.class);
		Parameter parameter = PowerMock.createMock(Parameter.class);
		expect(queryDelegate.isBound(EasyMock.anyObject(Parameter.class))).andReturn(true);

		replay(queryDelegate, parameter);

		queryProxy = new QueryProxy(queryDelegate, (EntityManagerProxy) manager);
		assertTrue(queryProxy.isBound(parameter));

		verifyAll();
	}

	@Test
	public void testFlushMode() {
		Query queryDelegate = PowerMock.createMock(Query.class);
		FlushModeType flushModeType = FlushModeType.AUTO;

		expect(queryDelegate.setFlushMode(FlushModeType.AUTO)).andReturn(queryDelegate);
		expect(queryDelegate.getFlushMode()).andReturn(flushModeType).anyTimes();

		replay(queryDelegate);

		queryProxy = new QueryProxy(queryDelegate, (EntityManagerProxy) manager);
		queryProxy.setFlushMode(FlushModeType.AUTO);
		assertEquals(queryProxy.getFlushMode(), queryDelegate.getFlushMode());
		verifyAll();
	}

	@Test
	public void testLockMode() {
		Query queryDelegate = PowerMock.createMock(Query.class);
		LockModeType lockModeType = LockModeType.OPTIMISTIC;

		expect(queryDelegate.setLockMode(lockModeType)).andReturn(queryDelegate);
		expect(queryDelegate.getLockMode()).andReturn(lockModeType).anyTimes();

		replay(queryDelegate);

		queryProxy = new QueryProxy(queryDelegate, (EntityManagerProxy) manager);
		queryProxy.setLockMode(lockModeType);
		assertEquals(queryProxy.getLockMode(), queryDelegate.getLockMode());
		verifyAll();
	}
}
