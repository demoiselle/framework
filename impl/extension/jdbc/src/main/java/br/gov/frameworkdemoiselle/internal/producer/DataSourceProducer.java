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
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.internal.configuration.JdbcConfig;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@ApplicationScoped
public class DataSourceProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	@Name("demoiselle-jdbc-bundle")
	private ResourceBundle bundle;

	private final Map<ClassLoader, Map<String, DataSource>> cache = Collections
			.synchronizedMap(new HashMap<ClassLoader, Map<String, DataSource>>());

	@PostConstruct
	public void loadDataSources() {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

		for (String dataBaseName : getDataSourceNames(contextClassLoader)) {

			try {
				create(dataBaseName);
			} catch (Throwable t) {
				throw new DemoiselleException(t);
			}

			logger.debug(bundle.getString("datasource-name-found", dataBaseName));
		}
	}

	private Set<String> getDataSourceNames(ClassLoader classLoader) {
		Set<String> result = new HashSet<String>();

		JdbcConfig config = Beans.getReference(JdbcConfig.class);

		if (config.getJndiName() != null) {
			result.addAll(config.getJndiName().keySet());
		}

		if (config.getDriverClass() != null) {
			result.addAll(config.getDriverClass().keySet());
		}

		if (result.isEmpty()) {
			throw new DemoiselleException(bundle.getString("datasource-name-not-found"));
		}

		return result;
	}

	public DataSource create(String dataSourceName) {
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

		try {
			JdbcConfig config = Beans.getReference(JdbcConfig.class);
			Map<String, String> jndiMap = config.getJndiName();

			if (jndiMap != null) {
				Context context = new InitialContext();
				result = (DataSource) context.lookup(jndiMap.get(dataSourceName));

			} else {
				String driver = config.getDriverClass().get(dataSourceName);
				String url = config.getUrl().get(dataSourceName);
				String username = config.getUsername().get(dataSourceName);
				String password = config.getPassword().get(dataSourceName);

				BasicDataSource dataSource = new BasicDataSource();
				dataSource.setDriverClassName(driver);
				dataSource.setUrl(url);
				dataSource.setUsername(username);
				dataSource.setPassword(password);

				result = dataSource;
			}

		} catch (Exception cause) {
			// TODO Colocar uma mensagem amigável

			throw new DemoiselleException("", cause);
		}

		return result;
	}

	@PreDestroy
	public void close() {
		cache.clear();
	}

	public Map<String, DataSource> getCache() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Map<String, DataSource> result = cache.get(classLoader);

		if (result == null || result.isEmpty()) {
			logger.debug(bundle.getString("datasource-not-found-in-cache"));

			for (String name : getDataSourceNames(classLoader)) {
				create(name);
				result = cache.get(classLoader);
			}
		}

		return result;
	}
}
