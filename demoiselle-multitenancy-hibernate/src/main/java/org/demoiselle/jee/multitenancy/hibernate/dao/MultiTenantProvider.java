package org.demoiselle.jee.multitenancy.hibernate.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.enterprise.context.RequestScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;

@RequestScoped
public class MultiTenantProvider implements MultiTenantConnectionProvider, ServiceRegistryAwareService {

	private static final long serialVersionUID = 1L;
	private DataSource dataSource;

	// Carregamento do Resource Bundle manualmente por causa da falta de
	// possibilidade de injeção
	private ResourceBundle config = ResourceBundle.getBundle("demoiselle");

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

	@Override
	public void injectServices(ServiceRegistryImplementor serviceRegistry) {
		try {
			final Context init = new InitialContext();
			dataSource = (DataSource) init.lookup(config.getString("demoiselle.multiTenancyTenantsDatabaseDatasource"));
		} catch (final NamingException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isUnwrappableAs(Class clazz) {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
		return null;
	}

	@Override
	public Connection getAnyConnection() throws SQLException {
		final Connection connection = dataSource.getConnection();
		return connection;
	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		final Connection connection = getAnyConnection();
		try {
			connection.createStatement()
					.execute(config.getString("demoiselle.multiTenancySetDatabaseSQL") + " " + tenantIdentifier);
		} catch (final SQLException e) {
			throw new HibernateException("Error trying to alter schema [" + tenantIdentifier + "]", e);
		}
		return connection;
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {	
		connection.close();
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		releaseAnyConnection(connection);
	}
}