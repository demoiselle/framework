package xxxx;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

import com.thoughtworks.selenium.DefaultSelenium;

@RunWith(Arquillian.class)
public class XTest {

	private static final String PATH = "src/test/resources/xxx";

	@Drone
	private DefaultSelenium browser;

	@ArquillianResource
	private URL deploymentUrl;

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return Tests.createDeployment().addClass(XServlet.class)
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml");
	}

	@Test
	public void xxxx() {
		browser.open(deploymentUrl + "login");

		// browser.type("id=xxx-input", "demo");
		// browser.waitForPageToLoad("15000");

		// assertTrue("User should be logged in!",
		// browser.isElementPresent("xpath=//li[contains(text(), 'Welcome')]"));
		// assertTrue("Username should be shown!",
		// browser.isElementPresent("xpath=//p[contains(text(), 'You are signed in as demo.')]"));
	}
}
