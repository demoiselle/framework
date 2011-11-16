package br.gov.frameworkdemoiselle.internal.producer;

import java.io.Serializable;
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

	private final Map<String, EntityManagerFactory> cache = Collections
			.synchronizedMap(new HashMap<String, EntityManagerFactory>());

	public EntityManagerFactory create(String persistenceUnit) {
		EntityManagerFactory factory;

		if (cache.containsKey(persistenceUnit)) {
			factory = cache.get(persistenceUnit);
		} else {
			factory = Persistence.createEntityManagerFactory(persistenceUnit);
			cache.put(persistenceUnit, factory);
		}

		return factory;
	}

	@PostConstruct
	public void init() {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = documentBuilder.parse(contextClassLoader.getResourceAsStream(ENTITY_MANAGER_RESOURCE));
			NodeList nodes = document.getElementsByTagName("persistence-unit");

			String persistenceUnit = "";
			for (int index = 0; index < nodes.getLength(); index++) {
				Node node = nodes.item(index);
				persistenceUnit = ((Element) node).getAttribute("name");

				if ("".equals(persistenceUnit)) {
					throw new DemoiselleException(bundle.getString("can-not-get-persistence-unit-from-persistence"));
				}

				create(persistenceUnit);
				logger.debug(bundle.getString("persistence-unit-name-found", persistenceUnit));
			}

		} catch (Exception cause) {
			String message = bundle.getString("can-not-get-persistence-unit-from-persistence");
			logger.error(message, cause);

			throw new DemoiselleException(message, cause);
		}
	}

	@PreDestroy
	public void close() {
		for (EntityManagerFactory factory : cache.values()) {
			factory.close();
		}

		cache.clear();
	}

	public Map<String, EntityManagerFactory> getCache() {
		return cache;
	}
}
