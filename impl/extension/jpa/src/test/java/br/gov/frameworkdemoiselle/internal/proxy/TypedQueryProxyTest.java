package br.gov.frameworkdemoiselle.internal.proxy;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
 * @author 81986912515
 *
 */
@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({Beans.class})
public class TypedQueryProxyTest {
	
	private EntityManager manager;
	private EntityManagerProducer producer;
	
	@Before
	public void setUp(){
		
		Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
		configOverrides.put("javax.persistence.jdbc.url", "jdbc:hsqldb:hsql:.");
		configOverrides.put("hibernate.show_sql", "true");
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
		client.setBirthDate(new Date());
		manager.persist(client);
		
		client = new Client();
		client.setName("Cliente 2");
		client.setBirthDate(new Date());
		manager.persist(client);
		
		client = new Client();
		client.setName("Cliente 3");
		client.setBirthDate(new Date());
		manager.persist(client);
		
		manager.flush();
		manager.getTransaction().commit();
		manager.clear();
		
		PowerMock.resetAll();
	}
	
	private <T> TypedQueryProxy<T> getQueryProxy(String jpql,Class<T> classType,Object... params){
		TypedQuery<T> q = manager.createQuery(jpql,classType);
		if (!(q instanceof TypedQueryProxy)){
			Assert.fail("Query não é instância de QueryProxy");
		}
		
		if (params!=null){
			int count = 1;
			for (Object param : params){
				q.setParameter(count++, param);
			}
		}
		
		return (TypedQueryProxy<T>)q;
	}
	
	@Test
	public void testResultList(){
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(2);
		PowerMock.replay(Beans.class);
		
		List<?> retorno = getQueryProxy("select c from Client c",Client.class).getResultList();
		Assert.assertNotNull(retorno);
		Assert.assertFalse(retorno.isEmpty());
	}
	
	@Test
	public void testSingleResult(){
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(2);
		PowerMock.replay(Beans.class);
		
		Client retorno = (Client)getQueryProxy("select c from Client c where c.name=?1",Client.class,"Cliente 1").getSingleResult();
		Assert.assertNotNull(retorno);
	}
	
	@Test
	public void testPagination(){
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(2);
		PowerMock.replay(Beans.class);

		TypedQueryProxy<Client> proxy = getQueryProxy("select c from Client c",Client.class);
		
		proxy.setMaxResults(2);
		Assert.assertEquals(2, proxy.getMaxResults());
		
		proxy.setFirstResult(1);
		Assert.assertEquals(1, proxy.getFirstResult());
		
		List<?> result = proxy.getResultList();
		Assert.assertEquals(2, result.size());
	}
	
	@Test
	public void testHint(){
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(14);
		PowerMock.replay(Beans.class);

		//Consulta um cliente definindo a hint readOnly, que torna a entidade retornada não atualizável.
		manager.getTransaction().begin();
		TypedQueryProxy<Client> proxy = getQueryProxy("select c from Client c where c.name=?1",Client.class,"Cliente 1");
		proxy.setHint("org.hibernate.readOnly", true);
		Assert.assertFalse( proxy.getHints().isEmpty() );
		
		//Tenta atualizar a entidade e limpar o cache de primeiro nível
		Client c = (Client)proxy.getSingleResult();
		c.setName("Cliente 1 Alterado");
		manager.flush();
		manager.getTransaction().commit();
		manager.clear();
		
		//Reconsultar a entidade tem que retornar 1 resultado, pois o nome "Cliente 1" não deve ter sido alterado.
		manager.getTransaction().begin();
		proxy = getQueryProxy("select c from Client c where c.name=?1",Client.class,"Cliente 1");
		c = (Client)proxy.getSingleResult();
		Assert.assertNotNull(c);
		
		//Mudar a entidade agora tem que funcionar, pois não foi informado o hint
		c.setName("Cliente 1 Alterado");
		manager.flush();
		manager.getTransaction().commit();
		manager.clear();
		
		proxy = getQueryProxy("select c from Client c where c.name=?1",Client.class,"Cliente 1");
		
		try{
			proxy.getSingleResult();
			Assert.fail();
		}
		catch(NoResultException ne){
		}

		PowerMock.verifyAll();
	}
	
	@Test
	public void testParameters(){
		
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(2);
		PowerMock.replay(Beans.class);
		
		TypedQueryProxy<Client> proxy = getQueryProxy("select c from Client c where 'Named Parameter'=:name and c.birthDate=:dateName and c.name=?1 and c.birthDate=?2",Client.class);
		
		Date dateValue = new Date();
		
		proxy.setParameter("name", "Named Parameter");
		proxy.setParameter("dateName", dateValue, TemporalType.DATE);
		
		proxy.setParameter(1, "Cliente 1");
		proxy.setParameter(2, dateValue,TemporalType.DATE);
		
		Assert.assertEquals(proxy.getParameterValue("name"),"Named Parameter");
		Assert.assertEquals(proxy.getParameterValue(1), "Cliente 1");
		
		List<Client> result = proxy.getResultList();
		
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFlushMode(){
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(3);
		PowerMock.replay(Beans.class);
		
		manager.getTransaction().begin();
		TypedQueryProxy<Client> proxy = getQueryProxy("select c from Client c where c.name=?1",Client.class,"Cliente 1");
		proxy.setFlushMode(FlushModeType.COMMIT);
		Assert.assertEquals(proxy.getFlushMode(), FlushModeType.COMMIT);
		manager.getTransaction().commit();
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testLockMode(){
		EasyMock.expect(Beans.getReference(EntityManagerProducer.class)).andReturn(producer).times(3);
		PowerMock.replay(Beans.class);
		
		manager.getTransaction().begin();
		TypedQueryProxy<Client> proxy = getQueryProxy("select c from Client c where c.name=?1",Client.class,"Cliente 1");
		proxy.setLockMode(LockModeType.OPTIMISTIC);
		Assert.assertEquals(proxy.getLockMode(), LockModeType.OPTIMISTIC);
		manager.getTransaction().commit();
		
		PowerMock.verifyAll();
	}
}
