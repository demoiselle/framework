package org.demoiselle.jee.multitenancy.hibernate.dao.context;

import javax.persistence.EntityManager;

public interface EntityManagerMaster {

	EntityManager getEntityManager();

}