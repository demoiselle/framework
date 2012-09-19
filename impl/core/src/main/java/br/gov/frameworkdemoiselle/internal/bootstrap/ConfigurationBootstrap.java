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
import javassist.LoaderClassPath;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.internal.implementation.ConfigurationImpl;

public class ConfigurationBootstrap implements Extension {

	private final List<Class<Object>> cache = Collections.synchronizedList(new ArrayList<Class<Object>>());

	public void processAnnotatedType(@Observes final ProcessAnnotatedType<Object> event) {
		final AnnotatedType<Object> annotatedType = event.getAnnotatedType();

		if (annotatedType.getJavaClass().isAnnotationPresent(Configuration.class)) {
			cache.add(annotatedType.getJavaClass());
			event.veto();
		}
	}

	public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) throws Exception {
		Class<Object> proxy;

		for (Class<Object> config : cache) {
			proxy = createProxy(config);
			event.addBean(new ProxyBean(proxy, beanManager));
		}
	}

	@SuppressWarnings("unchecked")
	private Class<Object> createProxy(Class<Object> type) throws Exception {
		String superClassName = type.getCanonicalName();
		String chieldClassName = superClassName + "__DemoiselleProxy";

		ClassPool pool = ClassPool.getDefault();
		ClassPool.doPruning = true;
		
		CtClass ctChieldClass = pool.getOrNull(chieldClassName);

		ClassLoader classLoader = type.getClassLoader();
		if (ctChieldClass == null) {

			pool.appendClassPath(new LoaderClassPath(classLoader));
			// classLoader = Thread.currentThread().getContextClassLoader();
			// pool.appendClassPath(new LoaderClassPath(classLoader));
			// classLoader = ConfigurationLoader.getClassLoaderForClass(superClassName);
			// pool.appendClassPath(new LoaderClassPath(classLoader));

			CtClass ctSuperClass = pool.get(superClassName);

			// ctChieldClass = pool.makeClass(chieldClassName, ctSuperClass);
			ctChieldClass = pool.getAndRename(ConfigurationImpl.class.getCanonicalName(), chieldClassName);
			ctChieldClass.setSuperclass(ctSuperClass);

			// for (CtField ctFieldImpl : ctClassImpl.getDeclaredFields()) {
			// ctChieldClass.addField(new CtField(ctFieldImpl, ctChieldClass));
			// System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFF-----------" + ctFieldImpl.toString());
			// }

			// for (CtMethod ctMethodImpl : ctClassImpl.getDeclaredMethods()) {
			// ctChieldClass.addMethod(new CtMethod(ctMethodImpl, ctChieldClass, null));
			// System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMM-----------" + ctMethodImpl.toString());
			// }

			CtMethod ctChieldMethod;
			for (CtMethod ctSuperMethod : ctSuperClass.getDeclaredMethods()) {
				ctChieldMethod = CtNewMethod.delegator(ctSuperMethod, ctChieldClass);
				ctChieldMethod.insertBefore("loadProxyConfigurarion(this);");
				// ctChieldMethod
				// .insertBefore("new br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader().load(this);");

				ctChieldClass.addMethod(ctChieldMethod);
			}
		}

		return ctChieldClass.toClass(classLoader, type.getProtectionDomain());
	}
}
