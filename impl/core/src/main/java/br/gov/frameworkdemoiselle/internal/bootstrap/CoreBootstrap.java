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

import java.util.Locale;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public class CoreBootstrap implements Extension {

	private Logger logger;

	private transient ResourceBundle bundle;
	
	private Logger getLogger() {
		if (this.logger == null) {
			this.logger = LoggerProducer.create(CoreBootstrap.class);
		}

		return this.logger;
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = new ResourceBundle("demoiselle-core-bundle", Locale.getDefault());
		}

		return bundle;
	}

	public void engineOn(@Observes final BeforeBeanDiscovery event, BeanManager beanManager) {
		getLogger().info(getBundle().getString("engine-on"));

		Beans.setBeanManager(beanManager);
		getLogger().info(getBundle().getString("setting-up-bean-manager", Beans.class.getCanonicalName()));
	}

	/*public void initializeCustomContexts(@Observes final AfterBeanDiscovery event) {
		Beans.getReference(ContextManager2.class);
	}*/

	/*public void terminateCustomContexts(@Observes final BeforeShutdown event) {
		ContextManager.shutdown();
	}*/

	public void takeOff(@Observes final AfterDeploymentValidation event) {
		getLogger().info(getBundle().getString("taking-off"));
	}

	public void engineOff(@Observes final BeforeShutdown event) {
		getLogger().info(getBundle().getString("engine-off"));
	}
}
