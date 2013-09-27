package br.gov.frameworkdemoiselle.internal.producer;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;


public interface EntityManagerStore extends Serializable {
	
	/**
	 * Run this to initialize all persistence units. It's recomended this method
	 * be annotated with {@link PostConstruct}, so it runs as soon as an EntityManager gets injected.
	 */
	public abstract void initialize();
	
	/**
	 * Run this to close all persistence units. It's recomended this method
	 * be annotated with {@link PreDestroy}, so it runs as soon as the scope the EntityManager is
	 * attached to ends.
	 */
	public abstract void terminate();
	
	Map<String, EntityManager> getCache();
	
	public EntityManager getEntityManager(String persistenceUnit);

}
