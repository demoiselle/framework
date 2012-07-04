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
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import br.gov.frameworkdemoiselle.internal.implementation.DefaultTransaction;
import br.gov.frameworkdemoiselle.transaction.Transaction;

public class TransactionBootstrap extends AbstractBootstrap {

	public <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> event) throws ConfigurationException {

		Configuration config = new PropertiesConfiguration("demoiselle.properties");
		String selected = config.getString("frameworkdemoiselle.transaction.class");

		Class<?> type = event.getAnnotatedType().getJavaClass();
		if (Transaction.class.isAssignableFrom(type) && type != Transaction.class && type != DefaultTransaction.class) {
			if (selected != null && !selected.equals(type.getCanonicalName())) {
				event.veto();
			}
		}

		// final AnnotatedType<T> annotatedType = event.getAnnotatedType();
		// for (AnnotatedMethod<?> am : annotatedType.getMethods()) {
		// if (am.isAnnotationPresent(annotationClass)) {
		// @SuppressWarnings("unchecked")
		// AnnotatedMethod<T> annotatedMethod = (AnnotatedMethod<T>) am;
		// processors.add(new StartupProcessor<T>(annotatedMethod, beanManager));
		// }
		// }
	}
}
