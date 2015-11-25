package producer.response;

import static junit.framework.Assert.assertEquals;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import producer.request.HelperServlet;
import test.Tests;

@Ignore
@RunWith(Arquillian.class)
public class HttpServletResponseProducerTest {

	private static final String PATH = "src/test/resources/producer/response";

	@ArquillianResource
	private URL deploymentUrl;

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return Tests.createDeployment().addClass(HelperServlet.class)
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml");
	}

	@Test
	public void producedSuccessfully() throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(deploymentUrl + "/helper");
		HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);

		int status = httpResponse.getStatusLine().getStatusCode();
		assertEquals(SC_OK, status);
	}
}
