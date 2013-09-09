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
package br.gov.frameworkdemoiselle.internal.implementation;

import java.util.List;

import javax.management.ObjectInstance;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.internal.configuration.JMXConfig;
import br.gov.frameworkdemoiselle.internal.proxy.DynamicMBeanProxy;
import br.gov.frameworkdemoiselle.management.ManagementExtension;
import br.gov.frameworkdemoiselle.util.Beans;

public class JMXManagementExtension implements ManagementExtension {
	
	public void registerNotificationMBean(){
		MBeanManager mbeanManager = Beans.getReference(MBeanManager.class);
		JMXConfig configuration = Beans.getReference(JMXConfig.class);

		StringBuffer notificationMBeanName = new StringBuffer()
			.append( configuration.getNotificationDomain()!=null ? configuration.getNotificationDomain() : "br.gov.frameworkdemoiselle.jmx" )
			.append(":name=")
			.append(configuration.getNotificationMBeanName());
		
		if (mbeanManager.findMBeanInstance(notificationMBeanName.toString()) == null){
			NotificationEventListener listener = Beans.getReference(NotificationEventListener.class);

			ObjectInstance instance = MBeanHelper.register(listener.createNotificationBroadcaster(), notificationMBeanName.toString());
			mbeanManager.storeRegisteredMBean(instance);
		}
	}
	
	@Override
	public void initialize(List<ManagedType> managedTypes) {
		MBeanManager manager = Beans.getReference(MBeanManager.class);
		JMXConfig configuration = Beans.getReference(JMXConfig.class);

		for (ManagedType type : managedTypes) {
			DynamicMBeanProxy beanProxy = new DynamicMBeanProxy(type);
			
			Name nameAnnotation = type.getType().getAnnotation(Name.class);
			String mbeanName = nameAnnotation != null ? nameAnnotation.value() : type.getType().getSimpleName();

			StringBuffer name = new StringBuffer()
				.append( configuration.getMbeanDomain()!=null ? configuration.getMbeanDomain() : type.getType().getPackage().getName() )
				.append(":name=")
				.append( mbeanName );
			

			if (manager.findMBeanInstance(name.toString()) == null){
				ObjectInstance instance = MBeanHelper.register(beanProxy, name.toString());
				manager.storeRegisteredMBean(instance);
			}
		}

		registerNotificationMBean();
	}

	@Override
	public void shutdown(List<ManagedType> managedTypes) {
		MBeanManager manager = Beans.getReference(MBeanManager.class);
		for (ObjectInstance instance : manager.listRegisteredMBeans()){
			MBeanHelper.unregister(instance.getObjectName());
		}
		
		manager.cleanRegisteredMBeans();
	}

}
