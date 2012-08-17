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

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;
import br.gov.frameworkdemoiselle.util.Strings;

public class StrategySelector {

	public static <T> T getReference(String configKey, Class<T> type, Class<? extends T> defaultType) {
		Class<T> selectedType = loadSelected(configKey, type, defaultType);
		return Beans.getReference(selectedType);
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> loadSelected(String configKey, Class<T> type, Class<? extends T> defaultType) {
		ResourceBundle bundle = ResourceBundleProducer.create("demoiselle-core-bundle",
				Beans.getReference(Locale.class));

		Class<T> result = null;
		String canonicalName = null;
		String typeName = type.getSimpleName().toLowerCase();
		String key = null;

		try {
			URL url = ConfigurationLoader.getResourceAsURL("demoiselle.properties");
			Configuration config = new PropertiesConfiguration(url);
			canonicalName = config.getString(configKey, defaultType.getCanonicalName());

			ClassLoader classLoader = ConfigurationLoader.getClassLoaderForClass(canonicalName);
			if (classLoader == null) {
				classLoader = Thread.currentThread().getContextClassLoader();
			}

			result = (Class<T>) Class.forName(canonicalName, false, classLoader);
			result.asSubclass(type);

		} catch (org.apache.commons.configuration.ConfigurationException cause) {
			throw new ConfigurationException(bundle.getString("file-not-found", "demoiselle.properties"));

		} catch (ClassNotFoundException cause) {
			key = Strings.getString("{0}-class-not-found", typeName);
			throw new ConfigurationException(bundle.getString(key, canonicalName));

		} catch (FileNotFoundException e) {
			throw new ConfigurationException(bundle.getString("file-not-found", "demoiselle.properties"));

		} catch (ClassCastException cause) {
			key = Strings.getString("{0}-class-must-be-of-type", typeName);
			throw new ConfigurationException(bundle.getString(key, canonicalName, type));
		}

		return result;
	}
}
