package connection.producer;

import java.sql.Connection;

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
public class ConnectionProducerNoConnectionUrlTest {

	private static String PATH = "src/test/resources/producer/no-connection-url";

	@Inject
	@Name("conn1")
	private Instance<Connection> conn1;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(ConnectionProducerNoConnectionUrlTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		return deployment;
	}

	/**
	 * TODO Refinar a exceção esperada 
	 */
	@Test(expected = Exception.class)
	public void failOnCreateConnection() {
		conn1.get();
	}

}
