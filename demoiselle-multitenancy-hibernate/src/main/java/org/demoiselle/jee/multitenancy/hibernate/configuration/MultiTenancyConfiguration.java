package org.demoiselle.jee.multitenancy.hibernate.configuration;

import org.demoiselle.jee.configuration.annotation.Configuration;

@Configuration
public class MultiTenancyConfiguration {

	private String multiTenancySetDatabaseSQL;

	private String multiTenancyCreateDatabaseSQL;

	private String multiTenancyDropDatabaseSQL;

	private String multiTenancyTenantsDatabaseDatasource;

	private String multiTenancyMasterDatabaseDatasource;

	private String multiTenancyCreateDatabaseDDL;

	private String multiTenancyDropDatabaseDDL;

	private String multiTenancyMasterDatabase;
	
	public String getMultiTenancySetDatabaseSQL() {
		return multiTenancySetDatabaseSQL;
	}

	public String getMultiTenancyCreateDatabaseSQL() {
		return multiTenancyCreateDatabaseSQL;
	}

	public String getMultiTenancyDropDatabaseSQL() {
		return multiTenancyDropDatabaseSQL;
	}

	public String getMultiTenancyTenantsDatabaseDatasource() {
		return multiTenancyTenantsDatabaseDatasource;
	}

	public String getMultiTenancyMasterDatabaseDatasource() {
		return multiTenancyMasterDatabaseDatasource;
	}

	public String getMultiTenancyCreateDatabaseDDL() {
		return multiTenancyCreateDatabaseDDL;
	}

	public String getMultiTenancyDropDatabaseDDL() {
		return multiTenancyDropDatabaseDDL;
	}

	public String getMultiTenancyMasterDatabase() {
		return multiTenancyMasterDatabase;
	}

}