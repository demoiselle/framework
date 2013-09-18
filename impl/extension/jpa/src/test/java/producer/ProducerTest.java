package producer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.context.RequestContext;
import br.gov.frameworkdemoiselle.internal.proxy.EntityManagerProxy;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;

@RunWith(Arquillian.class)
public class ProducerTest {

	private static final String PATH = "src/test/resources/producer";

	@Deployment(name="request_scoped_producer")
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(ProducerTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/persistence.xml"), "META-INF/persistence.xml");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		
		return deployment;
	}
	
	@Deployment(name="no_scoped_producer")
	public static WebArchive createNoScopedDeployment() {
		WebArchive deployment = Tests.createDeployment(ProducerTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/persistence.xml"), "META-INF/persistence.xml");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle_noscoped.properties"), "demoiselle.properties");
		
		return deployment;
	}
	
	@Before
	public void before(){
		RequestContext ctx = Beans.getReference(RequestContext.class);
		ctx.activate();
	}
	
	@After
	public void after(){
		RequestContext ctx = Beans.getReference(RequestContext.class);
		ctx.deactivate();
	}

	@Test
	@OperateOnDeployment("request_scoped_producer")
	public void produceEntityManager() {
		EntityManager manager = Beans.getReference(EntityManager.class);

		assertNotNull(manager);
		assertEquals(EntityManagerProxy.class, manager.getClass());
	}

	@Test
	@OperateOnDeployment("request_scoped_producer")
	public void produceMultipleEntityManagers() {
		EntityManager m1 = Beans.getReference(EntityManager.class, new NameQualifier("pu"));

		assertNotNull(m1);
		assertEquals(EntityManagerProxy.class, m1.getClass());

		EntityManager m2 = Beans.getReference(EntityManager.class, new NameQualifier("pu2"));

		assertNotNull(m2);
		assertEquals(EntityManagerProxy.class, m2.getClass());
	}

	@Test
	@OperateOnDeployment("request_scoped_producer")
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
	@OperateOnDeployment("no_scoped_producer")
	public void produceOneEntityManagerPerInjection() {
		//Testa se ao usar o produtor sem escopo, mais de um entity manager é criado a cada injeção.
		
		EntityManager m1 = Beans.getReference(EntityManager.class, new NameQualifier("pu"));

		assertNotNull(m1);
		assertEquals(EntityManagerProxy.class, m1.getClass());

		EntityManager m2 = Beans.getReference(EntityManager.class, new NameQualifier("pu"));

		assertNotNull(m2);
		assertEquals(EntityManagerProxy.class, m2.getClass());

		MyEntity entity = new MyEntity();
		entity.setId(createId("testID"));

		m1.persist(entity);

		assertTrue( ! m2.contains(entity));
	}

	private String createId(String id) {
		return this.getClass().getName() + "_" + id;
	}

}
