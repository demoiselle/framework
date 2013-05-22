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
package br.gov.frameworkdemoiselle.jmx.configuration;

import javax.management.NotificationBroadcaster;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.stereotype.ManagementController;

@Configuration(prefix = "frameworkdemoiselle.management.jmx.")
public class JMXConfig {
	
	@Name("mbean.domain")
	private String mbeanDomain;
	
	@Name("notification.domain")
	private String notificationDomain = "br.gov.frameworkdemoiselle.jmx";
	
	@Name("notification.name")
	private String notificationMBeanName = "NotificationBroadcaster";
	
	/**
	 * </p>The domain to register all {@link ManagementController} classes found during boot.</p>
	 * 
	 * <p>The full name of a MBean has the format of <code>domain:name=MBeanName</code> (ex: <code>br.gov.frameworkdemoiselle.jmx:name=NotificationBroadcaster</code>), this
	 * parameter is the "domain" portion of the full name.</p>
	 * 
	 * <p>The default is <code>null</code> and when is set to <code>null</code>, all {@link Managed} classes will use it's own package as the domain.</p>
	 * 
	 */
	public String getMbeanDomain() {
		return mbeanDomain;
	}
	
	/**
	 * @see #getMbeanDomain()
	 */
	public void setMbeanDomain(String mbeanDomain) {
		this.mbeanDomain = mbeanDomain;
	}

	/**
	 * <p>The name the {@link NotificationBroadcaster} MBean will be registered to. The full name
	 * of a MBean has the format of <code>domain:name=MBeanName</code> (ex: <code>br.gov.frameworkdemoiselle.jmx:name=NotificationBroadcaster</code>), this
	 * parameter is the ":name=MBeanName" portion without the ":name=".</p>
	 * 
	 * <p>The default is the value returned by {@link Class#getSimpleName()} when called from the {@link NotificationBroadcaster} class.</p>
	 * 
	 * @see #getMbeanDomain()
	 */
	public String getNotificationMBeanName() {
		return notificationMBeanName;
	}

	/**
	 * @see #getNotificationMBeanName()
	 */
	public void setNotificationMBeanName(String notificationMBeanName) {
		this.notificationMBeanName = notificationMBeanName;
	}

	/**
	 * </p>The domain to register the {@link NotificationBroadcaster} MBean.</p>
	 * 
	 * <p>The full name of a MBean has the format of <code>domain:name=MBeanName</code> (ex: <code>br.gov.frameworkdemoiselle.jmx:name=NotificationBroadcaster</code>), this
	 * parameter is the "domain" portion of the full name.</p>
	 * 
	 * <p>The default is <code>br.gov.frameworkdemoiselle.jmx</code>.</p>
	 * 
	 */
	public String getNotificationDomain() {
		return notificationDomain;
	}

	/**
	 * @see #getNotificationDomain()
	 */
	public void setNotificationDomain(String notificationDomain) {
		this.notificationDomain = notificationDomain;
	}
	
	

}
