/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.business;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.demoiselle.jee.core.message.DemoiselleMessage;
import org.demoiselle.jee.multitenancy.hibernate.dao.TenantDAO;
import org.demoiselle.jee.multitenancy.hibernate.entity.Tenant;
import org.demoiselle.jee.persistence.crud.AbstractBusiness;

@Stateless
public class TenantBC extends AbstractBusiness<Tenant, Long> {

	@Inject
	private TenantDAO dao;

	@Inject
	private DemoiselleMessage messages;

	public String getTenantName() {
		return dao.getMultiTenantContext().getTenant().getName();
	}

	public Tenant persist(Tenant entity) {
		entity.setDatabaseAppVersion(messages.version());
		return dao.persist(entity);
	}

}
