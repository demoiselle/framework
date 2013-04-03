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
package br.gov.frameworkdemoiselle.internal.configuration;

import static br.gov.frameworkdemoiselle.configuration.ConfigType.SYSTEM;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.ClassUtils;

import br.gov.frameworkdemoiselle.annotation.Ignore;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.ConfigType;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.util.Reflections;

/**
 * This component loads a config class annotated with {@link Configuration} by filling its attributes with {@link Param}
 * according to a {@link ConfigType}.
 * 
 * @author SERPRO
 */
public class ConfigurationLoader implements Serializable {

	private static final long serialVersionUID = 1L;

	private Object object;

	private ConfigType type;

	private String resource;

	private String prefix;

	private DataConfiguration configuration;

	private List<Field> fields;

	public void load(Object object) throws ConfigurationException {
		this.object = object;

		validateFields();

		loadType();
		loadResource();
		loadConfiguration();

		if (this.configuration != null) {
			loadPrefix();
			loadFields();
		}

		validateValues();
	}

	private void validateFields() {
		for (Field field : getFields()) {
			validateField(field);
		}
	}

	private void validateField(Field field) {
	}

	private void loadType() {
		this.type = object.getClass().getAnnotation(Configuration.class).type();
	}

	private void loadConfiguration() {
		AbstractConfiguration conf;

		switch (this.type) {
			case SYSTEM:
				conf = new SystemConfiguration();
				break;

			case XML:
				conf = new XMLConfiguration();
				break;

			default:
				conf = new PropertiesConfiguration();
				break;
		}

		conf.setDelimiterParsingDisabled(true);

		if (conf instanceof FileConfiguration) {
			((FileConfiguration) conf).setURL(Reflections.getResourceAsURL(this.resource));

			try {
				((FileConfiguration) conf).load();

			} catch (org.apache.commons.configuration.ConfigurationException cause) {
				conf = null;
			}
		}

		this.configuration = (conf == null ? null : new DataConfiguration(conf));
	}

	private void loadResource() {
		if (this.type != SYSTEM) {
			String name = this.object.getClass().getAnnotation(Configuration.class).resource();
			String extension = this.type.toString().toLowerCase();

			this.resource = name + "." + extension;
		}
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

	private void loadFields() {
		for (Field field : getFields()) {
			loadField(field);
		}
	}

	private List<Field> getFields() {
		if (this.fields == null) {
			this.fields = Reflections.getNonStaticFields(this.object.getClass());
		}

		return this.fields;
	}

	private void loadField(Field field) {
		if (hasIgnore(field)) {
			return;
		}

		Object defaultValue = Reflections.getFieldValue(field, this.object);
		Object finalValue = getValue(field.getType(), getKey(field), defaultValue);

		Reflections.setFieldValue(field, this.object, finalValue);
	}

	private Object getValue(Class<?> type, String key, Object defaultValue) {
		Object value;

		if (type.isArray()) {
			value = getArrayValue(type, key, defaultValue);

		} else if (type == Map.class) {
			value = getMapValue(type, key, defaultValue);

		} else if (type == Class.class) {
			value = getClassValue(type, key, defaultValue);

		} else {
			value = getPrimitiveOrWrappedValue(type, key, defaultValue);
		}

		return value;
	}

	private Object getArrayValue(Class<?> type, String key, Object defaultValue) {
		return this.configuration.getArray(type.getComponentType(), this.prefix + key, defaultValue);
	}

	private Object getMapValue(Class<?> type, String key, Object defaultValue) {
		@SuppressWarnings("unchecked")
		Map<String, Object> value = (Map<String, Object>) defaultValue;

		String regexp = "^(" + this.prefix + ")((.+)\\.)?(" + key + ")$";
		Pattern pattern = Pattern.compile(regexp);

		for (Iterator<String> iter = this.configuration.getKeys(); iter.hasNext();) {
			String iterKey = iter.next();
			Matcher matcher = pattern.matcher(iterKey);

			if (matcher.matches()) {
				String confKey = matcher.group(1) + (matcher.group(2) == null ? "" : matcher.group(2))
						+ matcher.group(4);

				if (value == null) {
					value = new HashMap<String, Object>();
				}

				String mapKey = matcher.group(3) == null ? "default" : matcher.group(3);
				value.put(mapKey, this.configuration.getString(confKey));
			}
		}

		return value;
	}

	private Object getClassValue(Class<?> type, String key, Object defaultValue) {
		Object value = defaultValue;
		String canonicalName = this.configuration.getString(this.prefix + key);

		if (canonicalName != null) {
			ClassLoader classLoader = Reflections.getClassLoaderForClass(canonicalName);

			try {
				value = Class.forName(canonicalName, true, classLoader);
			} catch (ClassNotFoundException cause) {
				// TODO Lançar a mensagem correta
				throw new ConfigurationException(null, cause);
			}
		}

		return value;
	}

	@SuppressWarnings("unchecked")
	private Object getPrimitiveOrWrappedValue(Class<?> type, String key, Object defaultValue) {
		Object value;

		try {
			value = this.configuration.get(ClassUtils.primitiveToWrapper(type), this.prefix + key, defaultValue);

		} catch (ConversionException cause) {
			value = defaultValue;
		}

		return value;
	}

	private String getKey(Field field) {
		String key = "";

		if (field.isAnnotationPresent(Name.class)) {
			key += field.getAnnotation(Name.class).value();
		} else {
			key += field.getName();
		}

		return key;
	}

	private boolean hasIgnore(Field field) {
		return field.isAnnotationPresent(Ignore.class);
	}

	private void validateValues() {
		for (Field field : getFields()) {
			validateValue(field);
		}
	}

	private void validateValue(Field field) {
		if (field.isAnnotationPresent(NotNull.class) && Reflections.getFieldValue(field, this.object) == null) {
			throw new ConfigurationException("", new NullPointerException());
			// TODO: Pegar mensagem do Bundle e verificar como as mensagens de log estão implementadas
		}
	}
}
