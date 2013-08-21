package connection.producer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.internal.configuration.JDBCConfig;

@RunWith(Arquillian.class)
public class ConnectionProducerDefaultTestWithName {

	@Inject
	private JDBCConfig config;
	
	private static String PATH = "src/test/resources/producer";
	
	@Inject
	private Connection connection;
	
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(ConnectionProducerDefaultTestWithName.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/default-producer-with-name.properties"), "default-producer-with-name.properties");
		return deployment;
	}
	
	@Test
	public void createDefaultConnectionWithoutName(){
		try {
			assertNotNull(connection);
			//verificar se retorna realmente o nome da conexão
			assertEquals(connection.getCatalog(), config.getDefaultDataSourceName());
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}
}
