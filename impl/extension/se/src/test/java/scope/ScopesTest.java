package scope;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class ScopesTest {
	
	@Deployment
	public static JavaArchive createBootstrapDeployment(){
		return Tests.createDeployment(ScopesTest.class)
				.addAsManifestResource(
						Tests.createFileAsset("src/test/resources/SeBootstrapExtension.txt"),
						"services/javax.enterprise.inject.spi.Extension");
	}
	
	@Test
	public void checkRequestActive(){
		
		ScopedBusiness business1 = Beans.getReference(ScopedBusiness.class);
		business1.setValueToRequest("REQUEST SCOPE TEST");
		
		ScopedBusiness business2 = Beans.getReference(ScopedBusiness.class);
		
		Assert.assertNotSame(business1, business2);
		Assert.assertEquals(business1.getValueFromRequest(), business2.getValueFromRequest());
		Assert.assertEquals("REQUEST SCOPE TEST" , business2.getValueFromRequest());
	}
	
	@Test
	public void checkViewActive(){
		
		ScopedBusiness business1 = Beans.getReference(ScopedBusiness.class);
		business1.setValueToView("VIEW SCOPE TEST");
		
		ScopedBusiness business2 = Beans.getReference(ScopedBusiness.class);
		
		Assert.assertNotSame(business1, business2);
		Assert.assertEquals(business1.getValueFromView(), business2.getValueFromView());
		Assert.assertEquals("VIEW SCOPE TEST" , business2.getValueFromView());
	}
	
	@Test
	public void checkSessionActive(){
		
		ScopedBusiness business1 = Beans.getReference(ScopedBusiness.class);
		business1.setValueToSession("SESSION SCOPE TEST");
		
		ScopedBusiness business2 = Beans.getReference(ScopedBusiness.class);
		
		Assert.assertNotSame(business1, business2);
		Assert.assertEquals(business1.getValueFromSession(), business2.getValueFromSession());
		Assert.assertEquals("SESSION SCOPE TEST" , business2.getValueFromSession());
	}
	
	@Test
	public void checkConversationActive(){
		
		ScopedBusiness business1 = Beans.getReference(ScopedBusiness.class);
		business1.setValueToConversation("CONVERSATION SCOPE TEST");
		
		ScopedBusiness business2 = Beans.getReference(ScopedBusiness.class);
		
		Assert.assertNotSame(business1, business2);
		Assert.assertEquals(business1.getValueFromConversation(), business2.getValueFromConversation());
		Assert.assertEquals("CONVERSATION SCOPE TEST" , business2.getValueFromConversation());
	}
	
}
