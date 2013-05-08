package management;

import java.io.File;

import management.testclasses.DummyManagedClass;
import management.testclasses.DummyManagementExtension;
import management.testclasses.ManagedClassStore;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.LocaleProducer;
import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class ValidationTestCase {
	
	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap
				.create(JavaArchive.class)
				.addClass(LocaleProducer.class)
				.addPackages(true, "br")
				.addAsResource(new FileAsset(new File("src/test/resources/test/beans.xml")), "beans.xml")
				.addAsManifestResource(
						new File("src/main/resources/META-INF/services/javax.enterprise.inject.spi.Extension"),
						"services/javax.enterprise.inject.spi.Extension")
				.addPackages(false, NotificationTestCase.class.getPackage())
				.addClasses(DummyManagementExtension.class,ManagedClassStore.class,DummyManagedClass.class);
	}
	
	/**
	 * Test if changing properties of a management controller passes through
	 * validation phase.
	 */
	@Test
	public void testManagedClassValidation(){
		
		//Testa se é possível definir um valor válido para uma propriedade.
		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);
		store.setProperty(DummyManagedClass.class, "id", new Integer(1));
		Assert.assertEquals(new Integer(1), store.getProperty(DummyManagedClass.class, "id"));
		
		//Testa se definir um valor inválido dispara o erro adequado
		try{
			store.setProperty(DummyManagedClass.class, "id", new Integer(5));
			Assert.fail();
		}
		catch(DemoiselleException e){
			//SUCCESS
		}
		
	}

}
