/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.multitenancy.hibernate.dao.context;

import javax.persistence.EntityManager;

/**
 * This interface need to be implemented in aplication to indicate the
 * EntityManager to be used in @TenantManager
 * 
 * @author SERPRO
 *
 */
public interface EntityManagerMaster {

	EntityManager getEntityManager();

}