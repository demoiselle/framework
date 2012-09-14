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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;

public class ConfigurationBootstrap implements Extension {

	private final List<Class<?>> cache = Collections.synchronizedList(new ArrayList<Class<?>>());

	public <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> event) {
		final AnnotatedType<T> annotatedType = event.getAnnotatedType();

		if (annotatedType.getJavaClass().isAnnotationPresent(Configuration.class)) {
			cache.add(annotatedType.getJavaClass());
			event.veto();
		}
	}

	public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) throws Exception {
		Class<?> proxy;

		for (Class<?> config : cache) {
			proxy = createProxy(config);
			abd.addBean(new ProxyBean((Class<Object>) proxy, beanManager));
		}
	}

	@SuppressWarnings("unchecked")
	private Class<Object> createProxy(Class<?> type) throws Exception {
		String superClassName = type.getCanonicalName();
		String chieldClassName = superClassName + "__DemoiselleProxy";
		ClassPool pool = ClassPool.getDefault();

		CtClass ctChieldClass = pool.makeClass(chieldClassName);
		CtClass ctSuperClass = pool.get(superClassName);
		ctChieldClass.setSuperclass(ctSuperClass);

		StringBuffer buffer = new StringBuffer();
		buffer.append("new ");
		buffer.append(ConfigurationLoader.class.getCanonicalName());
		buffer.append("().load(this);");

		CtMethod ctChieldMethod;
		for (CtMethod ctSuperMethod : ctSuperClass.getDeclaredMethods()) {
			ctChieldMethod = CtNewMethod.delegator(ctSuperMethod, ctChieldClass);
			ctChieldMethod.insertBefore(buffer.toString());

			ctChieldClass.addMethod(ctChieldMethod);
		}

		// CtConstructor ctChieldDefaultConstructor = CtNewConstructor.defaultConstructor(ctChieldClass);
		// ctChieldClass.addConstructor(ctChieldDefaultConstructor);
		//
		// for (CtConstructor ctConstructor : ctChieldClass.getConstructors()) {
		// ctConstructor.insertBefore(buffer.toString());
		// }

		return ctChieldClass.toClass();
	}
}
