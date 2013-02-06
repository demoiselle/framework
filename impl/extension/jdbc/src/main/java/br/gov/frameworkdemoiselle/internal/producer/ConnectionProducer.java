package br.gov.frameworkdemoiselle.internal.producer;

import java.io.Serializable;
import java.sql.Connection;

import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.util.Beans;

@RequestScoped
public class ConnectionProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient Connection connection;

	@Produces
	public Connection create() {
		if (this.connection == null) {
			this.connection = init();
		}

		return this.connection;
	}

	private Connection init() {
		Connection result;

		try {
			DataSource dataSource = Beans.getReference(DataSource.class);
			result = dataSource.getConnection();

		} catch (Exception cause) {
			// TODO Colocar uma mensagem amigável

			throw new ConfigurationException("", cause);
		}

		return result;
	}

	@PreDestroy
	public void close() {
		if (this.connection != null) {

			try {
				if (this.connection.isClosed()) {
					// TODO Logar um warning informando que a conexão já havia sido finalizada.

				} else {
					this.connection.close();
					// TODO Logar um info informando que a conexão foi finalizada.
				}

			} catch (Exception cause) {
				// TODO Colocar uma mensagem amigável

				throw new ConfigurationException("", cause);
			}
		}
	}
}
