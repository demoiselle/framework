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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@ApplicationScoped
// @StaticScoped
public class EntityManagerFactoryProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String ENTITY_MANAGER_RESOURCE = "META-INF/persistence.xml";
	
	@Inject
	protected Logger logger;

	@Inject
	@Name("demoiselle-jpa-bundle")
	protected ResourceBundle bundle;
	
	@Inject
	private Persistences persistenceUnitReader;

	private final Map<ClassLoader, Map<String, EntityManagerFactory>> factoryCache = Collections
			.synchronizedMap(new HashMap<ClassLoader, Map<String, EntityManagerFactory>>());
	
	@Default
	@Produces
	protected EntityManagerFactory createDefault(EntityManagerConfig config) {
		String persistenceUnit = persistenceUnitReader.getFromProperties(config);

		if (persistenceUnit == null) {
			persistenceUnit = persistenceUnitReader.getFromXML();
		}

		return create(persistenceUnit);
	}

	@Name("")
	@Produces
	protected EntityManagerFactory createNamed(InjectionPoint ip) {
		String persistenceUnit = ip.getAnnotated().getAnnotation(Name.class).value();
		return create(persistenceUnit);
	}
	
	public EntityManagerFactory create(String persistenceUnit) {
		EntityManagerFactory factory;

		ClassLoader c = Thread.currentThread().getContextClassLoader();

		if (factoryCache.containsKey(c)) {
			Map<String, EntityManagerFactory> localCache = factoryCache.get(c);
			if (localCache.containsKey(persistenceUnit)) {
				factory = localCache.get(persistenceUnit);
			} else {
				factory = Persistence.createEntityManagerFactory(persistenceUnit);
				localCache.put(persistenceUnit, factory);
			}
		} else {
			Map<String, EntityManagerFactory> localCache = new HashMap<String, EntityManagerFactory>();
			factory = Persistence.createEntityManagerFactory(persistenceUnit);
			localCache.put(persistenceUnit, factory);
			factoryCache.put(c, localCache);
		}

		return factory;
	}

	private String[] loadPersistenceUnitFromClassloader(ClassLoader classLoader) {
		try {
			ArrayList<String> persistenceUnits = new ArrayList<String>();
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = documentBuilder.parse(classLoader.getResourceAsStream(ENTITY_MANAGER_RESOURCE));
			NodeList nodes = document.getElementsByTagName("persistence-unit");

			String persistenceUnit = "";
			for (int index = 0; index < nodes.getLength(); index++) {
				Node node = nodes.item(index);
				persistenceUnit = ((Element) node).getAttribute("name");

				if ("".equals(persistenceUnit)) {
					throw new DemoiselleException(bundle.getString("can-not-get-persistence-unit-from-persistence"));
				} else {
					persistenceUnits.add(persistenceUnit);
				}
			}

			return persistenceUnits.toArray(new String[0]);

		} catch (Exception cause) {
			String message = bundle.getString("can-not-get-persistence-unit-from-persistence");
			logger.error(message, cause);

			throw new DemoiselleException(message, cause);
		}

	}

	@PostConstruct
	public void loadPersistenceUnits() {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		for (String persistenceUnit : loadPersistenceUnitFromClassloader(contextClassLoader)) {

			try {
				create(persistenceUnit);
			} catch (Exception cause) {
				throw new DemoiselleException(cause);
			}

			logger.debug(bundle.getString("persistence-unit-name-found", persistenceUnit));
		}
	}

	@PreDestroy
	public void close() {
		for (Map<String, EntityManagerFactory> factories : factoryCache.values()) {
			for (EntityManagerFactory factory : factories.values()) {
				factory.close();
			}
		}
		factoryCache.clear();
	}

	public Map<String, EntityManagerFactory> getCache() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Map<String, EntityManagerFactory> result = factoryCache.get(classLoader);

		if (result == null || result.isEmpty()) {
			logger.debug(bundle.getString("entity-manager-factory-not-found-in-cache"));
			for (String persistenceUnit : loadPersistenceUnitFromClassloader(classLoader)) {
				create(persistenceUnit);
				result = factoryCache.get(classLoader);
			}
		}

		return result;
	}
}
