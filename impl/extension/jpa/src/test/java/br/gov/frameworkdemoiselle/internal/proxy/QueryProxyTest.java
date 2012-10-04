package br.gov.frameworkdemoiselle.internal.proxy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.easymock.EasyMock;
import org.junit.Assert;
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
 * Test the proxied 
 * @author 81986912515
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Beans.class})
public class QueryProxyTest {
	
	private EntityManager manager;
	private EntityManagerProducer producer;
	
	@Before
	public void setUp(){
		
		Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
		configOverrides.put("javax.persistence.jdbc.url", "jdbc:hsqldb:hsql:.");
		configOverrides.put("hibernate.show_sql", "false");
		configOverrides.put("hibernate.hbm2ddl.auto", "create-drop");
		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("pu1", configOverrides);
		EntityManager delegate = factory.createEntityManager();
		
		Map<String, EntityManager> cache = Collections.synchronizedMap(new HashMap<String, EntityManager>());
		cache.put("pu1", delegate);
		
		producer = new EntityManagerProducer();
		Whitebox.setInternalState(producer, "cache", cache);
		
		PowerMock.mockStatic(Beans.class);
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(12);
		PowerMock.replayAll();

		manager = new EntityManagerProxy("pu1");
		
		manager.getTransaction().begin();
		manager.createQuery("delete from Client").executeUpdate();
		
		Client client = new Client();
		client.setName("Cliente 1");
		manager.persist(client);
		
		client = new Client();
		client.setName("Cliente 2");
		manager.persist(client);
		
		client = new Client();
		client.setName("Cliente 3");
		manager.persist(client);
		
		manager.flush();
		manager.getTransaction().commit();
		manager.clear();
		
		PowerMock.resetAll();
	}
	
	private QueryProxy getQueryProxy(String jpql,Object... params){
		Query q = manager.createQuery(jpql);
		if (!(q instanceof QueryProxy)){
			Assert.fail("Query não é instância de QueryProxy");
		}
		
		if (params!=null){
			int count = 1;
			for (Object param : params){
				q.setParameter(count++, param);
			}
		}
		
		return (QueryProxy)q;
	}
	
	@Test
	public void testResultList(){
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(2);
		PowerMock.replay(Beans.class);
		
		List<?> retorno = getQueryProxy("select c from Client c").getResultList();
		Assert.assertNotNull(retorno);
		Assert.assertFalse(retorno.isEmpty());
	}
	
	@Test
	public void testSingleResult(){
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(2);
		PowerMock.replay(Beans.class);
		
		Client retorno = (Client)getQueryProxy("select c from Client c where c.name=?1","Cliente 1").getSingleResult();
		Assert.assertNotNull(retorno);
	}
	
	@Test
	public void testExecuteUpdate(){
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(4);
		PowerMock.replay(Beans.class);

		manager.getTransaction().begin();
		int linesAffected = getQueryProxy("update Client set name=?1 where name=?2","Novo Cliente","Cliente 1").executeUpdate();
		manager.getTransaction().commit();
		Assert.assertEquals(1, linesAffected);
	}
	
	@Test
	public void testPagination(){
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(4);
		PowerMock.replay(Beans.class);

		QueryProxy proxy = getQueryProxy("select c from Client c");
		
		proxy.setMaxResults(2);
		Assert.assertEquals(2, proxy.getMaxResults());
		
		proxy.setFirstResult(1);
		Assert.assertEquals(1, proxy.getFirstResult());
		
		List<?> result = proxy.getResultList();
		Assert.assertEquals(2, result.size());
	}

}
