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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.context.CustomContext;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public abstract class AbstractCustomContext implements CustomContext {

	private boolean active;

	private final Class<? extends Annotation> scope;
	
	private Logger logger;
	
	private transient ResourceBundle bundle;

	AbstractCustomContext(final Class<? extends Annotation> scope) {
		this.scope = scope;
		this.active = false;
	}

	protected abstract Store getStore();
	
	protected abstract boolean isStoreInitialized();

	@Override
	public <T> T get(final Contextual<T> contextual) {
		return get(contextual, null);
	}

	@SuppressWarnings("unchecked")
	@Override
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

	@Override
	public boolean activate() {
		if (!this.active){
			BeanManager beanManager = Beans.getBeanManager();
			if (beanManager!=null){
				try{
					Context ctx = beanManager.getContext(this.getScope());
					if (ctx!=null){
						getLogger().debug( getBundle().getString("custom-context-already-activated" , this.getClass().getCanonicalName() , this.getScope().getSimpleName() , ctx.getClass().getCanonicalName() ) );
					}
				}
				catch(ContextNotActiveException ce){
					this.active = true;
					getLogger().debug( getBundle().getString("custom-context-was-activated" , this.getClass().getCanonicalName() , this.getScope().getSimpleName() ) );
				}
			}
			else{
				this.active = true;
				getLogger().debug( getBundle().getString("custom-context-was-activated" , this.getClass().getCanonicalName() , this.getScope().getSimpleName() ) );
			}
		}
		
		return this.active;
	}
	
	@Override
	public void deactivate(){
		if (this.active){
			if (isStoreInitialized()){
				getStore().clear();
			}
			
			this.active = false;
			
			Logger logger = getLogger();
			ResourceBundle bundle = getBundle();
			logger.debug( bundle.getString("custom-context-was-deactivated" , this.getClass().getCanonicalName() , this.getScope().getSimpleName() ) );
		}
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return this.scope;
	}

	protected static Store createStore(Class<?> owningClass) {
		return new Store(owningClass);
	}
	
	private ResourceBundle getBundle(){
		if (bundle==null){
			bundle = new ResourceBundle("demoiselle-core-bundle", Locale.getDefault());
		}
		
		return bundle;
	}
	
	private Logger getLogger(){
		if (logger==null){
			logger = LoggerProducer.create(this.getClass());
		}
		
		return logger;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if ( !this.getClass().equals(obj.getClass()) )
			return false;
		AbstractCustomContext other = (AbstractCustomContext) obj;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}

	static class Store implements Serializable {

		private static final long serialVersionUID = -8237464177510563034L;
		
		private final String SEPARATOR = "#";
		
		private final String ID_PREFIX;
		
		private Map<String, Object> cache = Collections.synchronizedMap(new HashMap<String, Object>());

		private Store(Class<?> owningClass) {
			ID_PREFIX = Thread.currentThread().getContextClassLoader().toString()
					+ SEPARATOR + owningClass.getCanonicalName();
		}

		private boolean contains(final Class<?> type) {
			return cache.containsKey( prefixId(type.getCanonicalName()) );
		}

		private Object get(final Class<?> type) {
			return cache.get( prefixId(type.getCanonicalName()) );
		}

		private void put(final Class<?> type, final Object instance) {
			cache.put( prefixId(type.getCanonicalName()) , instance);
		}

		public void clear() {
			cache.clear();
		}
		
		private String prefixId(String id){
			return ID_PREFIX + SEPARATOR + id;
		}

		/*private Map<String, Object> getMap() {
			if (!cache.containsKey(classLoader)) {
				cache.put(classLoader, Collections.synchronizedMap(new HashMap<Class<?>, Object>()));
			}

			return cache.get(classLoader);
		}*/
	}
}
