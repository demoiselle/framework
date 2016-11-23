/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.dao.context;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.demoiselle.jee.persistence.jpa.crud.GenericCrudDAO;

public abstract class PersistenceContextMasterDAO<T> extends GenericCrudDAO<T> {

	public PersistenceContextMasterDAO(Class<T> entityClass) {
		super(entityClass);
	}

	@PersistenceContext(unitName = "MasterPU")
	protected EntityManager emEntity;

	@Override
	protected EntityManager getEntityManager() {
		return emEntity;
	}

}
