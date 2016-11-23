/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.entity;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Tenant implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(nullable = false)
	private Integer id;

	@Size(max = 100, min = 2)
	@Column(length = 100, unique = true, updatable = false, nullable = false)
	@NotNull
	private String name;

	@Size(max = 50, min = 2)
	@Column(length = 50, updatable = true, nullable = false)
	private String databaseAppVersion;

	public Tenant() {
	}

	public Tenant(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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

	@Override
	public boolean equals(Object object) {
		Tenant other = (Tenant) object;
		if (!this.name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
