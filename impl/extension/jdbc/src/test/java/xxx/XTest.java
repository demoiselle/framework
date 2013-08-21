package xxx;

import static junit.framework.Assert.assertNotNull;

import java.sql.Connection;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

@RunWith(Arquillian.class)
public class XTest {

	private static final String PATH = "src/test/resources/xxx";

	@Inject
	private Connection conn;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(XTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"),
				"demoiselle.properties");

		return deployment;
	}

	@Test
	public void x() {
		assertNotNull(conn);
	}
}
