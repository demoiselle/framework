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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public final class Contexts {

	private static List<CustomContext> contexts = Collections.synchronizedList(new ArrayList<CustomContext>());

	private static Logger logger;

	private static ResourceBundle bundle;

	private Contexts() {
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = LoggerProducer.create(Contexts.class);
		}

		return logger;
	}

	private static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = ResourceBundleProducer.create("demoiselle-core-bundle",Locale.getDefault());
		}

		return bundle;
	}
	
	/**
	 * Adds a custom context to the list of managed contexts. If the {@link CustomContext#isActive()} returns
	 * <code>true</code> the moment this method is called, it will be activated by calling {@link #activate(Class contextClass)} immediately.
	 * Otherwise the context will remain inactive until activated.
	 * 
	 * @param context Context to be addedd
	 * @param event Captured CDI event for adding the context
	 */
	public static synchronized void add(CustomContext context , AfterBeanDiscovery event){
		getLogger().trace(getBundle().getString("custom-context-was-registered", context.getClass().getCanonicalName(),context.getScope().getCanonicalName()));
		contexts.add(context);
		
		boolean mustActivate = context.isActive();
		context.setActive(false);
		event.addContext(context);
			
		if(mustActivate){
			activate(context.getClass());
		}
	}
	
	/**
	 * Activates a custom context. If there's already another context registered for this custom context's scope then it will not be activated
	 * and this method returns <code>false</code>. It will also fail and return <code>false</code> if the custom context was
	 * not registered with {@link #add(CustomContext context, AfterBeanDiscovery event)}.
	 * 
	 * @param contextClass Class of the contexto to activate
	 * @return <code>true</code> if the context was activated, <code>false</code> if it was not registered prior to activation or if there's already
	 * another context active for this context's scope.
	 */
	public static synchronized boolean activate(Class<? extends CustomContext> contextClass){
		for(CustomContext ctx : contexts){
			if (contextClass.getCanonicalName().equals(ctx.getClass().getCanonicalName()) ){
				activate(ctx);
			}
		}
		
		return false;
	}
	
	public static synchronized boolean activate(CustomContext context){
		try{
			Beans.getBeanManager().getContext(context.getScope());
			return false;
		}
		catch(ContextNotActiveException ce){
			context.setActive(true);
			getLogger().trace(getBundle().getString("custom-context-was-activated", context.getClass().getCanonicalName(),context.getScope().getCanonicalName()));
			return true;
		}
	}
	
	/**
	 * Deactivates a custom context previously activated by {@link #activate(Class)}.
	 * 
	 * @param contextClass Class of context to be deactivated
	 * 
	 * @return <code>true</code> if this context was active and is now deactivated. <code>false</code> if no context
	 * matching contextClass is active at the moment.
	 */
	public static synchronized boolean deactivate(Class<? extends CustomContext> contextClass){
		for(CustomContext ctx : contexts){
			if (contextClass.getCanonicalName().equals(ctx.getClass().getCanonicalName()) && ctx.isActive()){
				return deactivate(ctx);
			}
		}
		
		return false;
	}
	
	public static boolean deactivate(CustomContext ctx){
		try{
			Context activeContext = Beans.getBeanManager().getContext(ctx.getScope());
			ctx.setActive(false);
			if (activeContext == ctx){
				getLogger().trace(getBundle().getString("custom-context-was-deactivated", ctx.getClass().getCanonicalName(),ctx.getScope().getCanonicalName()));
				return true;
			}
		}
		catch(ContextNotActiveException ce){
		}

		return false;
	}
	
	/**
	 * Unregister all custom contexts of the provided class. If they are active the moment they're being removed, they will first be deactivated.
	 * 
	 * @param contextClass Custom context's class to me removed
	 */
	public static void remove(Class<? extends CustomContext> contextClass){
		for (Iterator<CustomContext> it = contexts.iterator();it.hasNext();){
			CustomContext ctx = it.next();
			if (contextClass.getCanonicalName().equals(ctx.getClass().getCanonicalName()) ){
				deactivate(ctx);
				it.remove();
				getLogger().trace(getBundle().getString("custom-context-was-unregistered", ctx.getClass().getCanonicalName(),ctx.getScope().getCanonicalName()));
			}
		}
	}
	
	/**
	 * Unregister a custom context. If it is active the moment it's being removed, it will first be deactivated.
	 * 
	 * @param ctx Custom context to remove
	 */
	public static void remove(CustomContext ctx){
		if (contexts.indexOf(ctx)>-1){
			deactivate(ctx);
			contexts.remove(ctx);
			getLogger().trace(getBundle().getString("custom-context-was-unregistered", ctx.getClass().getCanonicalName(),ctx.getScope().getCanonicalName()));
		}
	}
	
	/**
	 * Remove all registered custom contexts. All removed contexts are deactivated.
	 */
	public static synchronized void clear(){
		for (Iterator<CustomContext> it = contexts.iterator(); it.hasNext();){
			CustomContext ctx = it.next();
			deactivate(ctx);
			it.remove();
			
			getLogger().trace(getBundle().getString("custom-context-was-unregistered", ctx.getClass().getCanonicalName(),ctx.getScope().getCanonicalName()));
		}
	}

	/*public static synchronized void add(CustomContext context, AfterBeanDiscovery event) {
		Class<? extends Annotation> scope = context.getScope();

		getLogger()
				.trace(getBundle().getString("custom-context-was-registered", context.getScope().getCanonicalName()));

		if (get(scope, activeContexts) != null) {
			inactiveContexts.add(context);
			context.setActive(false);

		} else {
			activeContexts.add(context);
			context.setActive(true);
		}

		if (event != null) {
			event.addContext(context);
		}
	}

	private static CustomContext get(Class<? extends Annotation> scope, List<CustomContext> contexts) {
		CustomContext result = null;

		for (CustomContext context : contexts) {
			if (scope.equals(context.getScope())) {
				result = context;
				break;
			}
		}

		return result;
	}

	public static synchronized void remove(CustomContext context) {
		getLogger().trace(
				getBundle().getString("custom-context-was-unregistered", context.getScope().getCanonicalName()));

		if (activeContexts.contains(context)) {
			activeContexts.remove(context);
			context.setActive(false);

			CustomContext inactive = get(context.getScope(), inactiveContexts);
			if (inactive != null) {
				activeContexts.add(inactive);
				inactive.setActive(true);
				inactiveContexts.remove(inactive);
			}

		} else if (inactiveContexts.contains(context)) {
			inactiveContexts.remove(context);
		}
	}

	public static synchronized void clear() {
		CustomContext context;
		for (Iterator<CustomContext> iter = activeContexts.iterator(); iter.hasNext();) {
			context = iter.next();
			context.setActive(false);
			iter.remove();
		}

		activeContexts.clear();
		inactiveContexts.clear();
	}

	public static synchronized List<CustomContext> getActiveContexts() {
		return activeContexts;
	}

	public static synchronized List<CustomContext> getInactiveContexts() {
		return inactiveContexts;
	}*/
}
