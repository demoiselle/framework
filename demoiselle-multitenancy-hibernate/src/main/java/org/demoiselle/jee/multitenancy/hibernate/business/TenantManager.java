/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.business;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.demoiselle.jee.core.message.DemoiselleMessage;
import org.demoiselle.jee.multitenancy.hibernate.configuration.MultiTenancyConfiguration;
import org.demoiselle.jee.multitenancy.hibernate.dao.TenantDAO;
import org.demoiselle.jee.multitenancy.hibernate.entity.Tenant;
import org.demoiselle.jee.multitenancy.hibernate.exception.DemoiselleMultiTenancyException;
import org.demoiselle.jee.persistence.crud.ResultSet;

/**
 * Classe with behaviors to manipulate basic Tenants operations.
 * 
 * @author SERPRO
 *
 */
@Stateless
public class TenantManager {

	@Inject
	private TenantDAO dao;

	@Inject
	private DemoiselleMessage messages;

	private DataSource dataSource;

	@Inject
	private MultiTenancyConfiguration configuration;

	/**
	 * Get tenant name in @MultiTenantContext
	 * 
	 * @return Tenant name
	 */
	public String getTenantName() {
		return dao.getMultiTenantContext().getTenant().getName();
	}

	/**
	 * Simple find @Tenant by id
	 * 
	 * @param id
	 *            Id of tenant
	 * @return Tenant entity
	 */
	public Tenant find(Long id) {
		return dao.find(id);
	}

	/**
	 * Simple find ALL Tenants.
	 * 
	 * @return List of @Tenant in @ResultSet
	 */
	public ResultSet find() {
		return dao.find();
	}

	/**
	 * Simple persist Tenant without Business rules.
	 * 
	 * @param tenant
	 *            Teannt Entity to create
	 * @return Created Tenant
	 */
	public Tenant persist(Tenant tenant) {
		tenant.setDatabaseAppVersion(messages.version());
		return dao.persist(tenant);
	}

	/**
	 * Creates a new Tenant with all required business rules.
	 * 
	 * @param tenant
	 *            The tenant to create
	 * @throws NamingException
	 *             When lookup is wrong
	 * @throws SQLException
	 *             When SQL to create or set databse has error
	 */
	public void createTenant(Tenant tenant) throws NamingException, SQLException {

		Connection conn = null;

		// Infos of Config
		String prefix = configuration.getMultiTenancyTenantDatabasePrefix();
		String createCommand = configuration.getMultiTenancyCreateDatabaseSQL();
		String setCommand = configuration.getMultiTenancySetDatabaseSQL();
		String masterDatabase = configuration.getMultiTenancyMasterDatabase();

		try {

			// Add Tenancy in table/master schema
			persist(tenant);

			// Create Schema
			final Context init = new InitialContext();
			dataSource = (DataSource) init.lookup(configuration.getMultiTenancyMasterDatabaseDatasource());

			conn = dataSource.getConnection();

			// Create the database for tenant
			conn.createStatement().execute(createCommand + " " + prefix + "" + tenant.getName());

			// Set USE database
			conn.createStatement().execute(setCommand + " " + prefix + "" + tenant.getName());

			// Run o DDL - DROP
			try {
				dropDatabase(conn);
			} catch (Exception e) {
				// Ignore erros, because maybe the table already not exists!
			}

			// Run o DDL - CREATE
			createDatabase(conn);

		} catch (IOException e) {
			throw new DemoiselleMultiTenancyException(e);
		} finally {
			// Set master database
			conn.createStatement().execute(setCommand + " " + masterDatabase);

			// Closes the connection
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}

	}

	/**
	 * Business deletion of Tenants
	 * 
	 * @param id
	 *            ID of Tenant
	 * @throws SQLException
	 *             When SQL to create or set databse has error
	 */
	public void removeTenant(Long id) throws SQLException {

		Connection conn = null;

		// Infos of Config
		String setCommand = configuration.getMultiTenancySetDatabaseSQL();
		String masterDatabase = configuration.getMultiTenancyMasterDatabase();

		try {
			// Add Tenancy in table/master schema
			Tenant t = dao.find(id);
			dao.remove(t.getId());

			final Context init = new InitialContext();
			dataSource = (DataSource) init.lookup(configuration.getMultiTenancyMasterDatabaseDatasource());

			String prefix = configuration.getMultiTenancyTenantDatabasePrefix();
			String dropCommand = configuration.getMultiTenancyDropDatabaseSQL();
			conn = dataSource.getConnection();

			// Delete database
			conn.createStatement().execute(dropCommand + " " + prefix + "" + t.getName());

		} catch (Exception e) {
			throw new DemoiselleMultiTenancyException(e);
		} finally {
			// Set master database
			conn.createStatement().execute(setCommand + " " + masterDatabase);

			// Closes the connection
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		}

	}

	/**
	 * Execute SQL for DROP DATABASE of @Tenant.
	 * 
	 * @param conn
	 *            Database Connection
	 * @throws SQLException
	 *             When SQL has error
	 * @throws IOException
	 *             Error with DDL file
	 */
	private void dropDatabase(Connection conn) throws SQLException, IOException {
		String filename = configuration.getMultiTenancyDropDatabaseDDL();
		List<String> ddl = getDDLString(filename);
		for (String ddlLine : ddl) {
			conn.createStatement().execute(ddlLine);
		}
	}

	/**
	 * Execute SQL for CREATE DATABASE of @Tenant.
	 * 
	 * @param conn
	 * @throws SQLException
	 *             Error on execute SQL
	 * @throws IOException
	 *             Error with DDL file
	 * 
	 */
	private void createDatabase(Connection conn) throws SQLException, IOException {
		String filename = configuration.getMultiTenancyCreateDatabaseDDL();
		List<String> ddl = getDDLString(filename);
		for (String ddlLine : ddl) {
			conn.createStatement().execute(ddlLine);
		}
	}

	/**
	 * Return all lines of DDL file generated by Hibernate on startup.
	 * 
	 * @param filename
	 *            Name of DDL file
	 * @return All lines of SQL
	 * @throws IOException
	 *             Error with DDL file
	 */
	private List<String> getDDLString(String filename) throws IOException {
		List<String> records = new ArrayList<String>();

		FileReader f = new FileReader(filename);
		BufferedReader reader = new BufferedReader(f);
		String line;
		while ((line = reader.readLine()) != null) {
			records.add(line + ";");
		}
		reader.close();
		return records;

	}

}
