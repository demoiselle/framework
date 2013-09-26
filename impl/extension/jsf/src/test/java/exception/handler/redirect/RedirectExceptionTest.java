package exception.handler.redirect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
public class RedirectExceptionTest {

	@ArquillianResource
	private URL deploymentUrl;
	
	private static final String PATH = "src/test/resources/exception-handler-redirect";

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return Tests.createDeployment().addClass(RedirectExceptionTest.class)
				.addClass(RedirectBean.class)
				.addClass(ExceptionWithCorrectRedirect.class)
				.addAsWebResource(Tests.createFileAsset(PATH + "/index.xhtml"), "index.xhtml")
				.addAsWebResource(Tests.createFileAsset(PATH + "/page.xhtml"), "page.xhtml")
				.addAsWebResource(Tests.createFileAsset(PATH + "/redirect.xhtml"), "redirect.xhtml")
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml");
	}
	
	@Test
	public void handleExceptionWithCorrectRedirect() {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(deploymentUrl + "/index.jsf");

		try {
			int status = client.executeMethod(method);
			String message = method.getResponseBodyAsString();
			
			assertEquals(HttpStatus.SC_OK, status);
			assertFalse(message.contains("Correct Redirect Exception!"));
			assertTrue(message.contains("Page redirected!"));

		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void handleExceptionWithWrongRedirect() {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(deploymentUrl + "/page.jsf");

		try {
			int status = client.executeMethod(method);
			assertEquals(HttpStatus.SC_NOT_FOUND, status);

		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

