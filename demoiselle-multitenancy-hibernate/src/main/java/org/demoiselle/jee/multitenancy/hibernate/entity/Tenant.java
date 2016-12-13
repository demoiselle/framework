/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.entity;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.demoiselle.jee.multitenancy.hibernate.message.DemoiselleMultitenancyMessage;
import org.jose4j.json.JsonUtil;

/**
 * Basic Entity to store Tenants in database.
 * 
 * @author SERPRO
 *
 */
@Entity
@NamedQueries({ @NamedQuery(name = "Tenant.findByName", query = "SELECT t FROM Tenant t WHERE t.name = :name") })
public class Tenant implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(nullable = false)
	private Long id;

	@Size(max = 100, min = 2)
	@Column(length = 100, unique = true, updatable = false, nullable = false)
	@NotNull
	private String name;

	@Size(max = 50, min = 2)
	@Column(length = 50, updatable = true, nullable = false)
	private String databaseAppVersion;

	@Column(columnDefinition = "TEXT", updatable = true, nullable = true)
	private String configuration;

	@Transient
	private Logger logger;

	@Transient
	private DemoiselleMultitenancyMessage multitenancyMessages;

	public Tenant() {
	}

	public Tenant(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDatabaseAppVersion() {
		return databaseAppVersion;
	}

	public void setDatabaseAppVersion(String databaseAppVersion) {
		this.databaseAppVersion = databaseAppVersion;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public Map<String, Object> getMappedConfiguration() {

		Map<String, Object> map = null;

		try {
			if (configuration != null)
				map = JsonUtil.parseJson(configuration);
		} catch (Exception e) {
			// Ignore parsing error
			getLogger().warning(getMessage().logWarnErrorWhenParseConfigurationTenant());
		}

		return map;
	}

	@Override
	public boolean equals(Object object) {
		Tenant other = (Tenant) object;
		if (!this.name.equals(other.name)) {
			return false;
		}
		return true;
	}

	private DemoiselleMultitenancyMessage getMessage() {
		if (this.multitenancyMessages == null) {
			this.multitenancyMessages = CDI.current().select(DemoiselleMultitenancyMessage.class).get();
		}
		return this.multitenancyMessages;
	}

	private Logger getLogger() {
		if (this.logger == null) {
			this.logger = CDI.current().select(Logger.class).get();
		}
		return this.logger;
	}

}
