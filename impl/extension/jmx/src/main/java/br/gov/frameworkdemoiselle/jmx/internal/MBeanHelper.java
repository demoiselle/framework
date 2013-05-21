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
package br.gov.frameworkdemoiselle.jmx.internal;

import java.lang.management.ManagementFactory;
import java.util.Locale;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Class with common tools for registering MBeans into an Managed server
 * 
 * @author SERPRO
 */
public class MBeanHelper {

	private static final Logger logger = LoggerProducer.create(MBeanHelper.class);
	
	private static ResourceBundle bundle = ResourceBundleProducer.create("demoiselle-jmx-bundle", Locale.getDefault());

	private static final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

	// @Inject
	// @Name("demoiselle-monitoring-bundle")
	// private ResourceBundle bundle;

	/**
	 * Return the MBean Server instance.
	 * 
	 * @return MBeanServer
	 */
	public static final MBeanServer getMBeanServer() {
		return server;
	}

	/**
	 * Register a given managed bean (MBean) with the specified name.
	 * 
	 * @param mbean
	 *            the managed bean to register
	 * @param name
	 *            the name under which to register the bean
	 * @return the object name of the mbean, for later deregistration
	 */
	public static ObjectInstance register(final Object mbean, final String name) {

		logger.info(bundle.getString("mbean-registration",name));

		ObjectInstance instance = null;
		try {
			ObjectName objectName = new ObjectName(name);
			instance = server.registerMBean(mbean, objectName);
		} catch (Exception e) {
			logger.error(bundle.getString("mbean-registration-error",name),e);
			throw new DemoiselleException(bundle.getString("mbean-registration-error",name), e);
		}

		return instance;
	}
	
	/**
	 * Remove the registration of a mbean.
	 * 
	 * @param objectName
	 *            the name of the bean to unregister
	 */
	public static void unregister(final ObjectName objectName) {

		logger.info(bundle.getString("mbean-deregistration",objectName.getCanonicalName()));

		try {
			server.unregisterMBean(objectName);
		} catch (Exception e) {
			logger.error(bundle.getString("mbean-deregistration",objectName.getCanonicalName()),e);
			throw new DemoiselleException(bundle.getString("mbean-deregistration",objectName.getCanonicalName()), e);
		}
	}
}
