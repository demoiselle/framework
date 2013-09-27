package producer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.context.http.HttpRequestContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.internal.proxy.EntityManagerProxy;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;

@RunWith(Arquillian.class)
public class RequestScopedProducerTest {

	private static final String PATH = "src/test/resources/producer";
	
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(RequestScopedProducerTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/persistence.xml"), "META-INF/persistence.xml");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		
		return deployment;
	}
	
	@Test
	public void produceEntityManager() {
		EntityManager manager = Beans.getReference(EntityManager.class);

		assertNotNull(manager);
		assertEquals(EntityManagerProxy.class, manager.getClass());
	}

	@Test
	public void produceMultipleEntityManagers() {
		EntityManager m1 = Beans.getReference(EntityManager.class, new NameQualifier("pu"));

		assertNotNull(m1);
		assertEquals(EntityManagerProxy.class, m1.getClass());

		EntityManager m2 = Beans.getReference(EntityManager.class, new NameQualifier("pu2"));

		assertNotNull(m2);
		assertEquals(EntityManagerProxy.class, m2.getClass());
	}

	@Test
	public void produceOneEntityManagerPerRequest() {
		EntityManager m1 = Beans.getReference(EntityManager.class, new NameQualifier("pu"));

		assertNotNull(m1);
		assertEquals(EntityManagerProxy.class, m1.getClass());

		EntityManager m2 = Beans.getReference(EntityManager.class, new NameQualifier("pu"));

		assertNotNull(m2);
		assertEquals(EntityManagerProxy.class, m2.getClass());

		MyEntity entity = new MyEntity();
		entity.setId(createId("testID"));

		m1.persist(entity);

		assertTrue(m2.contains(entity));
	}
	
	@Test
	public void produceDifferentEntityManagerPerRequest() {
		HttpRequestContext weldContext = Beans.getReference(HttpRequestContext.class);
		
		boolean wasNotActive = false;
		if (!weldContext.isActive()){
			wasNotActive = true;
			weldContext.activate();
		}
		
		EntityManager m1 = Beans.getReference(EntityManager.class, new NameQualifier("pu"));
		assertNotNull(m1);
		assertEquals(EntityManagerProxy.class, m1.getClass());
		
		MyEntity entity = new MyEntity();
		entity.setId(createId("testID"));
		
		m1.persist(entity);
		assertTrue(m1.contains(entity));
		
		weldContext.invalidate();
		weldContext.deactivate();
		
		if (!weldContext.isActive()){
			weldContext.activate();
		}
		
		EntityManager m2 = Beans.getReference(EntityManager.class, new NameQualifier("pu"));
		
		assertTrue( m2.isOpen() );
		assertTrue( !m2.contains(entity));
		
		if (wasNotActive && weldContext.isActive()){
			weldContext.invalidate();
			weldContext.deactivate();
		}
	}
	
	private String createId(String id) {
		return this.getClass().getName() + "_" + id;
	}

}
