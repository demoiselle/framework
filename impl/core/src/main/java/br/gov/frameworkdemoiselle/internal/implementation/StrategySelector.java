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

import static br.gov.frameworkdemoiselle.annotation.Priority.MIN_PRIORITY;

import java.io.Serializable;
import java.util.List;

import br.gov.frameworkdemoiselle.annotation.Priority;

public final class StrategySelector implements Serializable {

	public static final int CORE_PRIORITY = MIN_PRIORITY;

	public static final int EXTENSIONS_L1_PRIORITY = CORE_PRIORITY - 100;

	public static final int EXTENSIONS_L2_PRIORITY = EXTENSIONS_L1_PRIORITY - 100;

	public static final int COMPONENTS_PRIORITY = EXTENSIONS_L2_PRIORITY - 100;

	private static final long serialVersionUID = 1L;

	private StrategySelector() {
	}

	public static <T> Class<? extends T> getClass(Class<? extends T> configClass/* , Class<T> defaultClass */,
			List<Class<? extends T>> optionalClasses) {
		Class<? extends T> result = configClass;

		if (configClass == getDefaultClass(optionalClasses)) {
			result = getPriorityReference(optionalClasses);
		}

		return result;
	}

	public static <T> Class<? extends T> getDefaultClass(List<Class<? extends T>> optionalClasses) {
		Class<? extends T> result = null;

		for (Class<? extends T> optionalClass : optionalClasses) {
			Priority priority = optionalClass.getAnnotation(Priority.class);

			if (priority != null && priority.value() == CORE_PRIORITY) {
				result = optionalClass;
				break;
			}
		}

		return result;
	}

	/*
	 * public static <T> T getReference(String configKey, Class<T> strategyType, Class<T> defaultType, List<Class<T>>
	 * options) { T result = getExplicitReference(configKey, strategyType, defaultType); if (result.getClass() ==
	 * defaultType) { result = getPriorityReference(options); } return result; }
	 */

	public static <T> Class<? extends T> getPriorityReference(List<Class<? extends T>> options) {
		Class<? extends T> selected = null;

		for (Class<? extends T> option : options) {
			if (selected == null || getPriority(option) < getPriority(selected)) {
				selected = option;
			}
		}

		return selected;
	}

	private static <T> int getPriority(Class<T> type) {
		int result = Priority.MAX_PRIORITY;
		Priority priority = type.getAnnotation(Priority.class);

		if (priority != null) {
			result = priority.value();
		}

		return result;
	}

	/*
	 * public static <T> T getExplicitReference(String configKey, Class<T> strategyType, Class<T> defaultType) {
	 * Class<T> selectedType = loadSelected(configKey, strategyType, defaultType); return
	 * Beans.getReference(selectedType); }
	 */

	/*
	 * @SuppressWarnings("unchecked") private static <T> Class<T> loadSelected(String configKey, Class<T> strategyType,
	 * Class<T> defaultType) { ResourceBundle bundle = ResourceBundleProducer.create("demoiselle-core-bundle",
	 * Beans.getReference(Locale.class)); Class<T> result = null; String canonicalName = null; String typeName =
	 * strategyType.getSimpleName().toLowerCase(); String key = null; try { URL url =
	 * ConfigurationLoader.getResourceAsURL("demoiselle.properties"); Configuration config = new
	 * PropertiesConfiguration(url); canonicalName = config.getString(configKey, defaultType.getCanonicalName());
	 * ClassLoader classLoader = ConfigurationLoader.getClassLoaderForClass(canonicalName); if (classLoader == null) {
	 * classLoader = Thread.currentThread().getContextClassLoader(); } result = (Class<T>) Class.forName(canonicalName,
	 * false, classLoader); result.asSubclass(strategyType); } catch
	 * (org.apache.commons.configuration.ConfigurationException cause) { throw new
	 * ConfigurationException(bundle.getString("file-not-found", "demoiselle.properties")); } catch
	 * (ClassNotFoundException cause) { key = Strings.getString("{0}-class-not-found", typeName); throw new
	 * ConfigurationException(bundle.getString(key, canonicalName)); } catch (FileNotFoundException e) { throw new
	 * ConfigurationException(bundle.getString("file-not-found", "demoiselle.properties")); } catch (ClassCastException
	 * cause) { key = Strings.getString("{0}-class-must-be-of-type", typeName); throw new
	 * ConfigurationException(bundle.getString(key, canonicalName, strategyType)); } return result; }
	 */
}
