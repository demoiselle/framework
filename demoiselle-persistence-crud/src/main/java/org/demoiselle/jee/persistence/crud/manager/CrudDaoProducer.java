package org.demoiselle.jee.persistence.crud.manager;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class CrudDaoProducer {
	
	//@Produces
	@Dependent
	public CrudDAO createDAO(InjectionPoint ip){
		return null;
	}

}
