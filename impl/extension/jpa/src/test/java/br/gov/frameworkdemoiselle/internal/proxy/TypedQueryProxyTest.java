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
import javax.persistence.TypedQuery;

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
 * Test the proxied {@link Query} class, {@link TypedQueryProxy}.
 * 
 * @author 81986912515
 * @param <X>
 */

@SuppressWarnings("rawtypes")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class })
public class TypedQueryProxyTest {

	private EntityManager manager;

	private EntityManagerProducer producer;

	private TypedQueryProxy typedQueryProxy;

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

		// PowerMock.resetAll();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testResultList() {

		TypedQuery queryDelegate = PowerMock.createMock(TypedQuery.class);
		List<String> result = new ArrayList<String>();
		result.add("x");
		EasyMock.expect(queryDelegate.getResultList()).andReturn(result).anyTimes();

		replay(queryDelegate);

		typedQueryProxy = new TypedQueryProxy(queryDelegate, (EntityManagerProxy) manager);
		assertEquals(typedQueryProxy.getResultList().size(), queryDelegate.getResultList().size());

		verifyAll();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSingleResult() {
		TypedQuery queryDelegate = PowerMock.createMock(TypedQuery.class);
		String result = "Resultado";
		EasyMock.expect(queryDelegate.getSingleResult()).andReturn(result).anyTimes();

		replay(queryDelegate);

		typedQueryProxy = new TypedQueryProxy(queryDelegate, (EntityManagerProxy) manager);
		assertEquals(typedQueryProxy.getSingleResult(), queryDelegate.getSingleResult());

		verifyAll();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPagination() {
		TypedQuery queryDelegate = PowerMock.createMock(TypedQuery.class);
		expect(queryDelegate.getMaxResults()).andReturn(4).times(2);
		expect(queryDelegate.getFirstResult()).andReturn(2).times(2);
		expect(queryDelegate.setMaxResults(EasyMock.anyInt())).andReturn(queryDelegate);
		expect(queryDelegate.setFirstResult(EasyMock.anyInt())).andReturn(queryDelegate);
		replay(queryDelegate);

		typedQueryProxy = new TypedQueryProxy(queryDelegate, (EntityManagerProxy) manager);
		typedQueryProxy.setMaxResults(4);
		typedQueryProxy.setFirstResult(2);
		assertEquals(typedQueryProxy.getMaxResults(), queryDelegate.getMaxResults());
		assertEquals(typedQueryProxy.getFirstResult(), queryDelegate.getFirstResult());

		verifyAll();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHint() {
		TypedQuery queryDelegate = PowerMock.createMock(TypedQuery.class);
		Map<String, Object> map = new HashMap<String, Object>();
		Client client = new Client();
		map.put("1", client);
		expect(queryDelegate.getHints()).andReturn(map).times(2);
		expect(queryDelegate.setHint(EasyMock.anyObject(String.class), EasyMock.anyObject())).andReturn(queryDelegate);
		replay(queryDelegate);

		typedQueryProxy = new TypedQueryProxy(queryDelegate, (EntityManagerProxy) manager);
		typedQueryProxy.setHint("1", client);
		assertEquals(typedQueryProxy.getHints(), queryDelegate.getHints());

		verifyAll();
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@Test
	public void testParameters() {
		TypedQuery queryDelegate = PowerMock.createMock(TypedQuery.class);
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

		typedQueryProxy = new TypedQueryProxy(queryDelegate, (EntityManagerProxy) manager);
		typedQueryProxy.setParameter("name", "Named Parameter");
		assertEquals(typedQueryProxy.getParameterValue("name"), queryDelegate.getParameterValue("name"));
		typedQueryProxy.setParameter(1, "Client");
		assertEquals(typedQueryProxy.getParameterValue("1"), queryDelegate.getParameterValue("1"));
		typedQueryProxy.setParameter("dateName", dateValue, TemporalType.DATE);
		typedQueryProxy.setParameter(2, dateValue, TemporalType.DATE);
		typedQueryProxy.setParameter(parameter, "X");
		typedQueryProxy.getParameterValue(parameter);
		assertEquals(typedQueryProxy.getParameterValue(parameter), parameter);
		typedQueryProxy.setParameter("dateName", calendar, TemporalType.DATE);
		typedQueryProxy.setParameter(2, calendar, TemporalType.DATE);

		verifyAll();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testIsBound() {
		TypedQuery queryDelegate = PowerMock.createMock(TypedQuery.class);
		Parameter parameter = PowerMock.createMock(Parameter.class);
		expect(queryDelegate.isBound(EasyMock.anyObject(Parameter.class))).andReturn(true);

		replay(queryDelegate, parameter);

		typedQueryProxy = new TypedQueryProxy(queryDelegate, (EntityManagerProxy) manager);
		assertTrue(typedQueryProxy.isBound(parameter));

		verifyAll();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFlushMode() {
		TypedQuery queryDelegate = PowerMock.createMock(TypedQuery.class);
		FlushModeType flushModeType = FlushModeType.AUTO;

		expect(queryDelegate.setFlushMode(FlushModeType.AUTO)).andReturn(queryDelegate);
		expect(queryDelegate.getFlushMode()).andReturn(flushModeType).anyTimes();

		replay(queryDelegate);

		typedQueryProxy = new TypedQueryProxy(queryDelegate, (EntityManagerProxy) manager);
		typedQueryProxy.setFlushMode(FlushModeType.AUTO);
		assertEquals(typedQueryProxy.getFlushMode(), queryDelegate.getFlushMode());
		verifyAll();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLockMode() {
		TypedQuery queryDelegate = PowerMock.createMock(TypedQuery.class);
		LockModeType lockModeType = LockModeType.OPTIMISTIC;

		expect(queryDelegate.setLockMode(lockModeType)).andReturn(queryDelegate);
		expect(queryDelegate.getLockMode()).andReturn(lockModeType).anyTimes();

		replay(queryDelegate);

		typedQueryProxy = new TypedQueryProxy(queryDelegate, (EntityManagerProxy) manager);
		typedQueryProxy.setLockMode(lockModeType);
		assertEquals(typedQueryProxy.getLockMode(), queryDelegate.getLockMode());
		verifyAll();
	}

}
