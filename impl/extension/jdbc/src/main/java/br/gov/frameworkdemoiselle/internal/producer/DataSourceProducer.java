package br.gov.frameworkdemoiselle.internal.producer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.configuration.JDBCConfig;
import br.gov.frameworkdemoiselle.internal.proxy.BasicDataSourceProxy;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@ApplicationScoped
public class DataSourceProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient Logger logger;

	private transient ResourceBundle bundle;

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-jdbc-bundle"));
		}

		return bundle;
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = Beans.getReference(Logger.class, new NameQualifier(DataSourceProducer.class.getName()));
		}

		return logger;
	}

	private final Map<ClassLoader, Map<String, DataSource>> cache = Collections
			.synchronizedMap(new HashMap<ClassLoader, Map<String, DataSource>>());

	@PostConstruct
	public void loadDataSources() {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

		for (String dataBaseName : getDataSourceNames(contextClassLoader)) {

			try {
				create(dataBaseName);
			} catch (Exception cause) {
				throw new DemoiselleException(cause);
			}

			getLogger().debug(getBundle().getString("datasource-name-found", dataBaseName));
		}
	}

	private Set<String> getDataSourceNames(ClassLoader classLoader) {
		Set<String> result = new HashSet<String>();

		JDBCConfig config = Beans.getReference(JDBCConfig.class);

		if (config.getJndiName() != null) {
			result.addAll(config.getJndiName().keySet());
		}

		if (config.getDriverClass() != null) {
			result.addAll(config.getDriverClass().keySet());
		}

		if (result.isEmpty()) {
			throw new DemoiselleException(getBundle().getString("datasource-name-not-found"));
		}

		return result;
	}

	DataSource create(String dataSourceName) {
		DataSource factory;

		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Map<String, DataSource> localCache;

		if (cache.containsKey(contextClassLoader)) {
			localCache = cache.get(contextClassLoader);

			if (localCache.containsKey(dataSourceName)) {
				factory = localCache.get(dataSourceName);

			} else {
				factory = initDataSource(dataSourceName);

				localCache.put(dataSourceName, factory);
				cache.put(contextClassLoader, localCache);
			}

		} else {
			localCache = new HashMap<String, DataSource>();
			factory = initDataSource(dataSourceName);

			localCache.put(dataSourceName, factory);
			cache.put(contextClassLoader, localCache);
		}

		return factory;
	}

	private DataSource initDataSource(String dataSourceName) {
		DataSource result;

		JDBCConfig config = Beans.getReference(JDBCConfig.class);
		String jndi = config.getJndiName() == null ? null : config.getJndiName().get(dataSourceName);
		String url = config.getUrl() == null ? null : config.getUrl().get(dataSourceName);

		if (jndi != null) {
			result = initJNDIDataSource(dataSourceName, jndi);

		} else if (url != null) {
			result = new BasicDataSourceProxy(dataSourceName, config, getBundle());

		} else {
			throw new DemoiselleException(getBundle().getString("uncompleted-datasource-configuration", dataSourceName));
		}

		return result;
	}

	private DataSource initJNDIDataSource(String dataSourceName, String jndi) {
		DataSource result = null;

		try {
			Context context = new InitialContext();
			result = (DataSource) context.lookup(jndi);

		} catch (NamingException cause) {
			throw new DemoiselleException(getBundle().getString("load-jndi-datasource-failed", dataSourceName, jndi),
					cause);

		} catch (ClassCastException cause) {
			throw new DemoiselleException(getBundle().getString("load-duplicated-configuration-failed"), cause);
		}

		return result;
	}

	@PreDestroy
	public void close() {
		cache.clear();
	}

	Map<String, DataSource> getCache() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Map<String, DataSource> result = cache.get(classLoader);

		if (result == null || result.isEmpty()) {
			getLogger().debug(getBundle().getString("datasource-not-found-in-cache"));

			for (String name : getDataSourceNames(classLoader)) {
				create(name);
				result = cache.get(classLoader);
			}
		}

		return result;
	}
}
