/*
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
package br.gov.frameworkdemoiselle.internal.implementation;

import static br.gov.frameworkdemoiselle.configuration.ConfigType.SYSTEM;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

import javax.validation.constraints.NotNull;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Ignore;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.ConfigType;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.configuration.ConfigurationValueExtractor;
import br.gov.frameworkdemoiselle.internal.bootstrap.ConfigurationBootstrap;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
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

	private static ResourceBundle bundle;

	private static Logger logger;

	private Object object;

	private ConfigType type;

	private String resource;

	private String prefix;

	private org.apache.commons.configuration.Configuration configuration;

	private Collection<Field> fields;

	public void load(Object object) throws ConfigurationException {
		getLogger().debug(getBundle().getString("loading-configuration-class", object.getClass().getName()));

		this.object = object;

		loadFields();
		validateFields();

		loadType();
		loadResource();
		loadConfiguration();

		if (this.configuration != null) {
			loadPrefix();
			loadValues();
		}

		validateValues();
	}

	private void loadFields() {
		this.fields = Reflections.getNonStaticFields(this.object.getClass());
	}

	private void validateFields() {
		for (Field field : this.fields) {
			validateField(field);
		}
	}

	private void validateField(Field field) {
		Name annotation = field.getAnnotation(Name.class);

		if (annotation != null && Strings.isEmpty(annotation.value())) {
			throw new ConfigurationException(getBundle().getString("configuration-name-attribute-cant-be-empty"));
		}
	}

	private void loadType() {
		this.type = object.getClass().getAnnotation(Configuration.class).type();
	}

	private void loadResource() {
		if (this.type != SYSTEM) {
			String name = this.object.getClass().getAnnotation(Configuration.class).resource();
			String extension = this.type.toString().toLowerCase();

			this.resource = name + "." + extension;
		}
	}

	private void loadConfiguration() {
		org.apache.commons.configuration.Configuration config = createConfiguration();

		if (config instanceof FileConfiguration) {
			((FileConfiguration) config).setURL(Reflections.getResourceAsURL(this.resource));

			try {
				((FileConfiguration) config).load();

			} catch (org.apache.commons.configuration.ConfigurationException cause) {
				getLogger().warn(getBundle().getString("file-not-found", this.resource));
				config = null;
			}
		}

		this.configuration = config;
	}

	private org.apache.commons.configuration.Configuration createConfiguration() {
		AbstractConfiguration config;

		switch (this.type) {
			case PROPERTIES:
				config = new PropertiesConfiguration();
				break;

			case XML:
				config = new XMLConfiguration();
				break;

			case SYSTEM:
				config = new SystemConfiguration();
				break;

			default:
				throw new ConfigurationException(getBundle().getString("configuration-type-not-implemented-yet",
						type.name()));
		}

		config.setDelimiterParsingDisabled(true);
		return config;
	}

	private void loadPrefix() {
		String prefix = this.object.getClass().getAnnotation(Configuration.class).prefix();

		if (prefix.endsWith(".")) {
			// prefix = prefix.substring(0, prefix.length() - 1);
			// TODO Lançar warning pedindo para retirar o ponto (.)?
		} else if (!prefix.isEmpty()) {
			prefix += ".";
		}

		this.prefix = prefix;
	}

	private void loadValues() {
		for (Field field : this.fields) {
			loadValue(field);
		}
	}

	private void loadValue(Field field) {
		if (hasIgnore(field)) {
			return;
		}

		Object defaultValue = Reflections.getFieldValue(field, this.object);
		Object loadedValue = getValue(field, field.getType(), getKey(field), defaultValue);
		Object finalValue = (loadedValue == null ? defaultValue : loadedValue);

		if (loadedValue == null) {
			getLogger().debug(getBundle().getString("configuration-key-not-found", this.prefix + getKey(field)));
		}

		Reflections.setFieldValue(field, this.object, finalValue);
		getLogger().debug(
				getBundle().getString("configuration-field-loaded", this.prefix + getKey(field), field.getName(),
						finalValue == null ? "null" : finalValue));
	}

	private Object getValue(Field field, Class<?> type, String key, Object defaultValue) {
		ConfigurationValueExtractor extractor = getValueExtractor(field);
		return extractor.getValue(this.prefix, key, field, this.configuration);
	}

	private ConfigurationValueExtractor getValueExtractor(Field field) {
		Collection<ConfigurationValueExtractor> candidates = new HashSet<ConfigurationValueExtractor>();
		ConfigurationBootstrap bootstrap = Beans.getReference(ConfigurationBootstrap.class);

		for (Class<? extends ConfigurationValueExtractor> extractorClass : bootstrap.getCache()) {
			ConfigurationValueExtractor extractor = Beans.getReference(extractorClass);

			if (extractor.isSupported(field)) {
				candidates.add(extractor);
			}
		}

		ConfigurationValueExtractor elected = StrategySelector.getInstance(ConfigurationValueExtractor.class,
				candidates);

		if (elected == null) {
			// TODO lançar exceção informando que nenhum extrator foi encontrado para o field e ensinar como implementar
			// um extrator personalizado.
			throw new ConfigurationException("");
		}

		return elected;
	}

	private String getKey(Field field) {
		String key;

		if (field.isAnnotationPresent(Name.class)) {
			key = field.getAnnotation(Name.class).value();
		} else {
			key = field.getName();
		}

		return key;
	}

	private boolean hasIgnore(Field field) {
		return field.isAnnotationPresent(Ignore.class);
	}

	private void validateValues() {
		for (Field field : this.fields) {
			validateValue(field, Reflections.getFieldValue(field, this.object));
		}
	}

	private void validateValue(Field field, Object value) {
		if (field.isAnnotationPresent(NotNull.class) && value == null) {
			throw new ConfigurationException(getBundle().getString("configuration-attribute-is-mandatory",
					this.prefix + getKey(field), this.resource), new NullPointerException());
		}
	}

	private static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle"));
		}

		return bundle;
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = Beans.getReference(Logger.class, new NameQualifier(ConfigurationLoader.class.getName()));
		}

		return logger;
	}
}
