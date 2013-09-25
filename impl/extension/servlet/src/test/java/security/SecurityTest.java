package security;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpParamsNames;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

import com.sun.enterprise.security.auth.login.FileLoginModule;

@RunWith(Arquillian.class)
public class SecurityTest {

	private static final String PATH = "src/test/resources/security";

	@ArquillianResource
	private URL deploymentUrl;

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return Tests.createDeployment().addClasses(SecurityServlet.class, FileLoginModule.class)
				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml");
	}

	@Test
	public void loginSucessfull() throws ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
//		HttpGet get = new HttpGet(deploymentUrl + "/login");
//		get.getParams().setParameter("username", "demoiselle");
//		get.getParams().setParameter("password", "changeit");
		HttpGet get = new HttpGet(deploymentUrl + "/login?username=demoiselle&password=changeit");
		HttpResponse response = client.execute(get);

		int status = response.getStatusLine().getStatusCode();
		assertEquals(HttpStatus.SC_OK, status);
	}
	
	@Test
	public void loginFailed() throws ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(deploymentUrl + "/login?username=demoiselle&password=wrongpass");
		HttpResponse response = client.execute(get);
		
		int status = response.getStatusLine().getStatusCode();
		assertEquals(HttpStatus.SC_UNAUTHORIZED, status);
	}
}
