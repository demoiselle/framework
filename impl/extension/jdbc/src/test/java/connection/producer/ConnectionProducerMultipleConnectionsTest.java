package connection.producer;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.annotation.Name;

@RunWith(Arquillian.class)
public class ConnectionProducerMultipleConnectionsTest {

	private static String PATH = "src/test/resources/producer/multiple-connections";

	@Inject
	@Name("conn1")
	private Connection conn1;

	@Inject
	@Name("conn2")
	private Connection conn2;

	// Conexão Default
	@Inject
	private Connection conn3;

	@Inject
	@Name("conn4")
	private Connection conn4;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(ConnectionProducerMultipleConnectionsTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		return deployment;
	}

	@Test
	public void createConnection() throws SQLException {
		assertEquals(conn1.getMetaData().getURL(), "jdbc:hsqldb:hsql1");
		assertEquals(conn2.getMetaData().getURL(), "jdbc:hsqldb:hsql2");
		assertEquals(conn3.getMetaData().getURL(), "jdbc:hsqldb:hsql3");
		assertEquals(conn4.getMetaData().getURL(), "jdbc:derby:target/databases/derby");
	}

}
