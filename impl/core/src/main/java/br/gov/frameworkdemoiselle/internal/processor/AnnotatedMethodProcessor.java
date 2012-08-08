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
package br.gov.frameworkdemoiselle.internal.processor;

import java.lang.reflect.InvocationTargetException;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.BeanManager;

import br.gov.frameworkdemoiselle.exception.ApplicationException;
import br.gov.frameworkdemoiselle.message.SeverityType;

/**
 * Represents an annotated method to be processed;
 * 
 * @param <DC>
 *            declaring class owner of the method
 */
public class AnnotatedMethodProcessor<DC> extends AbstractProcessor<DC> {

	public AnnotatedMethodProcessor(final AnnotatedMethod<DC> annotatedMethod, final BeanManager beanManager) {
		super(annotatedMethod, beanManager);
	}

	public AnnotatedMethod<DC> getAnnotatedMethod() {
		return (AnnotatedMethod<DC>) getAnnotatedCallable();
	}

	public boolean process(Object... args) throws Throwable {
		getLogger().info(getBundle().getString("processing", getAnnotatedMethod().getJavaMember().toGenericString()));

		try {
			getAnnotatedMethod().getJavaMember().invoke(getReferencedBean(), args);

		} catch (InvocationTargetException cause) {
			handleException(cause.getCause());
		}

		return true;
	}

	private void handleException(Throwable cause) throws Throwable {
		ApplicationException ann = cause.getClass().getAnnotation(ApplicationException.class);

		if (ann == null || SeverityType.FATAL == ann.severity()) {
			throw cause;

		} else {
			switch (ann.severity()) {
				case INFO:
					getLogger().info(cause.getMessage());
					break;

				case WARN:
					getLogger().warn(cause.getMessage());
					break;

				default:
					getLogger().error(getBundle().getString("processing-fail"), cause);
					break;
			}
		}
	}

	@Override
	public String toString() {
		return getBundle().getString("for", getClass().getSimpleName(),
				getAnnotatedMethod().getJavaMember().toGenericString());
	}

}
