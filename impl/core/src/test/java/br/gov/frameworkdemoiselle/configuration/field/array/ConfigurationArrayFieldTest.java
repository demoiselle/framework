package br.gov.frameworkdemoiselle.configuration.field.array;

import static org.junit.Assert.assertArrayEquals;

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
public class ConfigurationArrayFieldTest extends AbstractConfigurationTest {

	@Inject
	private PropertiesArrayFieldConfig propertiesConfig;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = createConfigurationDeployment();

		deployment.addPackages(true, ConfigurationArrayFieldTest.class.getPackage());
		deployment.addAsResource(new FileAsset(new File(
				"src/test/resources/configuration/field/array/demoiselle.properties")), "demoiselle.properties");

		return deployment;
	}

	@Test
	public void loadPrimitiveInteger() {
		int[] expected = { 1, 20, 0 };

		assertArrayEquals(expected, propertiesConfig.getPrimitiveIntegers());
	}

	// private int[] primitiveIntegers;

	// private Integer[] wrappedIntegers;
	//
	// private String[] strings;
	//
	// private double[] primitiveDoubles;
	//
	// private Double[] wrappedDoubles;
}
