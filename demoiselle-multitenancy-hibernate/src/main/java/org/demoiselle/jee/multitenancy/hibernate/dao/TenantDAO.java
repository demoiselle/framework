/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.demoiselle.jee.multitenancy.hibernate.context.MultiTenantContext;
import org.demoiselle.jee.multitenancy.hibernate.dao.context.EntityManagerMaster;
import org.demoiselle.jee.multitenancy.hibernate.entity.Tenant;
import org.demoiselle.jee.persistence.crud.AbstractDAO;

public class TenantDAO extends AbstractDAO<Tenant, Long> {

	@Inject
	private MultiTenantContext multiTenantContext;

	@Inject
	private EntityManagerMaster entityManagerMaster;

	protected EntityManager getEntityManager() {
		return entityManagerMaster.getEntityManager();
	}

	/**
	 * O Contrutor desta classe precisa ser sem parâmetros por causa do CDI.
	 */
	public TenantDAO() {

	}

	public MultiTenantContext getMultiTenantContext() {
		return multiTenantContext;
	}

}
