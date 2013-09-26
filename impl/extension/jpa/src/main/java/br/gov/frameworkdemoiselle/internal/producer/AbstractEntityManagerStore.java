/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.internal.producer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig.EntityManagerScope;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * 
 * <p>Stores produced entity managers. When the {@link EntityManagerProducer} try to create an entity manager it will
 * seach this store for a cached instance, only creating a new instance if this cache doesn't contain a suitable one.</p>
 * 
 * <p>There are several concrete implementations of this class, each one corresponding to a scoped cache (ex: {@link RequestEntityManagerStore}
 * stores Entity Managers on the request scope). To select witch implementation is used (and with that, what scope is used to store Entity Managers)
 * open the "demoiselle.properties" file and edit the property "frameworkdemoiselle.persistence.entitymanager.scope". The default scope is the
 * {@link RequestScoped}.</p>
 * 
 * 
 * @author serpro
 *
 */
public abstract class AbstractEntityManagerStore implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final Map<String, EntityManager> cache = Collections.synchronizedMap(new HashMap<String, EntityManager>());
	
	@Inject
	private EntityManagerFactoryProducer factory;
	
	@Inject
	private Logger logger;

	@Inject
	@Name("demoiselle-jpa-bundle")
	private ResourceBundle bundle;
	
	@Inject
	private EntityManagerConfig configuration;
	
	EntityManager getEntityManager(String persistenceUnit) {
		EntityManager entityManager = null;

		if (cache.containsKey(persistenceUnit)) {
			entityManager = cache.get(persistenceUnit);

		} else {
			entityManager = getFactory().create(persistenceUnit).createEntityManager();
			entityManager.setFlushMode(FlushModeType.AUTO);

			cache.put(persistenceUnit, entityManager);
			this.getLogger().info(getBundle().getString("entity-manager-was-created", persistenceUnit));
		}

		return entityManager;
	}
	
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
	
	void init() {
		for (String persistenceUnit : getFactory().getCache().keySet()) {
			getEntityManager(persistenceUnit);
		}
	}

	void close() {
		//Se o produtor não possui escopo, então o ciclo de vida
		//de EntityManager produzidos é responsabilidade do desenvolvedor. Não
		//fechamos os EntityManagers aqui.
		if (configuration.getEntityManagerScope() != EntityManagerScope.NOSCOPE){
			for (EntityManager entityManager : cache.values()) {
				entityManager.close();
			}
		}
		cache.clear();
	}

	Map<String, EntityManager> getCache() {
		return cache;
	}
	
	private EntityManagerFactoryProducer getFactory(){
		if (factory==null){
			factory = Beans.getReference(EntityManagerFactoryProducer.class);
		}
		return factory;
	}
	
	protected Logger getLogger(){
		if (logger==null){
			logger = Beans.getReference(Logger.class);
		}
		return logger;
	}
	
	protected ResourceBundle getBundle(){
		if (bundle==null){
			bundle = Beans.getReference(ResourceBundle.class , new NameQualifier("demoiselle-jpa-bundle"));
		}
		return bundle;
	}
}
