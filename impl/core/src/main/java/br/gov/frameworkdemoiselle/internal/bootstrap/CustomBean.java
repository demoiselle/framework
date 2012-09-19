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
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;

/**
 * @see http://docs.jboss.org/weld/reference/latest/en-US/html_single/#d0e5035
 */
public class CustomBean implements Bean<Object> {

	private Class<Object> beanClass;

	private InjectionTarget<Object> injectionTarget;

	public CustomBean(Class<Object> beanClass, BeanManager beanManager) {
		AnnotatedType<Object> annotatedType = beanManager.createAnnotatedType(beanClass);

		this.injectionTarget = beanManager.createInjectionTarget(annotatedType);
		this.beanClass = beanClass;
	}

	public Object create(CreationalContext<Object> creationalContext) {
		Object instance = injectionTarget.produce(creationalContext);
		injectionTarget.inject(instance, creationalContext);
		injectionTarget.postConstruct(instance);

		return instance;
	}

	public void destroy(Object instance, CreationalContext<Object> creationalContext) {
		injectionTarget.preDestroy(instance);
		injectionTarget.dispose(instance);
		creationalContext.release();
	}

	public Set<Type> getTypes() {
		Set<Type> types = new HashSet<Type>();
		types.add(beanClass.getSuperclass());
		types.add(Object.class);

		return types;
	}

	@SuppressWarnings("serial")
	public Set<Annotation> getQualifiers() {
		Set<Annotation> result = new HashSet<Annotation>();

		result.add(new AnnotationLiteral<Default>() {
		});
		result.add(new AnnotationLiteral<Any>() {
		});

		for (Annotation annotation : beanClass.getAnnotations()) {
			if (annotation.getClass().isAnnotationPresent(Qualifier.class)) {
				result.add(annotation);
			}
		}

		return result;
	}

	public Class<? extends Annotation> getScope() {
		Class<? extends Annotation> result = Dependent.class;

		Class<? extends Annotation> annotationClass;
		for (Annotation annotation : beanClass.getAnnotations()) {
			annotationClass = annotation.getClass();

			if (annotationClass.isAnnotationPresent(Scope.class)
					|| annotationClass.isAnnotationPresent(NormalScope.class)) {
				result = annotationClass;
				break;
			}
		}

		return result;
	}

	public String getName() {
		String result = null;

		if (beanClass.isAnnotationPresent(Named.class)) {
			result = beanClass.getAnnotation(Named.class).value();
		}

		return result;
	}

	public Set<Class<? extends Annotation>> getStereotypes() {
		Set<Class<? extends Annotation>> result = new HashSet<Class<? extends Annotation>>();

		for (Annotation annotation : beanClass.getAnnotations()) {
			if (annotation.getClass().isAnnotationPresent(Stereotype.class)) {
				result.add(annotation.getClass());
			}
		}

		return result;
	}

	public Class<Object> getBeanClass() {
		return beanClass;
	}

	public boolean isAlternative() {
		return beanClass.isAnnotationPresent(Alternative.class);
	}

	public boolean isNullable() {
		return false;
	}

	public Set<InjectionPoint> getInjectionPoints() {
		return injectionTarget.getInjectionPoints();
	}
}
