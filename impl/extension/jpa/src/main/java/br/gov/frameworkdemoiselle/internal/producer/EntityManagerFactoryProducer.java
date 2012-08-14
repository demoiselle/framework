package br.gov.frameworkdemoiselle.internal.producer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
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
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@ApplicationScoped
public class EntityManagerFactoryProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	public static String ENTITY_MANAGER_RESOURCE = "META-INF/persistence.xml";

	@Inject
	private Logger logger;

	@Inject
	@Name("demoiselle-jpa-bundle")
	private ResourceBundle bundle;

	// private final Map<String, EntityManagerFactory> cache = Collections
	// .synchronizedMap(new HashMap<String, EntityManagerFactory>());

	/*
	 * private final Map<Key, EntityManagerFactory> cache = Collections .synchronizedMap(new HashMap<Key,
	 * EntityManagerFactory>());
	 */

	private final Map<ClassLoader, Map<String, EntityManagerFactory>> factoryCache = Collections
			.synchronizedMap(new HashMap<ClassLoader, Map<String, EntityManagerFactory>>());

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
				// logger.debug(bundle.getString("persistence-unit-name-found",
				// persistenceUnit));

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
			create(persistenceUnit);
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
