package management.validation;

import management.testclasses.DummyManagedClass;
import management.testclasses.DummyManagementExtension;
import management.testclasses.DummyValidator;
import management.testclasses.DummyValidatorAnnotation;
import management.testclasses.ManagedClassStore;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class ValidationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		
		return Tests.createDeployment(ValidationTest.class)
				.addClasses(DummyManagementExtension.class, ManagedClassStore.class, DummyManagedClass.class,DummyValidator.class, DummyValidatorAnnotation.class);
		
	}

	/**
	 * Test if a management controller accepts a valid value annotated with a core validation (from javax.validation)
	 * when a property is being set by a management client
	 */
	@Test
	public void setValidValue() {
		// Testa se é possível definir um valor válido para uma propriedade.
		ManagedClassStore store = Beans.getReference(ManagedClassStore.class);
		store.setProperty(DummyManagedClass.class, "id", new Integer(1));
		Assert.assertEquals(new Integer(1), store.getProperty(DummyManagedClass.class, "id"));
	}

	/**
	 * Test if a management controller refuses a valid value annotated with a core validation (from javax.validation)
	 * when a property is being set by a management client
	 */
	@Test
	public void setInvalidValue() {
		// Testa se é possível definir um valor válido para uma propriedade.
		try {
			ManagedClassStore store = Beans.getReference(ManagedClassStore.class);
			store.setProperty(DummyManagedClass.class, "id", (Integer) null);

			Assert.fail();
		} catch (DemoiselleException de) {
			// Classes de gerenciamento disparam Demoiselle Exception quando uma validação falha
		}
	}

	/**
	 * Tests if custom validators (outside the javax.validation package) run as normal
	 */
	@Test
	public void customValidation() {

		try {
			ManagedClassStore store = Beans.getReference(ManagedClassStore.class);

			// Atributo "gender" deve aceitar apenas "M" ou "F", tanto maiúsculo quanto minúsculo. A anotação
			// customizada DummyValidatorAnnotation é uma simples validação que testa se uma string passada está
			// na lista de strings aceitas.
			store.setProperty(DummyManagedClass.class, "gender", "J");

			Assert.fail();
		} catch (DemoiselleException e) {
			Assert.assertTrue(e.getMessage().contains("Test Message"));
		}

	}

}
