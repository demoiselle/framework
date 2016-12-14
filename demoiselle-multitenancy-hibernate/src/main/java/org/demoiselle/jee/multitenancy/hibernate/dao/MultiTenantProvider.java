/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.demoiselle.jee.multitenancy.hibernate.configuration.MultiTenancyConfiguration;
import org.demoiselle.jee.multitenancy.hibernate.exception.DemoiselleMultiTenancyException;
import org.demoiselle.jee.multitenancy.hibernate.message.DemoiselleMultitenancyMessage;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * Implementation of @MultiTenantConnectionProvider in Hibernate.
 * 
 * @author SERPRO
 *
 */
@RequestScoped
public class MultiTenantProvider implements MultiTenantConnectionProvider, ServiceRegistryAwareService {

	private static final long serialVersionUID = 1L;
	private DataSource dataSource;

	private MultiTenancyConfiguration configuration;

	private DemoiselleMultitenancyMessage messages;

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

	/**
	 * Instance Datasource for manipulate Tenants on Server Startup
	 */
	@Override
	public void injectServices(ServiceRegistryImplementor serviceRegistry) {
		try {
			// Load messages manually because the @Inject dont enable yet
			ResourceBundle configBundle = ResourceBundle.getBundle("demoiselle");

			// Create context to create DataSource manually
			final Context init = new InitialContext();
			dataSource = (DataSource) init
					.lookup(configBundle.getString("demoiselle.multiTenancyTenantsDatabaseDatasource"));// configuration.getMultiTenancyTenantsDatabaseDatasource());
		} catch (final NamingException e) {
			throw new DemoiselleMultiTenancyException(e);
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

	/**
	 * Get connection for Tenant using configurations.
	 */
	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		final Connection connection = getAnyConnection();
		try {
			String prefix = getConfiguration().getMultiTenancyTenantDatabasePrefix();
			String setDatabase = getConfiguration().getMultiTenancySetDatabaseSQL();
			String masterDatabase = getConfiguration().getMultiTenancyMasterDatabase();
			String finalDatabaseName = prefix + "" + tenantIdentifier;

			// If the master database name equals a tenantIdentifier dont concat
			// prefix
			if (masterDatabase.equals(tenantIdentifier)) {
				finalDatabaseName = tenantIdentifier;
			}

			connection.createStatement().execute(setDatabase + " " + finalDatabaseName);
		} catch (final SQLException e) {
			throw new DemoiselleMultiTenancyException(getMessage().errorSetSchema(tenantIdentifier), e);
		}
		return connection;
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		// Close JDBC connectionMessage
		connection.close();
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		releaseAnyConnection(connection);
	}

	/**
	 * Method to help to get messages class in CDI context.
	 * 
	 * @return Multitenancy messages
	 */
	private DemoiselleMultitenancyMessage getMessage() {
		if (this.messages == null) {
			this.messages = CDI.current().select(DemoiselleMultitenancyMessage.class).get();
		}
		return this.messages;
	}

	/**
	 * Method to help to get configuration class in CDI context.
	 * 
	 * @return Multitenancy configuration
	 */
	private MultiTenancyConfiguration getConfiguration() {
		if (this.configuration == null) {
			this.configuration = CDI.current().select(MultiTenancyConfiguration.class).get();
		}
		return this.configuration;
	}

}