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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.implementation.ManagedType;
import br.gov.frameworkdemoiselle.internal.implementation.Management;
import br.gov.frameworkdemoiselle.lifecycle.AfterShutdownProccess;
import br.gov.frameworkdemoiselle.management.ManagementExtension;
import br.gov.frameworkdemoiselle.stereotype.ManagementController;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public class ManagementBootstrap implements Extension {

	protected List<AnnotatedType<?>> types = Collections.synchronizedList(new ArrayList<AnnotatedType<?>>());

	private List<Class<? extends ManagementExtension>> managementExtensionCache = Collections
			.synchronizedList(new ArrayList<Class<? extends ManagementExtension>>());

	public <T> void detectAnnotation(@Observes final ProcessAnnotatedType<T> event, final BeanManager beanManager) {
		if (event.getAnnotatedType().isAnnotationPresent(ManagementController.class)) {
			types.add(event.getAnnotatedType());
		}
	}

	/*public void activateContexts(@Observes final AfterBeanDiscovery event) {
		ContextManager.initialize(event);
		ContextManager.add(new ManagedContext(), event);
	}*/

	@SuppressWarnings("unchecked")
	public void registerAvailableManagedTypes(@Observes final AfterDeploymentValidation event, BeanManager beanManager) {
		ResourceBundle bundle = new ResourceBundle("demoiselle-core-bundle", Locale.getDefault());

		Management monitoringManager = Beans.getReference(Management.class);
		for (AnnotatedType<?> type : types) {
			if (type.getJavaClass().isInterface() || Modifier.isAbstract(type.getJavaClass().getModifiers())) {
				throw new DemoiselleException(bundle.getString("management-abstract-class-defined", type.getJavaClass()
						.getCanonicalName()));
			}

			ManagedType managedType = new ManagedType(type.getJavaClass());
			monitoringManager.addManagedType(managedType);
		}

		Set<Bean<?>> extensionBeans = beanManager.getBeans(ManagementExtension.class);
		if (extensionBeans != null) {
			for (Bean<?> bean : extensionBeans) {
				Class<?> extensionConcreteClass = bean.getBeanClass();
				managementExtensionCache.add((Class<? extends ManagementExtension>) extensionConcreteClass);
			}
		}

		monitoringManager.initialize(managementExtensionCache);
	}

	public void unregisterAvailableManagedTypes(@Observes final AfterShutdownProccess event) {

		Management manager = Beans.getReference(Management.class);
		manager.shutdown(managementExtensionCache);

		managementExtensionCache.clear();
		types.clear();
	}

}
