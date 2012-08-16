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
package br.gov.frameworkdemoiselle.internal.producer;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URL;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Reflections;
import br.gov.frameworkdemoiselle.util.ResourceBundle;
import br.gov.frameworkdemoiselle.util.Strings;

public abstract class AbstractStrategyProducer<T, D extends T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private Class<T> type;

	private Class<D> defaultClass;

	private Class<? extends T> selected;

	@Inject
	@Name("demoiselle-core-bundle")
	private ResourceBundle bundle;

	@SuppressWarnings("unchecked")
	public T create() {
		BeanManager beanManager = Beans.getBeanManager();

		AnnotatedType<T> type = ((AnnotatedType<T>) beanManager.createAnnotatedType(getSelected()));
		InjectionTarget<T> it = beanManager.createInjectionTarget(type);
		CreationalContext<T> ctx = beanManager.createCreationalContext(null);

		T instance = it.produce(ctx);
		it.inject(instance, ctx);
		it.postConstruct(instance);

		return instance;
	}

	protected Class<? extends T> getSelected() {
		if (selected == null) {
			selected = loadSelected();
		}

		return selected;
	}

	private Class<T> getType() {
		if (this.type == null) {
			this.type = Reflections.getGenericTypeArgument(this.getClass(), 0);
		}

		return this.type;
	}

	private Class<D> getDefaultClass() {
		if (this.defaultClass == null) {
			this.defaultClass = Reflections.getGenericTypeArgument(this.getClass(), 1);
		}

		return this.defaultClass;
	}

	@SuppressWarnings("unchecked")
	private Class<T> loadSelected() {
		Class<T> result = null;
		String canonicalName = null;
		String typeName = getType().getSimpleName().toLowerCase();
		String key = null;

		try {
			URL url = ConfigurationLoader.getResourceAsURL("demoiselle.properties");
			Configuration config = new PropertiesConfiguration(url);
			canonicalName = config.getString(getConfigKey(), getDefaultClass().getCanonicalName());

			ClassLoader classLoader = ConfigurationLoader.getClassLoaderForClass(canonicalName);
			if (classLoader == null) {
				classLoader = Thread.currentThread().getContextClassLoader();
			}

			result = (Class<T>) Class.forName(canonicalName, false, classLoader);
			result.asSubclass(getType());

		} catch (org.apache.commons.configuration.ConfigurationException cause) {
			throw new ConfigurationException(bundle.getString("file-not-found", "demoiselle.properties"));

		} catch (ClassNotFoundException cause) {
			key = Strings.getString("{0}-class-not-found", typeName);
			throw new ConfigurationException(bundle.getString(key, canonicalName));

		} catch (ClassCastException cause) {
			key = Strings.getString("{0}-class-must-be-of-type", typeName);
			throw new ConfigurationException(bundle.getString(key, canonicalName, getType()));

		} catch (FileNotFoundException e) {
			throw new ConfigurationException(bundle.getString("file-not-found", "demoiselle.properties"));
		}

		return result;
	}

	public abstract String getConfigKey();

}
