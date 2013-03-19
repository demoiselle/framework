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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.spi.AfterBeanDiscovery;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public final class Contexts {

	private static List<CustomContext> activeContexts = Collections.synchronizedList(new ArrayList<CustomContext>());

	private static List<CustomContext> inactiveContexts = Collections.synchronizedList(new ArrayList<CustomContext>());

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
			bundle = ResourceBundleProducer.create("demoiselle-core-bundle");
		}

		return bundle;
	}

	public static synchronized void add(CustomContext context, AfterBeanDiscovery event) {
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
	}
}
