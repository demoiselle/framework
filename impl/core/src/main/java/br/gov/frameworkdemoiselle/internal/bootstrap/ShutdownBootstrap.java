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

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Shutdown;
import br.gov.frameworkdemoiselle.annotation.ViewScoped;
import br.gov.frameworkdemoiselle.internal.context.CustomContext;
import br.gov.frameworkdemoiselle.internal.context.ThreadLocalContext;
import br.gov.frameworkdemoiselle.internal.processor.ShutdownProcessor;

/**
 * This class run at application shutdown
 */
public class ShutdownBootstrap extends AbstractBootstrap {

	private static final Class<? extends Annotation> annotationClass = Shutdown.class;

	private static final List<CustomContext> tempContexts = new ArrayList<CustomContext>();

	private static final List<ShutdownProcessor<?>> processors = Collections
			.synchronizedList(new ArrayList<ShutdownProcessor<?>>());

	/**
	 * Observes all methods annotated with @Shutdown and create an instance of ShutdownProcessor for them
	 * 
	 * @param <T>
	 * @param event
	 * @param beanManager
	 */
	public <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> event, final BeanManager beanManager) {
		final AnnotatedType<T> annotatedType = event.getAnnotatedType();
		for (AnnotatedMethod<?> am : annotatedType.getMethods()) {
			if (am.isAnnotationPresent(annotationClass)) {
				@SuppressWarnings("unchecked")
				AnnotatedMethod<T> annotatedMethod = (AnnotatedMethod<T>) am;
				processors.add(new ShutdownProcessor<T>(annotatedMethod, beanManager));
			}
		}
	}

	public static void loadTempContexts(final AfterBeanDiscovery event) {
		// Não registrar o contexto de aplicação pq ele já é registrado pela
		// implementação do CDI
		tempContexts.add(new ThreadLocalContext(ViewScoped.class));
		tempContexts.add(new ThreadLocalContext(SessionScoped.class, false));
		tempContexts.add(new ThreadLocalContext(ConversationScoped.class));
		tempContexts.add(new ThreadLocalContext(RequestScoped.class));

		for (CustomContext tempContext : tempContexts) {
			addContext(tempContext, event);
		}
	}

	/**
	 * Before Shutdown it execute the methods annotateds with @Shutdown considering the priority order;
	 */
	public synchronized static void shutdown() {
		getLogger().debug(
				getBundle("demoiselle-core-bundle").getString("executing-all", annotationClass.getSimpleName()));

		Collections.sort(processors);
		Throwable failure = null;

		for (Iterator<ShutdownProcessor<?>> iter = processors.iterator(); iter.hasNext();) {
			ShutdownProcessor<?> processor = iter.next();

			try {
				processor.process();
				processors.remove(processor);

			} catch (Throwable cause) {
				failure = cause;
			}
		}

		unloadTempContexts();

		if (failure != null) {
			throw new DemoiselleException(failure);
		}
	}

	private static void unloadTempContexts() {
		for (CustomContext tempContext : tempContexts) {
			disableContext(tempContext);
		}
	}
}
