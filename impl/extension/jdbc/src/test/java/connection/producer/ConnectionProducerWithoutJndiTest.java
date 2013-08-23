package connection.producer;

import java.sql.Connection;
import java.sql.SQLException;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.annotation.Name;

@RunWith(Arquillian.class)
public class ConnectionProducerWithoutJndiTest {

	private static String PATH = "src/test/resources/producer/without-jndi";

	@Inject
	@Name("conn1")
	private Instance<Connection> conn1;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(ConnectionProducerWithoutJndiTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		return deployment;
	}

	@Test(expected=Exception.class)
	public void createConnection() throws SQLException {
		conn1.get();
	}

}