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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

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

	private static final Map<ClassLoader, Map<String, Class<Object>>> cacheClassLoader = Collections
			.synchronizedMap(new HashMap<ClassLoader, Map<String, Class<Object>>>());

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
			event.addBean(new CustomBean(proxy, beanManager));
		}
	}

	private static List<CtMethod> getMethods(CtClass type) throws NotFoundException {
		List<CtMethod> fields = new ArrayList<CtMethod>();

		if (type != null && !type.getName().equals(Object.class.getName())) {
			fields.addAll(Arrays.asList(type.getDeclaredMethods()));
			fields.addAll(getMethods(type.getSuperclass()));
		}

		return fields;
	}

	@SuppressWarnings("unchecked")
	private Class<Object> createProxy(Class<Object> type) throws Exception {
		String superClassName = type.getCanonicalName();
		String chieldClassName = superClassName + "__DemoiselleProxy";

		Map<String, Class<Object>> cacheProxy = Collections.synchronizedMap(new HashMap<String, Class<Object>>());;

		Class<Object> clazzProxy = null;

		ClassLoader classLoader = type.getClassLoader();
		if (cacheClassLoader.containsKey(classLoader)) {
			cacheProxy = cacheClassLoader.get(classLoader);
			if (cacheProxy.containsKey(chieldClassName)) {
				clazzProxy = cacheProxy.get(chieldClassName);
			}
		}

		if (clazzProxy == null) {

			ClassPool pool = new ClassPool();
			CtClass ctChieldClass = pool.getOrNull(chieldClassName);

			pool.appendClassPath(new LoaderClassPath(classLoader));
			CtClass ctSuperClass = pool.get(superClassName);

			ctChieldClass = pool.getAndRename(ConfigurationImpl.class.getCanonicalName(), chieldClassName);
			ctChieldClass.setSuperclass(ctSuperClass);

			CtMethod ctChieldMethod;
			for (CtMethod ctSuperMethod : getMethods(ctSuperClass)) {
				ctChieldMethod = CtNewMethod.delegator(ctSuperMethod, ctChieldClass);
				ctChieldMethod.insertBefore("load(this);");

				ctChieldClass.addMethod(ctChieldMethod);
			}

			clazzProxy = ctChieldClass.toClass(classLoader, type.getProtectionDomain());

			cacheProxy.put(chieldClassName, clazzProxy);
			cacheClassLoader.put(classLoader, cacheProxy);
		}

		return clazzProxy;
	}
}
