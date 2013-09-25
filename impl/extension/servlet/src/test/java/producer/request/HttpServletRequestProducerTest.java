package producer.request;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

@RunWith(Arquillian.class)
public class HttpServletRequestProducerTest {

	private static final String PATH = "src/test/resources/producer/request";

	@ArquillianResource
	private URL deploymentUrl;

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return Tests.createDeployment().addClass(RequestServlet.class)
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml");
	}

	@Test
	public void createR() throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(deploymentUrl + "/servlet");
		HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);

		int status = httpResponse.getStatusLine().getStatusCode();
		assertEquals(HttpStatus.SC_OK, status);
	}
}
