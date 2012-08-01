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
package br.gov.frameworkdemoiselle.internal.bootstrap;

import java.io.FileNotFoundException;
import java.net.URL;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.util.Reflections;
import br.gov.frameworkdemoiselle.util.Strings;

public abstract class AbstractStrategyBootstrap<T, D extends T> extends AbstractBootstrap {

	private Class<T> type;
	
	private Class<D> defaultClass;

	private Class<? extends T> selected;

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

	public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event) {
		selected = loadSelected();
	}

	public <A> void processAnnotatedType(@Observes final ProcessAnnotatedType<A> event) {
		Class<A> annotated = event.getAnnotatedType().getJavaClass();

		if (Reflections.isOfType(annotated, getType()) && annotated != selected) {
			event.veto();
		}
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

			ClassLoader classLoader = ConfigurationLoader.getClassLoaderForResource(canonicalName);
			result = (Class<T>) Class.forName(canonicalName, false, classLoader);
			result.asSubclass(getType());

		} catch (org.apache.commons.configuration.ConfigurationException cause) {
			throw new ConfigurationException(getBundle().getString("file-not-found", "demoiselle.properties"));

		} catch (ClassNotFoundException cause) {
			key = Strings.getString("{0}-class-not-found", typeName);
			throw new ConfigurationException(getBundle().getString(key, canonicalName));

		} catch (ClassCastException cause) {
			key = Strings.getString("{0}-class-must-be-of-type", typeName);
			throw new ConfigurationException(getBundle().getString(key, canonicalName, getType()));
			
		} catch (FileNotFoundException e) {
			throw new ConfigurationException(getBundle().getString("file-not-found", "demoiselle.properties"));
		}

		return result;
	}
	
	public abstract String getConfigKey();

}
