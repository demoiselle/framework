/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.business;

import java.io.BufferedReader;
import java.io.FileReader;
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

import org.demoiselle.jee.core.exception.DemoiselleException;
import org.demoiselle.jee.core.message.DemoiselleMessage;
import org.demoiselle.jee.multitenancy.hibernate.configuration.MultiTenancyConfiguration;
import org.demoiselle.jee.multitenancy.hibernate.dao.TenantDAO;
import org.demoiselle.jee.multitenancy.hibernate.entity.Tenant;
import org.demoiselle.jee.persistence.crud.ResultSet;

@Stateless
public class TenantBC {

	@Inject
	private TenantDAO dao;

	@Inject
	private DemoiselleMessage messages;

	private DataSource dataSource;

	@Inject
	private MultiTenancyConfiguration configuration;

	public String getTenantName() {
		return dao.getMultiTenantContext().getTenant().getName();
	}

	public Tenant find(Long id) {
		return dao.find(id);
	}

	public ResultSet find() {
		return dao.find();
	}

	public Tenant persist(Tenant entity) {
		entity.setDatabaseAppVersion(messages.version());
		return dao.persist(entity);
	}

	public void createTenant(Tenant tenant) throws NamingException, SQLException {

		// Infos
		String prefix = configuration.getMultiTenancyTenantDatabasePrefix();
		String createCommand = configuration.getMultiTenancyCreateDatabaseSQL();
		String setCommand = configuration.getMultiTenancySetDatabaseSQL();
		String masterDatabase = configuration.getMultiTenancyMasterDatabase();
		
		// Add Tenancy in table/master schema
		persist(tenant);

		// Create Schema
		final Context init = new InitialContext();
		dataSource = (DataSource) init.lookup(configuration.getMultiTenancyMasterDatabaseDatasource());

		Connection conn = dataSource.getConnection();

		// Cria o BANCO/SCHEMA
		conn.createStatement().execute(createCommand + " " + prefix + "" + tenant.getName());

		// Usa o BANCO/SCHEMA (MySQL)
		conn.createStatement().execute(setCommand + " " + prefix + "" + tenant.getName());

		// Roda o DDL - DROP
		dropDatabase(conn);

		// Roda o DDL - CREATE
		createDatabase(conn);
		
		// Set master database
		conn.createStatement().execute(setCommand + " " + masterDatabase);

		// Como a conexão esta fora de contexto é importante fechar ela aqui
		if (!conn.isClosed()) {
			conn.close();
		}

	}

	/**
	 * Business deletion of Tenants
	 * 
	 * @param id
	 *            ID of Tenant
	 */
	public void removeTenant(Long id) {
		try {
			// Add Tenancy in table/master schema
			Tenant t = dao.find(id);
			dao.remove(t.getId());

			final Context init = new InitialContext();
			dataSource = (DataSource) init.lookup(configuration.getMultiTenancyMasterDatabaseDatasource());

			String prefix = configuration.getMultiTenancyTenantDatabasePrefix();
			String dropCommand = configuration.getMultiTenancyDropDatabaseSQL();
			Connection conn = dataSource.getConnection();

			// EXCLUIR o BANCO/SCHEMA
			conn.createStatement().execute(dropCommand + " " + prefix + "" + t.getName());

			// Como a conexão esta fora de contexto é importante fechar ela aqui
			if (!conn.isClosed()) {
				conn.close();
			}
		} catch (Exception e) {
			throw new DemoiselleException(e);
		}
	}

	private void dropDatabase(Connection conn) throws SQLException {
		String filename = configuration.getMultiTenancyDropDatabaseDDL();
		List<String> ddl = getDDLString(filename);
		for (String ddlLine : ddl) {
			conn.createStatement().execute(ddlLine);
		}
	}

	private void createDatabase(Connection conn) throws SQLException {
		String filename = configuration.getMultiTenancyCreateDatabaseDDL();
		List<String> ddl = getDDLString(filename);
		for (String ddlLine : ddl) {
			conn.createStatement().execute(ddlLine);
		}
	}

	private List<String> getDDLString(String filename) {
		List<String> records = new ArrayList<String>();
		try {
			FileReader f = new FileReader(filename);
			BufferedReader reader = new BufferedReader(f);
			String line;
			while ((line = reader.readLine()) != null) {
				records.add(line);
			}
			reader.close();
			return records;
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", filename);
			return null;
		}
	}

}
