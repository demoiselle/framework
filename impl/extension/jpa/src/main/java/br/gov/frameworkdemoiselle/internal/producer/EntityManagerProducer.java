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
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig;
import br.gov.frameworkdemoiselle.internal.proxy.EntityManagerProxy;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * <p>
 * Factory class responsible to produces instances of EntityManager. Produces instances based on informations defined in
 * persistence.xml, demoiselle.properties or @PersistenceUnit annotation.
 * </p>
 */
public class EntityManagerProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	@Name("demoiselle-jpa-bundle")
	private ResourceBundle bundle;

	private final Map<String, EntityManager> cache = Collections.synchronizedMap(new HashMap<String, EntityManager>());

	@Inject
	private EntityManagerFactoryProducer factory;

	/**
	 * <p>
	 * Default EntityManager factory. Tries two strategies to produces EntityManager instances.
	 * <li>The first one is based on informations available on demoiselle properties file
	 * ("frameworkdemoiselle.persistence.unit.name" key).</li>
	 * <li>The second one is based on persistence.xml file. If exists only one Persistence Unit defined, this one is
	 * used.</li>
	 * 
	 * @param config
	 *            Suplies informations about EntityManager defined in properties file.
	 * @return Produced EntityManager.
	 */
	@Default
	@Produces
	public EntityManager createDefault(InjectionPoint ip, EntityManagerConfig config) {
		String persistenceUnit = getFromProperties(config);

		if (persistenceUnit == null) {
			persistenceUnit = getFromXML();
		}

		return new EntityManagerProxy(persistenceUnit);
	}

	@Name("")
	@Produces
	public EntityManager createNamed(InjectionPoint ip, EntityManagerConfig config) {
		String persistenceUnit = ip.getAnnotated().getAnnotation(Name.class).value();
		return new EntityManagerProxy(persistenceUnit);
	}

	public EntityManager getEntityManager(String persistenceUnit) {
		EntityManager entityManager = null;

		if (cache.containsKey(persistenceUnit)) {
			entityManager = cache.get(persistenceUnit);

		} else {
			entityManager = factory.create(persistenceUnit).createEntityManager();
			entityManager.setFlushMode(FlushModeType.AUTO);

			cache.put(persistenceUnit, entityManager);
			this.logger.info(bundle.getString("entity-manager-was-created", persistenceUnit));
		}

		return entityManager;
	}

	/**
	 * Tries to get persistence unit name from demoiselle.properties.
	 * 
	 * @param config
	 *            Configuration containing persistence unit name.
	 * @return Persistence unit name.
	 */
	private String getFromProperties(EntityManagerConfig config) {
		String persistenceUnit = config.getDefaultPersistenceUnitName();

		if (persistenceUnit != null) {
			this.logger.debug(bundle.getString("getting-persistence-unit-from-properties",
					Configuration.DEFAULT_RESOURCE));
		}

		return persistenceUnit;
	}

	/**
	 * Uses persistence.xml to get informations about which persistence unit to use. Throws DemoiselleException if more
	 * than one Persistence Unit is defined.
	 * 
	 * @return Persistence Unit Name
	 */
	private String getFromXML() {
		Set<String> persistenceUnits = factory.getCache().keySet();

		if (persistenceUnits.size() > 1) {
			throw new DemoiselleException(bundle.getString("more-than-one-persistence-unit-defined",
					Name.class.getSimpleName()));
		} else {
			return persistenceUnits.iterator().next();
		}
	}

	@PostConstruct
	public void init() {
		for (String persistenceUnit : factory.getCache().keySet()) {
			getEntityManager(persistenceUnit);
		}
	}

	@PreDestroy
	public void close() {
		for (EntityManager entityManager : cache.values()) {
			entityManager.close();
		}

		cache.clear();
	}

	public Map<String, EntityManager> getCache() {
		return cache;
	}
}
