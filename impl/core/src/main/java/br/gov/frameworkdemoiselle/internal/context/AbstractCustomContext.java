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
package br.gov.frameworkdemoiselle.internal.context;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

public abstract class AbstractCustomContext implements CustomContext {

	private boolean active;

	private final Class<? extends Annotation> scope;

	public AbstractCustomContext(final Class<? extends Annotation> scope, boolean active) {
		this.scope = scope;
		this.active = active;
	}

	protected abstract Store getStore();

	@Override
	public <T> T get(final Contextual<T> contextual) {
		return get(contextual, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
		T instance = null;

		if (!isActive()) {
			throw new ContextNotActiveException();
		}

		Class<?> type = getType(contextual);
		if (getStore().contains(type)) {
			instance = (T) getStore().get(type);

		} else if (creationalContext != null) {
			instance = contextual.create(creationalContext);
			getStore().put(type, instance);
		}

		return instance;
	}

	private <T> Class<?> getType(final Contextual<T> contextual) {
		Bean<T> bean = (Bean<T>) contextual;
		return bean.getBeanClass();
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return this.scope;
	}

	protected static Store createStore() {
		return new Store();
	}

	static class Store {

		private Map<ClassLoader, Map<Class<?>, Object>> cache = Collections
				.synchronizedMap(new HashMap<ClassLoader, Map<Class<?>, Object>>());

		private Store() {
		}

		private boolean contains(final Class<?> type) {
			return this.getMap().containsKey(type);
		}

		private Object get(final Class<?> type) {
			return this.getMap().get(type);
		}

		private void put(final Class<?> type, final Object instance) {
			this.getMap().put(type, instance);
		}

		private Map<Class<?>, Object> getMap() {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

			if (!cache.containsKey(classLoader)) {
				cache.put(classLoader, Collections.synchronizedMap(new HashMap<Class<?>, Object>()));
			}

			return cache.get(classLoader);
		}
	}
}
