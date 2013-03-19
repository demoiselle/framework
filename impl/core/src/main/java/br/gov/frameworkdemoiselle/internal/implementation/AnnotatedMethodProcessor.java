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

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import javax.enterprise.inject.spi.AnnotatedMethod;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.exception.ApplicationException;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.message.SeverityType;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Represents an annotated method to be processed;
 * 
 * @param <T>
 *            declaring class owner of the method
 */
public class AnnotatedMethodProcessor<T> implements Comparable<AnnotatedMethodProcessor<T>> {

	private AnnotatedMethod<T> annotatedMethod;

	private ResourceBundle bundle;

	public AnnotatedMethodProcessor(final AnnotatedMethod<T> annotatedMethod) {
		this.annotatedMethod = annotatedMethod;
	}

	public AnnotatedMethod<T> getAnnotatedMethod() {
		return this.annotatedMethod;
	}

	@SuppressWarnings("unchecked")
	protected T getReferencedBean() {
		Class<T> classType = (Class<T>) getAnnotatedMethod().getJavaMember().getDeclaringClass();

		return Beans.getReference(classType);
	}

	public int compareTo(final AnnotatedMethodProcessor<T> other) {
		Integer orderThis = getPriority(getAnnotatedMethod());
		Integer orderOther = getPriority(other.getAnnotatedMethod());

		return orderThis.compareTo(orderOther);
	}

	public boolean process(Object... args) throws Exception {
		getLogger().info(getBundle().getString("processing", getAnnotatedMethod().getJavaMember().toGenericString()));

		try {
			getAnnotatedMethod().getJavaMember().invoke(getReferencedBean(), args);

		} catch (InvocationTargetException cause) {
			handleException(cause.getCause());
		}

		return true;
	}

	private void handleException(Throwable cause) throws Exception {
		ApplicationException ann = cause.getClass().getAnnotation(ApplicationException.class);

		if (ann == null || SeverityType.FATAL == ann.severity()) {
			throw (cause instanceof Exception ? (Exception) cause : new Exception(cause));

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

	private static <T> Integer getPriority(AnnotatedMethod<T> annotatedMethod) {
		Integer priority = Priority.MIN_PRIORITY;

		Priority annotation = annotatedMethod.getAnnotation(Priority.class);
		if (annotation != null) {
			priority = annotation.value();
		}

		return priority;
	}

	// @Override
	// public String toString() {
	// return getBundle().getString("for", getClass().getSimpleName(),
	// getAnnotatedMethod().getJavaMember().toGenericString());
	// }

	protected ResourceBundle getBundle() {
		if (this.bundle == null) {
			this.bundle = ResourceBundleProducer.create("demoiselle-core-bundle", Locale.getDefault());
		}

		return bundle;
	}

	protected Logger getLogger() {
		return LoggerProducer.create(this.getClass());
	}
}
