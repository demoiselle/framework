package exception.handler.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

@RunWith(Arquillian.class)
public class AuthorizationNotHandledExceptionTest {

	@ArquillianResource
	private URL deploymentUrl;

	private static final String PATH = "src/test/resources/exception-handler-authorization";
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return Tests.createDeployment().addClass(AuthorizationNotHandledExceptionTest.class).addClass(AuthorizationBean.class)
				.addAsWebResource(Tests.createFileAsset(PATH + "/page.xhtml"), "page.xhtml")
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml");
	}
	
	@Test
	public void authorizationNotHandledException() {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(deploymentUrl + "/page.jsf");

		try {
			int status = client.executeMethod(method);
			String message = method.getResponseBodyAsString();
			System.out.println("MESAGE: " + message);
			
			assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, status);
			assertTrue(message.contains("Authorization Exception!"));
			assertFalse(message.contains("Authorization Message."));

		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
