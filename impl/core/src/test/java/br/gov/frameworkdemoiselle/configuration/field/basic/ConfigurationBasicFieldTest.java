package br.gov.frameworkdemoiselle.configuration.field.basic;

import static junit.framework.Assert.assertEquals;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.FileAsset;
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
	
	@Inject
	private SystemBasicFieldConfig systemConfig;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = createConfigurationDeployment();

		deployment.addPackages(true, ConfigurationBasicFieldTest.class.getPackage());
		deployment.addAsResource(new FileAsset(new File(
				"src/test/resources/configuration/field/basic/demoiselle.properties")), "demoiselle.properties").
				addAsResource(new FileAsset(new File(
						"src/test/resources/configuration/field/basic/demoiselle.xml")), "demoiselle.xml");

		return deployment;
	}
	
	@Test
	public void loadPrimitiveInteger() {
		int expected = 1;
		System.setProperty("primitiveInteger", String.valueOf(expected));

		assertEquals(expected, systemConfig.getPrimitiveInteger());
		assertEquals(expected, propertiesConfig.getPrimitiveInteger());
		assertEquals(expected, xmlConfig.getPrimitiveInteger());
	}

	@Test
	public void loadWrappedInteger() {
		Integer expected = 2;
		System.setProperty("wrappedInteger", String.valueOf(expected));

		assertEquals(expected, systemConfig.getWrappedInteger());
		assertEquals(expected, propertiesConfig.getWrappedInteger());
		assertEquals(expected, xmlConfig.getWrappedInteger());
	}

	@Test
	public void loadStringWithSpace() {
		String expected = "demoiselle framework";
		System.setProperty("stringWithSpace", String.valueOf(expected));

		assertEquals(expected, systemConfig.getStringWithSpace());
		assertEquals(expected, propertiesConfig.getStringWithSpace());
		assertEquals(expected, xmlConfig.getStringWithSpace());
	}

	//@Test
	public void loadStringWithComma() {
		String expected = "demoiselle,framework";
		System.setProperty("stringWithComma", String.valueOf(expected));

		assertEquals(expected, systemConfig.getStringWithComma());
		assertEquals(expected, propertiesConfig.getStringWithComma());
		assertEquals(expected, xmlConfig.getStringWithComma());
	}
}
