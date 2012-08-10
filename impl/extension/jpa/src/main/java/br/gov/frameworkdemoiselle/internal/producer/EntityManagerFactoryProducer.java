package br.gov.frameworkdemoiselle.internal.producer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

	private final Map<Key, EntityManagerFactory> cache = Collections
			.synchronizedMap(new HashMap<Key, EntityManagerFactory>());

	public EntityManagerFactory create(String persistenceUnit) {
		EntityManagerFactory factory;

		//String[] key = new String [] { persistenceUnit, Thread.currentThread().getContextClassLoader().toString()};
		Key key = new Key(persistenceUnit, Thread.currentThread().getContextClassLoader());
		
		if (cache.containsKey(key)) {
			factory = cache.get(key);
		} else {
			factory = Persistence.createEntityManagerFactory(persistenceUnit);
			cache.put(key, factory);
		}

		return factory;
	}

	// @PostConstruct
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
		init();
		Map<String, EntityManagerFactory> result = new  HashMap<String, EntityManagerFactory>();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		for (Key key : cache.keySet()) {
			if(key.classLoader.equals(classLoader)) {
				result.put(key.persistenceUnit, cache.get(key));
			}
		}		
		
		return result;
	}
	
	
	class Key{
		
		private String persistenceUnit; 
		private ClassLoader classLoader;
		
		
		public Key(String persistenceUnit,  ClassLoader classLoader) {
			this.persistenceUnit = persistenceUnit;
			this.classLoader = classLoader;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((classLoader == null) ? 0 : classLoader.hashCode());
			result = prime * result + ((persistenceUnit == null) ? 0 : persistenceUnit.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (classLoader == null) {
				if (other.classLoader != null)
					return false;
			} else if (!classLoader.equals(other.classLoader))
				return false;
			if (persistenceUnit == null) {
				if (other.persistenceUnit != null)
					return false;
			} else if (!persistenceUnit.equals(other.persistenceUnit))
				return false;
			return true;
		}

	}

}


