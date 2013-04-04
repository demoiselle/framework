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

import br.gov.frameworkdemoiselle.annotation.Ignore;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.ConfigType;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.configuration.ConfigurationValueExtractor;
import br.gov.frameworkdemoiselle.internal.bootstrap.ConfigurationBootstrap;
import br.gov.frameworkdemoiselle.util.Beans;
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

	private org.apache.commons.configuration.Configuration configuration;

	private Collection<Field> fields;

	public void load(Object object) throws ConfigurationException {
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

		this.configuration = conf;
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
		Object finalValue = getValue(field, field.getType(), getKey(field), defaultValue);

		Reflections.setFieldValue(field, this.object, finalValue);
	}

	private Object getValue(Field field, Class<?> type, String key, Object defaultValue) {
		ConfigurationValueExtractor extractor = getValueExtractor(field);
		return extractor.getValue(this.prefix, key, field, this.configuration, defaultValue);
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
		}

		return elected;
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
		for (Field field : this.fields) {
			validateValue(field, Reflections.getFieldValue(field, this.object));
		}
	}

	private void validateValue(Field field, Object value) {
		if (field.isAnnotationPresent(NotNull.class) && value == null) {
			throw new ConfigurationException("", new NullPointerException());
			// TODO: Pegar mensagem do Bundle e verificar como as mensagens de log estão implementadas
		}
	}
}
