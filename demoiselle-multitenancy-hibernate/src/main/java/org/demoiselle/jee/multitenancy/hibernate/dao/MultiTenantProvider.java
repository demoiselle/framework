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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.demoiselle.jee.multitenancy.hibernate.exception.DemoiselleMultiTenancyException;
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

	// Load resource bundle manually because the @Inject dont enable yet
	private ResourceBundle config = ResourceBundle.getBundle("demoiselle");

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

	/**
	 * Get connection for Tenant using configurations.
	 */
	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		final Connection connection = getAnyConnection();
		try {
			String prefix = config.getString("demoiselle.multiTenancyTenantDatabasePrefix");
			String setDatabase = config.getString("demoiselle.multiTenancySetDatabaseSQL");
			String masterDatabase = config.getString("demoiselle.multiTenancyMasterDatabase");
			String finalDatabaseName = prefix + "" + tenantIdentifier;

			// If the master database name equals a tenantIdentifier dont concat
			// prefix
			if (masterDatabase.equals(tenantIdentifier)) {
				finalDatabaseName = tenantIdentifier;
			}

			connection.createStatement().execute(setDatabase + " " + finalDatabaseName);
		} catch (final SQLException e) {
			throw new DemoiselleMultiTenancyException("Error trying to alter schema [" + tenantIdentifier + "]", e);
		}
		return connection;
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		// Close JDBC connection
		connection.close();
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		releaseAnyConnection(connection);
	}

}