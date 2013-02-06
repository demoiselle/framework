package br.gov.frameworkdemoiselle.internal.producer;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.internal.configuration.DataSourceConfig;
import br.gov.frameworkdemoiselle.util.Beans;

@ApplicationScoped
public class DataSourceProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient DataSource dataSource;

	@Produces
	public DataSource create() {
		if (this.dataSource == null) {
			this.dataSource = init();
		}

		return this.dataSource;
	}

	private DataSource init() {
		DataSource result;

		try {
			DataSourceConfig config = Beans.getReference(DataSourceConfig.class);
			String jndi = config.getJndiName();

			// TODO Lançar exceção caso o JNDI esteja vazio ou nulo.

			Context context = new InitialContext();
			result = (DataSource) context.lookup(jndi);

		} catch (Exception cause) {
			// TODO Colocar uma mensagem amigável

			throw new ConfigurationException("", cause);
		}

		return result;
	}
}
