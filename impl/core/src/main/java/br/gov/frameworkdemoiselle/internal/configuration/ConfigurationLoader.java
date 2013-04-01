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

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import br.gov.frameworkdemoiselle.annotation.Ignore;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.ConfigType;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.util.Reflections;
import br.gov.frameworkdemoiselle.util.Strings;

/**
 * This component loads a config class annotated with {@link Configuration} by filling its attributes with {@link Param}
 * according to a {@link ConfigType}.
 * 
 * @author SERPRO
 */
public class ConfigurationLoader implements Serializable {

	private static final long serialVersionUID = 1L;

	private ConfigType type;

	private String resource;

	private String prefix;

	private org.apache.commons.configuration.Configuration configuration;

	public void load(Object object) throws ConfigurationException {
		loadType(object);
		loadResource(object);
		loadPrefix(object);
		loadConfiguration();
		loadFields(object);
	}

	private void loadType(Object object) {
		this.type = object.getClass().getAnnotation(Configuration.class).type();
	}

	private void loadConfiguration() {
		try {
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
				((FileConfiguration) conf).load();
			}

			this.configuration = conf;

		} catch (FileNotFoundException cause) {
			cause.printStackTrace();
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void loadResource(Object object) {
		if (this.type != SYSTEM) {
			String name = object.getClass().getAnnotation(Configuration.class).resource();
			String extension = this.type.toString().toLowerCase();

			this.resource = name + "." + extension;
		}
	}

	private void loadPrefix(Object object) {
		this.prefix = object.getClass().getAnnotation(Configuration.class).prefix();
	}

	private void loadFields(Object object) {
		for (Field field : Reflections.getNonStaticFields(object.getClass())) {
			loadField(field, object);
		}
	}

	private void loadField(Field field, Object object) {
		if (hasIgnore(field)) {
			return;
		}

		try {
			String key = getKey(field);
			Class<?> fieldType = field.getType();
			String methodName = "get" + Strings.firstToUpper(fieldType.getSimpleName());

			Method method = configuration.getClass().getMethod(methodName, String.class, fieldType);
			Object value = method.invoke(configuration, key, Reflections.getFieldValue(field, object));
			// TODO Se não achar no arquivo de configuração vai dar a falsa sensação que o valor padrão foi carregado de
			// lá. Corrigir isso!

			Reflections.setFieldValue(field, object, value);

		} catch (SecurityException cause) {
			cause.printStackTrace();
		} catch (NoSuchMethodException cause) {
			cause.printStackTrace();
		} catch (IllegalArgumentException cause) {
			cause.printStackTrace();
		} catch (IllegalAccessException cause) {
			cause.printStackTrace();
		} catch (InvocationTargetException cause) {
			cause.printStackTrace();
		}
	}

	private String getKey(Field field) {
		String key = this.prefix;

		if (field.isAnnotationPresent(Name.class)) {
			key += field.getAnnotation(Name.class).value();
		}else{
			key += field.getName();
		}

		return key;
	}

	private boolean hasIgnore(Field field) {
		return field.isAnnotationPresent(Ignore.class);
	}
}
