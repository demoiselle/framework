package connection.producer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.Test;

import br.gov.frameworkdemoiselle.internal.configuration.JDBCConfig;


public class ConnectionProducerDefaultTestWithoutName {

	@Inject
	private JDBCConfig config;
	
	private String PATH = "src/test/resources/producer";
	
	@Inject
	private Connection connection;
	
	@Test
	public void createDefaultConnectionWithoutName(){
		//utilizar o arquivo de propriedade sem o name
		try {
			assertNotNull(connection);
			//verificar se retorna realmente o nome da conexão
			assertNotNull(connection.getCatalog());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
	}
}
