package br.gov.frameworkdemoiselle.configuration.field.basic;

import static junit.framework.Assert.assertEquals;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.frameworkdemoiselle.configuration.AbstractConfigurationTest;

@RunWith(Arquillian.class)
public class ConfigurationBasicFieldTest extends AbstractConfigurationTest {

	@Inject
	private PropertiesBasicFieldConfig propertiesConfig;
	
	@Inject
	private XMLBasicFieldConfig xmlConfig;

	@Deployment
	public static JavaArchive createDeployment() {
		return createConfigurationDeployment().addPackages(true, ConfigurationBasicFieldTest.class.getPackage());
	}

	@Test
	public void loadPrimitiveInteger() {
		int expected = 1;

		assertEquals(expected, propertiesConfig.getPrimitiveInteger());
		assertEquals(expected, xmlConfig.getPrimitiveInteger());
	}

	@Test
	public void loadWrappedInteger() {
		Integer expected = 2;

		assertEquals(expected, propertiesConfig.getWrappedInteger());
		assertEquals(expected, xmlConfig.getWrappedInteger());
	}

	@Test
	public void loadStringWithSpace() {
		String expected = "demoiselle framework";

		assertEquals(expected, propertiesConfig.getStringWithSpace());
		assertEquals(expected, xmlConfig.getStringWithSpace());
	}

//	@Test
	public void loadStringWithComma() {
		String expected = "demoiselle,framework";

		assertEquals(expected, propertiesConfig.getStringWithComma());
		assertEquals(expected, xmlConfig.getStringWithComma());
	}
}
