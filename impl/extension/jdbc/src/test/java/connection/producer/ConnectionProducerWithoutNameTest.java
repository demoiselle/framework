package connection.producer;

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

@RunWith(Arquillian.class)
public class ConnectionProducerWithoutNameTest {

	private static String PATH = "src/test/resources/producer/without-name";

	@Inject
	private Connection connection;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(ConnectionProducerWithoutNameTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		return deployment;
	}

	@Test
	public void createConnection() {
		assertNotNull(connection);
	}

}
