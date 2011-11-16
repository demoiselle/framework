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

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.util.Beans;

//TODO Inter [NQ]: verificar o melhor nome para todas as classes desta natureza. 
public class CoreBootstrap extends AbstractBootstrap {

	private static final Map<Class<?>, AnnotatedType<?>> beans = new HashMap<Class<?>, AnnotatedType<?>>();

	public static boolean isAnnotatedType(Class<?> type) {
		return beans.containsKey(type);
	}

	public void engineOn(@Observes final BeforeBeanDiscovery event, BeanManager beanManager) {
		String description;
		Logger log = getLogger();

		description = getBundle("demoiselle-core-bundle").getString("engine-on");
		log.info(description);

		Beans.setBeanManager(beanManager);

		description = getBundle("demoiselle-core-bundle").getString("setting-up-bean-manager",
				Beans.class.getCanonicalName());
		log.info(description);
	}

	protected <T> void detectAnnotation(@Observes final ProcessAnnotatedType<T> event) {
		beans.put(event.getAnnotatedType().getJavaClass(), event.getAnnotatedType());
	}

	public static void takeOff(@Observes final AfterDeploymentValidation event) {
		String description = getBundle("demoiselle-core-bundle").getString("taking-off");

		Logger log = getLogger();
		log.info(description);
	}

	public static void engineOff(@Observes final BeforeShutdown event) {
		String description = getBundle("demoiselle-core-bundle").getString("engine-off");

		Logger log = getLogger();
		log.info(description);
	}
}
