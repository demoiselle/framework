package producer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.context.RequestContext;

@RunWith(Arquillian.class)
public class HttpServletResponseProducerTest {

	private static final String PATH = "src/test/resources/producer";

	@ArquillianResource
	private URL deploymentUrl;

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return Tests.createDeployment().addClass(RequestServlet.class)
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml");
	}

	@Test
	public void createResponse() {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(deploymentUrl + "/responseproducer");
		try {
			int status = client.executeMethod(method);
			assertEquals(HttpStatus.SC_OK, status);
		} catch (Exception e) {
			fail();
		}
	}

}
