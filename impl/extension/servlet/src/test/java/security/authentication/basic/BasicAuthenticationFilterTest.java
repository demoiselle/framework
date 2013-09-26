package security.authentication.basic;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

@RunWith(Arquillian.class)
public class BasicAuthenticationFilterTest {

	private static final String PATH = "src/test/resources/security/authentication/basic";

	@ArquillianResource
	private URL deploymentUrl;

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return Tests.createDeployment().addClasses(BasicAuthenticationFilterTest.class)
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml");
	}

	@Test
	public void loginSucessfull() throws ClientProtocolException, IOException {
		String username = "demoiselle";
		String password = "changeit";

		HttpGet httpGet = new HttpGet(deploymentUrl + "/helper");
		byte[] encoded = Base64.encodeBase64((username + ":" + password).getBytes());
		httpGet.setHeader("Authorization", "Basic " + new String(encoded));

		HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);

		int status = httpResponse.getStatusLine().getStatusCode();
		assertEquals(SC_OK, status);
	}

	@Test
	public void loginFailed() throws ClientProtocolException, IOException {
		String username = "invalid";
		String password = "invalid";

		HttpGet httpGet = new HttpGet(deploymentUrl + "/helper");
		byte[] encoded = Base64.encodeBase64((username + ":" + password).getBytes());
		httpGet.setHeader("Authorization", "Basic " + new String(encoded));

		HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);

		int status = httpResponse.getStatusLine().getStatusCode();
		assertEquals(SC_FORBIDDEN, status);
	}
}
