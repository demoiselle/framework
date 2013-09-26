package producer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.context.ContextNotActiveException;
import javax.persistence.EntityManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.context.ViewContext;
import br.gov.frameworkdemoiselle.internal.proxy.EntityManagerProxy;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;

//TODO Implementação de escopo selecionável tem que concluir antes de ativar esse teste
@Ignore
@RunWith(Arquillian.class)
public class ViewScopedProducerTest {

	private static final String PATH = "src/test/resources/producer";
	
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(ViewScopedProducerTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/persistence.xml"), "META-INF/persistence.xml");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle_viewscoped.properties"), "demoiselle.properties");
		
		return deployment;
	}
	
	@Test
	public void produceOneEntityManagerPerView() {
		ViewContext ctx = Beans.getReference(ViewContext.class);
		ctx.activate();
		
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
		
		ctx.deactivate();
	}
	
	@Test(expected=ContextNotActiveException.class)
	public void errorWhenContextNotActive() {
		ViewContext ctx = Beans.getReference(ViewContext.class);
		if (ctx.isActive()){
			ctx.deactivate();
		}
		
		EntityManager m1 = Beans.getReference(EntityManager.class, new NameQualifier("pu"));

		assertNotNull(m1);
		assertEquals(EntityManagerProxy.class, m1.getClass());

		MyEntity entity = new MyEntity();
		entity.setId(createId("testID"));
		
		m1.persist(entity);
	}
	
	private String createId(String id) {
		return this.getClass().getName() + "_" + id;
	}

}
