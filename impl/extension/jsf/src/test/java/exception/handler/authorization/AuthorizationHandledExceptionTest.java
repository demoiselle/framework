package exception.handler.authorization;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

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
public class AuthorizationHandledExceptionTest {

	@ArquillianResource
	private URL deploymentUrl;

	private static final String PATH = "src/test/resources/exception-handler-authorization";
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return Tests.createDeployment().addClass(AuthorizationHandledExceptionTest.class).addClass(AuthorizationBean.class)
				.addAsWebResource(Tests.createFileAsset(PATH + "/index.xhtml"), "index.xhtml")
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml")
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/pretty-config.xml"), "pretty-config.xml");

	}
	
	@Test
	public void authorizationHandledException() {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(deploymentUrl + "/index");

		try {
			int status = client.executeMethod(method);
			String message = method.getResponseBodyAsString();
			System.out.println("MESAGE: " + message);
			
			assertNotSame(HttpStatus.SC_INTERNAL_SERVER_ERROR, status);
			assertTrue(message.contains("Authorization Message."));
			assertTrue(message.contains("Authorization Exception!"));

		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
