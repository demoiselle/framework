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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.ViewScoped;
import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.internal.context.Contexts;
import br.gov.frameworkdemoiselle.internal.context.CustomContext;
import br.gov.frameworkdemoiselle.internal.context.ThreadLocalContext;
import br.gov.frameworkdemoiselle.internal.implementation.AnnotatedMethodProcessor;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.Reflections;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public abstract class AbstractLifecycleBootstrap<A extends Annotation> implements Extension {

	private Class<A> annotationClass;

	@SuppressWarnings("rawtypes")
	private List<AnnotatedMethodProcessor> processors = Collections
			.synchronizedList(new ArrayList<AnnotatedMethodProcessor>());

	private List<CustomContext> tempContexts = new ArrayList<CustomContext>();

	private AfterBeanDiscovery afterBeanDiscoveryEvent;

	private boolean registered = false;

	private ResourceBundle bundle;

	protected abstract Logger getLogger();

	protected ResourceBundle getBundle() {
		if (this.bundle == null) {
			this.bundle = ResourceBundleProducer.create("demoiselle-core-bundle", Locale.getDefault());
		}

		return this.bundle;
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

	public <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> event) {
		final AnnotatedType<T> annotatedType = event.getAnnotatedType();

		for (AnnotatedMethod<?> am : annotatedType.getMethods()) {
			if (am.isAnnotationPresent(getAnnotationClass())) {
				@SuppressWarnings("unchecked")
				AnnotatedMethod<T> annotatedMethod = (AnnotatedMethod<T>) am;
				processors.add(newProcessorInstance(annotatedMethod));
			}
		}
	}

	public void loadTempContexts(@Observes final AfterBeanDiscovery event) {
		// Não registrar o contexto de aplicação pq ele já é registrado pela implementação do CDI
		tempContexts.add(new ThreadLocalContext(ViewScoped.class));
		tempContexts.add(new ThreadLocalContext(SessionScoped.class));
		tempContexts.add(new ThreadLocalContext(ConversationScoped.class));
		tempContexts.add(new ThreadLocalContext(RequestScoped.class));

		afterBeanDiscoveryEvent = event;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected synchronized void proccessEvent() {
		getLogger().debug(getBundle().getString("executing-all", getAnnotationClass().getSimpleName()));

		Collections.sort(processors);
		Exception failure = null;

		if (!registered) {
			for (CustomContext tempContext : tempContexts) {
				Contexts.add(tempContext, afterBeanDiscoveryEvent);
			}

			registered = true;
		}

		for (Iterator<AnnotatedMethodProcessor> iter = processors.iterator(); iter.hasNext();) {
			AnnotatedMethodProcessor<?> processor = iter.next();

			try {
				ClassLoader classLoader = ConfigurationLoader.getClassLoaderForClass(processor.getAnnotatedMethod()
						.getDeclaringType().getJavaClass().getCanonicalName());

				if (Thread.currentThread().getContextClassLoader().equals(classLoader)) {
					processor.process();
					iter.remove();
				}

			} catch (Exception cause) {
				failure = cause;
			}
		}

		if (processors.isEmpty()) {
			unloadTempContexts();
		}

		if (failure != null) {
			throw new DemoiselleException(failure);
		}
	}

	private void unloadTempContexts() {
		for (CustomContext tempContext : tempContexts) {
			Contexts.remove(tempContext);
		}
	}
}
