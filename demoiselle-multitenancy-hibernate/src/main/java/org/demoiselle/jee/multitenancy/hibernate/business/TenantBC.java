/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.business;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

import org.demoiselle.jee.core.message.DemoiselleMessage;
import org.demoiselle.jee.multitenancy.hibernate.dao.TenantDAO;
import org.demoiselle.jee.multitenancy.hibernate.entity.Tenant;

@Stateless
public class TenantBC {

	@Inject
	private TenantDAO dao;

	@Inject
	private DemoiselleMessage messages;

	@PersistenceContext(unitName = "MasterPU")
	protected EntityManager entityManagerMaster;

	protected TenantDAO getPersistenceDAO() {
		return dao;
	}

	public List<Tenant> listAllTenants() {
		List<Tenant> tenants = entityManagerMaster
				.createQuery("select u from " + Tenant.class.getSimpleName() + " u", Tenant.class).getResultList();
		return tenants;
	}

	@Transactional
	public void create(Tenant entity) {
		entity.setDatabaseAppVersion(messages.version());
		getPersistenceDAO().create(entity);
	}

	@Transactional
	public void edit(int id, Tenant entity) {
		try {
			// é necessário setar o ID do objeto para que ele possa ser alterado
			// esta ação é feita por reflexão por não termos o tipo do objeto
			Method m = entity.getClass().getMethod("setId", new Class[] { Integer.class });
			m.invoke(entity, id);
		} catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
		}

		getPersistenceDAO().edit(entity);
	}

	@Transactional
	public void remove(Object id) {
		Tenant obj = find(id);
		getPersistenceDAO().remove(obj);
	}

	public Tenant find(Object id) {
		Tenant obj = getPersistenceDAO().find(id);
		if (obj == null) {
			throw new NotFoundException(messages.keyNotFound(id.toString()));
		}
		return obj;
	}

	public List<Tenant> findAll() {
		return getPersistenceDAO().findAll();
	}

	public List<Tenant> findRange(int[] range) {
		return getPersistenceDAO().findRange(range);
	}	

	public String getTenantName() {
		return getPersistenceDAO().getMultiTenantContext().getTenant().getName();
	}

}
