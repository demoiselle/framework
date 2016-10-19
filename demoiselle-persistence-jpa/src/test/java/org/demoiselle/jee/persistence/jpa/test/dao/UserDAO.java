package org.demoiselle.jee.persistence.jpa.test.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.demoiselle.jee.persistence.jpa.crud.GenericCrudDAO;
import org.demoiselle.jee.persistence.jpa.entity.User;

public class UserDAO extends GenericCrudDAO<User>  {

	@PersistenceContext
	EntityManager entityManager;
	
	public UserDAO() {
		super(User.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}

}
