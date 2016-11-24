/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.dao.context;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.demoiselle.jee.persistence.crud.AbstractDAO;

public abstract class EntityManagerMasterDAO<T> extends AbstractDAO<T, Long> {

	@PersistenceContext(unitName = "MasterPU")
	protected EntityManager emEntity;

	@Override
	protected EntityManager getEntityManager() {
		return emEntity;
	}

}
