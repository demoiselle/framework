package br.gov.frameworkdemoiselle.internal.producer;

import java.io.Serializable;
import java.sql.Connection;
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
import br.gov.frameworkdemoiselle.internal.configuration.JdbcConfig;
import br.gov.frameworkdemoiselle.internal.proxy.ConnectionProxy;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@RequestScoped
public class ConnectionProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	@Name("demoiselle-jdbc-bundle")
	private ResourceBundle bundle;

	private final Map<String, Connection> cache = Collections.synchronizedMap(new HashMap<String, Connection>());

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
	public Connection create(InjectionPoint ip, JdbcConfig config) {
		String name = getName(ip, config);
		return new ConnectionProxy(name);
	}

	public Connection getConnection(String name) {
		Connection result = null;

		if (cache.containsKey(name)) {
			result = cache.get(name);

		} else {
			try {
				result = producer.create(name).getConnection();

				cache.put(name, result);
				this.logger.info(bundle.getString("connection-was-created", name));

			} catch (Exception cause) {
				// TODO Colocar uma mensagem amigável

				throw new DemoiselleException("", cause);
			}
		}

		return result;
	}

	private String getName(InjectionPoint ip, JdbcConfig config) {
		String result;

		if (ip != null && ip.getAnnotated() != null && ip.getAnnotated().isAnnotationPresent(Name.class)) {
			// TODO Quando o comando Beans.getReference é usado para simular injeção, não existe
			// anotação @Inject então precisamos testar se #getAnnotated() retorna nulo aqui.
			result = ip.getAnnotated().getAnnotation(Name.class).value();

		} else {
			result = getNameFromProperties(config);

			if (result == null) {
				result = getNameFromCache();
			}
		}

		return result;
	}

	private String getNameFromProperties(JdbcConfig config) {
		String result = config.getDefaultDataDourceName();

		if (result != null) {
			this.logger.debug(bundle.getString("getting-default-datasource-name-from-properties", result));
		}

		return result;
	}

	private String getNameFromCache() {
		String result;
		Set<String> names = producer.getCache().keySet();

		if (names.size() > 1) {
			throw new DemoiselleException(bundle.getString("more-than-one-datasource-defined",
					Name.class.getSimpleName()));
		} else {
			result = names.iterator().next();
		}

		return result;
	}

	@PreDestroy
	public void close() {
		for (Connection connection : cache.values()) {
			try {
				if (connection.isClosed()) {
					// TODO Logar um warning informando que a conexão já havia sido finalizada.

				} else {
					connection.close();
					// TODO Logar um info informando que a conexão foi finalizada.
				}

			} catch (Exception cause) {
				// TODO Colocar uma mensagem amigável

				throw new DemoiselleException("", cause);
			}
		}

		cache.clear();
	}

	public Map<String, Connection> getCache() {
		return cache;
	}

}
