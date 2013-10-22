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

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Extension;

import br.gov.frameworkdemoiselle.internal.context.CustomContextProducer;
import br.gov.frameworkdemoiselle.internal.context.FacesViewContextImpl;
import br.gov.frameworkdemoiselle.lifecycle.AfterShutdownProccess;
import br.gov.frameworkdemoiselle.util.Beans;

public class JsfBootstrap implements Extension {

	//private List<CustomContext> customContexts = new ArrayList<CustomContext>();

	//private AfterBeanDiscovery afterBeanDiscoveryEvent;
	
	private FacesViewContextImpl context;
	private boolean contextActivatedHere;
	
	public void createCustomContext(@Observes AfterBeanDiscovery event){
		context = new FacesViewContextImpl();
		event.addContext(context);
	}

	public void addContexts(@Observes final AfterDeploymentValidation event) {
		CustomContextProducer producer = Beans.getReference(CustomContextProducer.class);
		producer.addRegisteredContext(context);
		
		//Ativa o ViewContext
		if (!context.isActive()){
			contextActivatedHere = context.activate();
		}
		else{
			contextActivatedHere = false;
		}
	}

	public void removeContexts(@Observes AfterShutdownProccess event) {
		//Desativa o ViewContext
		if (contextActivatedHere){
			context.deactivate();
		}
	}
}
