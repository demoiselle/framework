package xxxx;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

@RunWith(Arquillian.class)
public class XTest {

	private static final String PATH = "src/test/resources/xxx";

	@ArquillianResource
	private URL deploymentUrl;

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return Tests.createDeployment().addClass(XServlet.class)
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml");
	}

	@Test
	public void xxxx() {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(deploymentUrl + "/login");

		try {
			int status = client.executeMethod(method);
			System.out.println(status);

		} catch (HttpException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// browser.open(deploymentUrl + "login");

		// browser.type("id=xxx-input", "demo");
		// browser.waitForPageToLoad("15000");

		// assertTrue("User should be logged in!",
		// browser.isElementPresent("xpath=//li[contains(text(), 'Welcome')]"));
		// assertTrue("Username should be shown!",
		// browser.isElementPresent("xpath=//p[contains(text(), 'You are signed in as demo.')]"));
	}
}
