package br.gov.frameworkdemoiselle.configuration.resource;

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
public class ConfigurationResourceTest extends AbstractConfigurationTest{
	
	@Inject
	private PropertiesDefaultFileConfig propDefault;
	
	@Inject
	private PropertiesNamedDefaultFileConfig propNamedDefault;
	
	@Inject
	private PropertiesNotDefaultFileConfig propNotDefault;
	
	@Inject
	private PropertiesWithoutFileConfig propWithoutFile;
	
	@Inject
	private XMLDefaultFileConfig xmlDefault;
	
	@Inject
	private XMLNamedDefaultFileConfig xmlNamedDefault;
	
	@Inject
	private XMLNotDefaultFileConfig xmlNotDefault;
	
	@Inject
	private XMLWithoutFileConfig xmlWithoutFile;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = createConfigurationDeployment();

		deployment.addPackages(true, ConfigurationResourceTest.class.getPackage());
		deployment.addAsResource(new FileAsset(new File(
				"src/test/resources/configuration/resource/demoiselle.properties")), "demoiselle.properties").
				addAsResource(new FileAsset(new File(
						"src/test/resources/configuration/resource/demoiselle.xml")), "demoiselle.xml").
				addAsResource(new FileAsset(new File(
						"src/test/resources/configuration/resource/resource.properties")), "resource.properties").
				addAsResource(new FileAsset(new File(
						"src/test/resources/configuration/resource/resource.xml")), "resource.xml");
		
		return deployment;
	}
	
	@Test
	public void loadFromDefaultFile(){
		int expectedInt = 1;
		String expectedString = "demoiselle framework";
		
		assertEquals(expectedInt, propDefault.getPrimitiveInteger());
		assertEquals(expectedString, propDefault.getStringWithComma());
		
		assertEquals(expectedInt, xmlDefault.getPrimitiveInteger());
		assertEquals(expectedString, xmlDefault.getStringWithComma());
	}
	
	@Test
	public void loadFromNamedDefaultFile(){
		int expectedInt = 1;
		String expectedString = "demoiselle framework";
		
		assertEquals(expectedInt, propNamedDefault.getPrimitiveInteger());
		assertEquals(expectedString, propNamedDefault.getStringWithComma());
		
		assertEquals(expectedInt, xmlNamedDefault.getPrimitiveInteger());
		assertEquals(expectedString, xmlNamedDefault.getStringWithComma());
	}
	
	@Test
	public void loadFromNotDefaultFile(){
		int expectedInt = 2;
		String expectedString = "demoiselle framework from resource";
		
		assertEquals(expectedInt, propNotDefault.getPrimitiveInteger());
		assertEquals(expectedString, propNotDefault.getStringWithComma());
		
		assertEquals(expectedInt, xmlNotDefault.getPrimitiveInteger());
		assertEquals(expectedString, xmlNotDefault.getStringWithComma());
	}
	
	@Test
	public void loadFromNonexistentFile(){
		assertEquals(0, propWithoutFile.getPrimitiveInteger());
		assertEquals(null, propWithoutFile.getStringWithComma());
		
		assertEquals(0, xmlWithoutFile.getPrimitiveInteger());
		assertEquals(null, xmlWithoutFile.getStringWithComma());
	}
	
}
