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

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.transaction.Transaction;
import br.gov.frameworkdemoiselle.util.Reflections;

public abstract class AbstractTransactionBootstrap<T extends Transaction> extends AbstractBootstrap {

	private Class<T> transactionClass;

	protected <A> void processAnnotatedType(@Observes final ProcessAnnotatedType<A> event) {
		Class<?> annotated = event.getAnnotatedType().getJavaClass();

		if (Reflections.isOfType(annotated, Transaction.class) && annotated != getTransactionClass()
				&& getPriority(getTransactionClass()) < getPriority(annotated)) {
			event.veto();
		}
	}

	private int getPriority(Class<?> type) {
		int priority = Priority.MAX_PRIORITY;

		if (type.isAnnotationPresent(Priority.class)) {
			Priority annotation = type.getAnnotation(Priority.class);
			priority = annotation.value();
		}

		return priority;
	}

	protected Class<T> getTransactionClass() {
		if (this.transactionClass == null) {
			this.transactionClass = Reflections.getGenericTypeArgument(this.getClass(), 0);
		}
		return this.transactionClass;
	}
}
