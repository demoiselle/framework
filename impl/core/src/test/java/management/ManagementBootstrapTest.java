package management;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.lifecycle.AfterShutdownProccess;
import br.gov.frameworkdemoiselle.management.extension.ManagementExtension;
import br.gov.frameworkdemoiselle.util.Beans;


@RunWith(Arquillian.class)
public class ManagementBootstrapTest {
	
	@Inject
	private ManagedClassStore store;
	
	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(ManagementBootstrapTest.class);
		
		/*deployment
			.addClass(ManagedClassStore.class)
			.addClass(DummyManagedClass.class)
			.addClass(DummyManagementExtension.class);*/
		
		return deployment;
	}
	
	/**
	 * Test if a a management extension (a library that implements {@link ManagementExtension}) is correctly detected.
	 */
	@Test
	public void testManagementExtensionRegistration(){
		
		//"store" é application scoped e é usado pelo DummyManagementExtension para
		//armazenar todos os beans anotados com @Managed. Se o bootstrap rodou corretamente,
		//ele chamou DummyManagementExtension.initialize e este store conterá o bean de teste que anotamos.
		Assert.assertNotNull(store.getManagedTypes());
		Assert.assertEquals(1, store.getManagedTypes().size());
		
	}
	
	/**
	 * Test if a a management extension's (a library that implements {@link ManagementExtension}) shutdown
	 * method is correctly called upon application shutdown.
	 */
	@Test
	public void testManagementExtensionShutdown(){
		
		//"store" é application scoped e é usado pelo DummyManagementExtension para
		//armazenar todos os beans anotados com @Managed. Se o bootstrap rodou corretamente,
		//ele chamou DummyManagementExtension.initialize e este store conterá o bean de teste que anotamos.
		//Nós então disparamos o evento de shutdown onde ele deverá limpar o store.
		Assert.assertNotNull(store.getManagedTypes());
		Assert.assertEquals(1, store.getManagedTypes().size());
		
		Beans.getBeanManager().fireEvent(new AfterShutdownProccess() {});
		Assert.assertNull(store.getManagedTypes());
	}

}
