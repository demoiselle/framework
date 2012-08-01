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
package br.gov.frameworkdemoiselle.internal.configuration;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Ignore;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.ConfigType;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.internal.bootstrap.CoreBootstrap;
import br.gov.frameworkdemoiselle.util.Reflections;
import br.gov.frameworkdemoiselle.util.ResourceBundle;
import br.gov.frameworkdemoiselle.util.Strings;

/**
 * This component loads a config class annotated with {@link Configuration} by filling its attributes with {@link Param}
 * according to a {@link ConfigType}.
 * 
 * @author SERPRO
 */
public class ConfigurationLoader implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	@Name("demoiselle-core-bundle")
	private ResourceBundle bundle;

	@Inject
	private Logger logger;

	/**
	 * Loads a config class filling it with the corresponding values.
	 * 
	 * @param object
	 *            config object
	 * @throws ConfigurationException
	 */
	public void load(Object object) throws ConfigurationException {
		Class<?> config = object.getClass();

		if (!CoreBootstrap.isAnnotatedType(config)) {
			config = config.getSuperclass();
			logger.debug(bundle.getString("proxy-detected", config, config.getClass().getSuperclass()));
		}

		logger.debug(bundle.getString("loading-configuration-class", config.getName()));

		for (Field field : Reflections.getNonStaticDeclaredFields(config)) {
			loadField(field, object, config);
		}
	}

	private void loadField(Field field, Object object, Class<?> clazz) {
		if (!field.isAnnotationPresent(Ignore.class) && clazz.isAnnotationPresent(Configuration.class)) {
			String resource = clazz.getAnnotation(Configuration.class).resource();
			ConfigType type = clazz.getAnnotation(Configuration.class).type();
			org.apache.commons.configuration.Configuration config = getConfiguration(resource, type);

			String key = getKey(field, clazz, config);
			Object value = getValue(key, field.getType(), config);

			validate(field, key, value, resource);
			setValue(field, key, object, value);
		}
	}

	private void setValue(Field field, String key, Object object, Object value) {
		if (value != null) {
			Reflections.setFieldValue(field, object, value);
			logger.debug(bundle.getString("configuration-field-loaded", key, field.getName(), value));
		}
	}

	private void validate(Field field, String key, Object value, String resource) {
		if (field.isAnnotationPresent(NotNull.class) && value == null) {
			throw new ConfigurationException(bundle.getString("configuration-attribute-is-mandatory", key, resource));
		}
	}

	private String getKey(final Field field, final Class<?> clazz,
			final org.apache.commons.configuration.Configuration config) {

		final String prefix = getPrefix(field, clazz);
		final StringBuffer key = new StringBuffer();

		key.append(prefix);

		if (field.isAnnotationPresent(Name.class)) {
			key.append(getKeyByAnnotation(field));
		} else {
			key.append(getKeyByConvention(field, prefix, config));
		}

		return key.toString();
	}

	private String getPrefix(Field field, Class<?> type) {
		String prefix = "";

		Configuration classAnnotation = type.getAnnotation(Configuration.class);
		if (!Strings.isEmpty(classAnnotation.prefix())) {
			prefix = classAnnotation.prefix() + ".";
		}

		return prefix;
	}

	private String getKeyByAnnotation(Field field) {
		String key = null;

		Name nameAnnotation = field.getAnnotation(Name.class);
		if (Strings.isEmpty(nameAnnotation.value())) {
			throw new ConfigurationException(bundle.getString("configuration-name-attribute-cant-be-empty"));
		} else {
			key = nameAnnotation.value();
		}

		return key;
	}

	private String getKeyByConvention(Field field, String prefix, org.apache.commons.configuration.Configuration config) {

		Set<String> conventions = new HashSet<String>();
		conventions.add(field.getName());
		conventions.add(Strings.camelCaseToSymbolSeparated(field.getName(), "."));
		conventions.add(Strings.camelCaseToSymbolSeparated(field.getName(), "_"));
		conventions.add(field.getName().toLowerCase());
		conventions.add(field.getName().toUpperCase());

		int matches = 0;
		String key = field.getName();
		for (String convention : conventions) {
			if (config.containsKey(prefix + convention)) {
				key = convention;
				matches++;
			}
		}

		if (matches == 0) {
			logger.debug(bundle.getString("configuration-key-not-found", key, conventions));
		} else if (matches > 1) {
			throw new ConfigurationException(bundle.getString("ambiguous-key", field.getName(),
					field.getDeclaringClass()));
		}

		return key;
	}

	/**
	 * Returns the configuration class according to specified resource name and configuration type.
	 * 
	 * @param resource
	 * @param type
	 * @return a configuration
	 */
	private org.apache.commons.configuration.Configuration getConfiguration(String resource, ConfigType type) {
		org.apache.commons.configuration.Configuration config = null;

		try {
			URL url;

			switch (type) {
				case SYSTEM:
					config = new SystemConfiguration();
					break;

				case PROPERTIES:
					url = getResourceAsURL(resource + ".properties");
					config = new PropertiesConfiguration(url);
					break;

				case XML:
					url = getResourceAsURL(resource + ".xml");
					config = new PropertiesConfiguration(url);
					break;

				default:
					throw new ConfigurationException(bundle.getString("configuration-type-not-implemented-yet",
							type.name()));
			}

		} catch (Exception cause) {
			throw new ConfigurationException(bundle.getString("error-creating-configuration-from-resource", resource),
					cause);
		}

		return config;
	}

	/**
	 * Returns the value associated with the given configuration class and field type.
	 * 
	 * @param name
	 * @param config
	 * @param fieldClass
	 * @return the value
	 */
	@SuppressWarnings("unchecked")
	private <T> T getValue(String key, Class<T> fieldClass, org.apache.commons.configuration.Configuration config) {
		Object value;

		if (fieldClass.isArray() && fieldClass.getComponentType().equals(String.class)) {
			value = config.getStringArray(key);

		} else if (fieldClass.equals(Properties.class)) {
			value = getProperty(key, config);

		} else {
			value = getBasic(key, fieldClass, config);
		}

		return (T) value;
	}

	private <T> Object getBasic(String key, Class<T> fieldClass, org.apache.commons.configuration.Configuration config) {
		Object value = null;

		try {
			Method method;
			String methodName = "get" + Strings.firstToUpper(fieldClass.getSimpleName());

			if (!fieldClass.isPrimitive()) {
				method = config.getClass().getMethod(methodName, String.class, fieldClass);
				value = method.invoke(config, key, null);
			} else if (config.containsKey(key)) {
				method = config.getClass().getMethod(methodName, String.class);
				value = method.invoke(config, key);
			}

		} catch (Throwable cause) {
			throw new ConfigurationException(bundle.getString("error-converting-to-type", fieldClass.getName()), cause);
		}

		return value;
	}

	private Object getProperty(String key, org.apache.commons.configuration.Configuration config) {
		Object value = null;

		@SuppressWarnings("unchecked")
		Iterator<String> iterator = config.getKeys(key);
		if (iterator.hasNext()) {
			Properties props = new Properties();

			while (iterator.hasNext()) {
				String fullKey = iterator.next();
				String prefix = key + ".";
				String unprefixedKey = fullKey.substring(prefix.length());
				props.put(unprefixedKey, config.getString(fullKey));
			}

			value = props;
		}

		return value;
	}

	public static URL getResourceAsURL(final String resource) throws FileNotFoundException {
		final String stripped = resource.startsWith("/") ? resource.substring(1) : resource;

		URL url = null;
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		if (classLoader != null) {
			url = classLoader.getResource(stripped);
		}

		if (url == null) {
			url = ConfigurationLoader.class.getResource(stripped);
		}
		if (url == null) {
			url = ConfigurationLoader.class.getClassLoader().getResource(stripped);
		}

		if (url == null) {
			throw new FileNotFoundException(resource + " not found.");
		}

		return url;
	}
}
