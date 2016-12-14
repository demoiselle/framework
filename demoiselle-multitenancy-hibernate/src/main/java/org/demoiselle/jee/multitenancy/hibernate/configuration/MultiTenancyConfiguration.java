/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.configuration;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * Class with ALL required configurations for Multitenancy module in Framework
 * Demoiselle.
 * 
 * @author SERPRO
 *
 */
@Configuration
public class MultiTenancyConfiguration {

	private String multiTenancySetDatabaseSQL;

	private String multiTenancyCreateDatabaseSQL;

	private String multiTenancyDropDatabaseSQL;

	private String multiTenancyTenantDatabasePrefix;

	private String multiTenancyTenantsDatabaseDatasource;

	private String multiTenancyMasterDatabaseDatasource;

	private String multiTenancyCreateDatabaseDDL;

	private String multiTenancyDropDatabaseDDL;

	private String multiTenancyMasterDatabase;

	/**
	 * Get the SQL command used to select database in SGBD type.
	 * 
	 * @return value of configuration
	 */
	public String getMultiTenancySetDatabaseSQL() {
		return multiTenancySetDatabaseSQL;
	}

	/**
	 * Get the SQL command used to create database in SGBD type.
	 * 
	 * @return value of configuration
	 */
	public String getMultiTenancyCreateDatabaseSQL() {
		return multiTenancyCreateDatabaseSQL;
	}

	/**
	 * Get the SQL command used to drop database in SGBD type.
	 * 
	 * @return value of configuration
	 */
	public String getMultiTenancyDropDatabaseSQL() {
		return multiTenancyDropDatabaseSQL;
	}

	/**
	 * Get prefix to use in Tenant database creation.
	 * 
	 * @return value of configuration
	 */
	public String getMultiTenancyTenantDatabasePrefix() {
		return multiTenancyTenantDatabasePrefix;
	}

	/**
	 * 
	 * Get the web application Datasource (e.g.:
	 * java:jboss/datasources/UserTenantsDS) to use in Tenants databases.
	 * 
	 * @return value of configuration
	 */
	public String getMultiTenancyTenantsDatabaseDatasource() {
		return multiTenancyTenantsDatabaseDatasource;
	}

	/**
	 * * Get the web application Datasource (e.g.:
	 * java:jboss/datasources/UserMasterDS) to use in Master database.
	 * 
	 * @return value of configuration
	 */
	public String getMultiTenancyMasterDatabaseDatasource() {
		return multiTenancyMasterDatabaseDatasource;
	}

	/**
	 * Get the file path that contains the DDL to create database of Tenant.
	 * 
	 * @return value of configuration
	 */
	public String getMultiTenancyCreateDatabaseDDL() {
		return multiTenancyCreateDatabaseDDL;
	}

	/**
	 * Get the file path that contains the DDL to drop database of Tenant.
	 * 
	 * @return value of configuration
	 */
	public String getMultiTenancyDropDatabaseDDL() {
		return multiTenancyDropDatabaseDDL;
	}

	/**
	 * Get the name of Master database of Tenants.
	 * 
	 * @return value of configuration
	 */
	public String getMultiTenancyMasterDatabase() {
		return multiTenancyMasterDatabase;
	}

}