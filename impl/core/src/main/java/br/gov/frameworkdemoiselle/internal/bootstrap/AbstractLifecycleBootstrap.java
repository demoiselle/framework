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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.context.ConversationContext;
import br.gov.frameworkdemoiselle.context.CustomContext;
import br.gov.frameworkdemoiselle.context.RequestContext;
import br.gov.frameworkdemoiselle.context.SessionContext;
import br.gov.frameworkdemoiselle.context.ViewContext;
import br.gov.frameworkdemoiselle.internal.context.TemporaryViewContextImpl;
import br.gov.frameworkdemoiselle.internal.implementation.AnnotatedMethodProcessor;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.Reflections;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public abstract class AbstractLifecycleBootstrap<A extends Annotation> implements Extension {

	private Class<A> annotationClass;

	@SuppressWarnings("rawtypes")
	private List<AnnotatedMethodProcessor> processors = Collections
			.synchronizedList(new ArrayList<AnnotatedMethodProcessor>());

	private transient static ResourceBundle bundle;

	private boolean registered = false;

	private HashMap<String, Boolean> startedContextHere = new HashMap<String, Boolean>();

	private transient CustomContext backupContext = null;

	protected abstract Logger getLogger();

	protected static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle"));
		}

		return bundle;
	}

	protected <T> AnnotatedMethodProcessor<T> newProcessorInstance(AnnotatedMethod<T> annotatedMethod) {
		return new AnnotatedMethodProcessor<T>(annotatedMethod);
	}

	protected Class<A> getAnnotationClass() {
		if (this.annotationClass == null) {
			this.annotationClass = Reflections.getGenericTypeArgument(this.getClass(), 0);
		}

		return this.annotationClass;
	}

	@SuppressWarnings("unchecked")
	public <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> event) {
		final AnnotatedType<T> annotatedType = event.getAnnotatedType();

		for (AnnotatedMethod<?> am : annotatedType.getMethods()) {
			if (am.isAnnotationPresent(getAnnotationClass())) {
				processors.add(newProcessorInstance((AnnotatedMethod<T>) am));
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected synchronized void proccessEvent() {
		getLogger().fine(getBundle().getString("executing-all", getAnnotationClass().getSimpleName()));

		Collections.sort(processors);
		Exception failure = null;

		startContexts();

		for (Iterator<AnnotatedMethodProcessor> iter = processors.iterator(); iter.hasNext();) {
			AnnotatedMethodProcessor<?> processor = iter.next();

			try {
				ClassLoader classLoader = Reflections.getClassLoaderForClass(processor.getAnnotatedMethod()
						.getDeclaringType().getJavaClass().getCanonicalName());

				if (Thread.currentThread().getContextClassLoader().equals(classLoader)) {
					processor.process();
					iter.remove();
				}

			} catch (Exception cause) {
				failure = cause;
			}
		}

		stopContexts();

		if (failure != null) {
			throw new DemoiselleException(failure);
		}
	}

	private void startContexts() {
		if (!registered) {
			RequestContext requestContext = Beans.getReference(RequestContext.class);
			SessionContext sessionContext = Beans.getReference(SessionContext.class);
			ViewContext viewContext = Beans.getReference(ViewContext.class);
			ConversationContext conversationContext = Beans.getReference(ConversationContext.class);

			if (requestContext != null) {
				startedContextHere.put("request", requestContext.activate());
			}

			if (sessionContext != null) {
				startedContextHere.put("session", sessionContext.activate());
			}

			if (conversationContext != null) {
				startedContextHere.put("conversation", conversationContext.activate());
			}

			// Contexto temporário de visão precisa de tratamento especial
			// para evitar conflito com o contexto presente na extensão demoiselle-jsf
			if (viewContext != null) {
				if (TemporaryViewContextImpl.class.isInstance(viewContext)) {
					startedContextHere.put("view", viewContext.activate());
				} else {
					// Precisamos desativar temporariamente o contexto
					if (viewContext.isActive()) {
						backupContext = viewContext;
						viewContext.deactivate();

						CustomContextBootstrap customContextBootstrap = Beans
								.getReference(CustomContextBootstrap.class);
						for (CustomContext customContext : customContextBootstrap.getCustomContexts()) {
							if (TemporaryViewContextImpl.class.isInstance(customContext)) {
								startedContextHere.put("view", customContext.activate());
								break;
							}
						}
					}
				}
			}

			registered = true;
		}
	}

	private void stopContexts() {
		if (registered) {
			RequestContext requestContext = Beans.getReference(RequestContext.class);
			SessionContext sessionContext = Beans.getReference(SessionContext.class);
			ViewContext viewContext = Beans.getReference(ViewContext.class);
			ConversationContext conversationContext = Beans.getReference(ConversationContext.class);

			if (requestContext != null && Boolean.TRUE.equals(startedContextHere.get("request"))) {
				requestContext.deactivate();
			}

			if (sessionContext != null && Boolean.TRUE.equals(startedContextHere.get("session"))) {
				sessionContext.deactivate();
			}

			if (conversationContext != null && Boolean.TRUE.equals(startedContextHere.get("conversation"))) {
				conversationContext.deactivate();
			}

			// Contexto temporário de visão precisa de tratamento especial
			// para evitar conflito com o contexto presente na extensão demoiselle-jsf
			if (viewContext != null) {
				if (TemporaryViewContextImpl.class.isInstance(viewContext) && startedContextHere.get("view")) {
					viewContext.deactivate();

					if (backupContext != null) {
						backupContext.activate();
						backupContext = null;
					}
				}
			}
		}
	}
}
