package br.gov.frameworkdemoiselle.configuration.fields.basic;

import static junit.framework.Assert.assertEquals;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.frameworkdemoiselle.configuration.AbstractConfigurationTest;

@RunWith(Arquillian.class)
public class ConfigurationBasicFieldsTest extends AbstractConfigurationTest {

	@Inject
	private PropertiesBasicFieldsConfig propertiesConfig;

	@Deployment
	public static JavaArchive createDeployment() {
		return createConfigurationDeployment().addPackages(true, ConfigurationBasicFieldsTest.class.getPackage());
	}

	@Test
	public void loadPrimitiveInteger() {
		int expected = 1;

		assertEquals(expected, propertiesConfig.getPrimitiveInteger());
	}

	@Test
	public void loadWrappedInteger() {
		Integer expected = 2;

		assertEquals(expected, propertiesConfig.getWrappedInteger());
	}

	@Test
	public void loadStringWithSpace() {
		String expected = "demoiselle framework";

		assertEquals(expected, propertiesConfig.getStringWithSpace());
	}

//	@Test
	public void loadStringWithComma() {
		String expected = "demoiselle,framework";

		assertEquals(expected, propertiesConfig.getStringWithComma());
	}
}
