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
import br.gov.frameworkdemoiselle.annotation.Name;

@RunWith(Arquillian.class)
public class ConnectionProducerTest {

	private static String PATH = "src/test/resources/producer";
	
	@Inject
	@Name("conn1")
	private Connection connectionWithName;
	
	@Inject
	private Connection connectionWithoutName;
	
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(ConnectionProducerTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		return deployment;
	}
	
	@Test
	public void createDefaultConnectionWithName(){
		assertNotNull(connectionWithName);
	}
	
	@Test
	public void createDefaultConnectionWithoutName(){
		assertNotNull(connectionWithoutName);
	}
}
