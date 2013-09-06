package br.gov.frameworkdemoiselle.internal.producer;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.internal.configuration.JDBCConfig;
import br.gov.frameworkdemoiselle.internal.proxy.ConnectionProxy;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@RequestScoped
public class ConnectionProducer implements Serializable {

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

	private final Map<String, Connection> cache = Collections.synchronizedMap(new HashMap<String, Connection>());

	private final Map<Connection, Status> statusCache = Collections.synchronizedMap(new HashMap<Connection, Status>());

	@Inject
	private DataSourceProducer producer;

	@PostConstruct
	public void init() {
		for (String name : producer.getCache().keySet()) {
			getConnection(name);
		}
	}

	@Default
	@Produces
	public Connection createDefault(InjectionPoint ip, JDBCConfig config) {
		String name = getNameFromProperties(config);

		if (name == null) {
			name = getNameFromCache();
		}

		return new ConnectionProxy(name);
	}

	@Name("")
	@Produces
	public Connection createNamed(InjectionPoint ip, JDBCConfig config) {
		String name = ip.getAnnotated().getAnnotation(Name.class).value();
		return new ConnectionProxy(name);
	}

	public Connection getConnection(String name) {
		Connection connection = null;

		if (cache.containsKey(name)) {
			connection = cache.get(name);

		} else {
			try {
				connection = producer.create(name).getConnection();
				disableAutoCommit(connection);

				cache.put(name, connection);
				statusCache.put(connection, new Status());
				getLogger().info(getBundle().getString("connection-was-created", name));

			} catch (Exception cause) {
				throw new DemoiselleException(getBundle().getString("connection-creation-failed", name), cause);
			}
		}

		return connection;
	}

	private void disableAutoCommit(Connection connection) {
		try {
			connection.setAutoCommit(false);

		} catch (SQLException cause) {
			getLogger().debug(getBundle().getString("set-autocommit-failed"));
		}
	}

	private String getNameFromProperties(JDBCConfig config) {
		String result = config.getDefaultDataSourceName();

		if (result != null) {
			getLogger().debug(getBundle().getString("getting-default-datasource-name-from-properties", result));
		}

		return result;
	}

	private String getNameFromCache() {
		String result;
		Set<String> names = producer.getCache().keySet();

		if (names.size() > 1) {
			throw new DemoiselleException(getBundle().getString("more-than-one-datasource-defined",
					Name.class.getSimpleName()));
		} else {
			result = names.iterator().next();
		}

		return result;
	}

	@PreDestroy
	public void close() {
		Connection connection;

		for (String key : cache.keySet()) {
			connection = cache.get(key);

			try {
				if (connection.isClosed()) {
					getLogger().warn(getBundle().getString("connection-has-already-been-closed", key));

				} else {
					connection.close();

					getLogger().info(getBundle().getString("connection-was-closed", key));
				}

			} catch (Exception cause) {
				throw new DemoiselleException(getBundle().getString("connection-close-failed", key), cause);
			}
		}

		cache.clear();
	}

	public Map<String, Connection> getCache() {
		return cache;
	}

	public Status getStatus(Connection connection) {
		return statusCache.get(connection);
	}

	public static class Status implements Serializable {

		private static final long serialVersionUID = 1L;

		private boolean active = false;

		private boolean markedRollback = false;

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public boolean isMarkedRollback() {
			return markedRollback;
		}

		public void setRollbackOnly(boolean markedRollback) {
			this.markedRollback = markedRollback;
		}
	}
}
