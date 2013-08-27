package productor;

import javax.persistence.EntityManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.internal.proxy.EntityManagerProxy;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;


@RunWith(Arquillian.class)
public class ProductorTest {
	
	private static final String PATH = "src/test/resources/productor";
	
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(ProductorTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/persistence.xml"), "META-INF/persistence.xml");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");

		return deployment;
	}
	
	@Test
	public void produceEntityManager(){
		
		EntityManager manager = Beans.getReference(EntityManager.class);
		Assert.assertNotNull(manager);
		Assert.assertEquals(EntityManagerProxy.class,manager.getClass());
		
	}
	
	@Test
	public void produceMultipleEntityManagers(){
		
		EntityManager m1 = Beans.getReference(EntityManager.class,new NameQualifier("pu"));
		
		Assert.assertNotNull(m1);
		Assert.assertEquals(EntityManagerProxy.class,m1.getClass());
		
		EntityManager m2 = Beans.getReference(EntityManager.class,new NameQualifier("pu2"));
		
		Assert.assertNotNull(m2);
		Assert.assertEquals(EntityManagerProxy.class,m2.getClass());
		
	}
	
	@Test
	public void produceOneEntityManagerPerRequest(){
		EntityManager m1 = Beans.getReference(EntityManager.class,new NameQualifier("pu"));
		
		Assert.assertNotNull(m1);
		Assert.assertEquals(EntityManagerProxy.class,m1.getClass());
		
		EntityManager m2 = Beans.getReference(EntityManager.class,new NameQualifier("pu"));
		
		Assert.assertNotNull(m2);
		Assert.assertEquals(EntityManagerProxy.class,m2.getClass());
		
		MyEntity entity = new MyEntity();
		entity.setId(createId("testID"));
		
		m1.persist(entity);
		
		Assert.assertTrue( m2.contains(entity) );
	}
	
	private String createId(String id) {
		return this.getClass().getName() + "_" + id;
	}
	
}
