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
/*
 * Demoiselle Framework Copyright (c) 2010 Serpro and other contributors as indicated by the @author tag. See the
 * copyright.txt in the distribution for a full listing of contributors. Demoiselle Framework is an open source Java EE
 * library designed to accelerate the development of transactional database Web applications. Demoiselle Framework is
 * released under the terms of the LGPL license 3 http://www.gnu.org/licenses/lgpl.html LGPL License 3 This file is part
 * of Demoiselle Framework. Demoiselle Framework is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License 3 as published by the Free Software Foundation. Demoiselle Framework
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You
 * should have received a copy of the GNU Lesser General Public License along with Demoiselle Framework. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package br.gov.frameworkdemoiselle.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.bootstrap.CustomInjectionPoint;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;

public final class Beans {

	private static BeanManager manager;

	private Beans() {
	}

	public static void setBeanManager(BeanManager beanManager) {
		manager = beanManager;
	}

	public static BeanManager getBeanManager() {
		return manager;
	}

	public static <T> T getReference(final Class<T> beanClass, Annotation... qualifiers) {
		T instance;

		try {
			instance = (T) getReference(manager.getBeans(beanClass, qualifiers), beanClass, qualifiers);

		} catch (NoSuchElementException cause) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(beanClass.getCanonicalName());

			for (Annotation qualifier : qualifiers) {
				buffer.append(", ");
				buffer.append(qualifier.getClass().getCanonicalName());
			}

			String message = getBundle().getString("bean-not-found", buffer.toString());
			throw new DemoiselleException(message, cause);
		}

		return instance;
	}

	public static <T> T getReference(final Class<T> beanClass) {
		T instance;

		try {
			instance = (T) getReference(manager.getBeans(beanClass), beanClass);

		} catch (NoSuchElementException cause) {
			String message = getBundle().getString("bean-not-found", beanClass.getCanonicalName());
			throw new DemoiselleException(message, cause);
		}

		return instance;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getReference(String beanName) {
		T instance;

		try {
			instance = (T) getReference(manager.getBeans(beanName));

		} catch (NoSuchElementException cause) {
			String message = getBundle().getString("bean-not-found", beanName);
			throw new DemoiselleException(message, cause);
		}

		return instance;
	}

	@SuppressWarnings("unchecked")
	private static <T> T getReference(Set<Bean<?>> beans, Class<T> beanClass, Annotation... qualifiers) {
		Bean<?> bean = beans.iterator().next();
		CreationalContext<?> context = manager.createCreationalContext(bean);
		Type beanType = beanClass == null ? bean.getBeanClass() : beanClass;
		InjectionPoint injectionPoint = new CustomInjectionPoint(bean, beanType, qualifiers);

		return (T) manager.getInjectableReference(injectionPoint, context);
	}

	private static <T> T getReference(Set<Bean<?>> beans) {
		return getReference(beans, (Class<T>) null);
	}

	private static ResourceBundle getBundle() {
		return ResourceBundleProducer.create("demoiselle-core-bundle", Locale.getDefault());
	}
}
