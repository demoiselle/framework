/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.dao;

import javax.inject.Inject;

import org.demoiselle.jee.multitenancy.hibernate.context.MultiTenantContext;
import org.demoiselle.jee.multitenancy.hibernate.dao.context.PersistenceContextMasterDAO;
import org.demoiselle.jee.multitenancy.hibernate.entity.Tenant;


public class TenantDAO extends PersistenceContextMasterDAO<Tenant> {

	@Inject
	private MultiTenantContext multiTenantContext;

	/**
	 * O Contrutor desta classe precisa ser sem parâmetros por causa do CDI.
	 */
	public TenantDAO() {
		super(Tenant.class);
	}

	public MultiTenantContext getMultiTenantContext() {
		return multiTenantContext;
	}

}
