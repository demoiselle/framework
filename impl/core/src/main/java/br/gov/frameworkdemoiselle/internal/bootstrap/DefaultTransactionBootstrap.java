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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import br.gov.frameworkdemoiselle.transaction.Transaction;
import br.gov.frameworkdemoiselle.util.Reflections;

//public class DefaultTransactionBootstrap extends AbstractVetoBootstrap<Transaction, DefaultTransaction> {
public class DefaultTransactionBootstrap implements Extension {

	private static void x() {
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class<?> clClass = cl.getClass();

			while (clClass != ClassLoader.class) {
				clClass = clClass.getSuperclass();
			}

			Field field = clClass.getDeclaredField("classes");

			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			// Vector<Class<?>> classes = (Vector<Class<?>>) field.get(cl);
			List<Class<?>> classes = new ArrayList<Class<?>>((List<Class<?>>) field.get(cl));
			// List<Class<?>> classes = (List<Class<?>>) field.get(cl);
			field.setAccessible(false);

			// for (Iterator<Class<?>> iter = classes.iterator(); iter.hasNext();) {
			// Class<?> clazz = iter.next();
			//
			// if (Reflections.isOfType(clazz, Transaction.class)) {
			// System.out.println("......... " + clazz);
			// }
			// }

			for (Class<?> clazz : classes) {
				if (Reflections.isOfType(clazz, Transaction.class)) {
					// if (Transaction.class.isAssignableFrom(clazz) && clazz != Transaction.class) {
					System.out.println("......... " + clazz);
				}
			}

		} catch (Exception cause) {
			cause.printStackTrace();
		}
	}

	// private static Class<?> cache;

	 public void beforeBeanDiscovery(@Observes final ProcessAnnotatedType<?> event) {

//	public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event) {
		
		x();
		System.out.println();

//		ClassLoader cl

		// private static ProcessAnnotatedType<?> cachedEvent;
		//
		// protected <A> void processAnnotatedType(@Observes final ProcessAnnotatedType<A> event) {
		// Class<?> annotated = event.getAnnotatedType().getJavaClass();
		//
		// if (Reflections.isOfType(annotated, Transaction.class)) {
		//
		// if(cachedEvent == null) {
		// cachedEvent = event;
		//
		// } else {
		// Class<?> cachedClass = cachedEvent.getAnnotatedType().getJavaClass();
		//
		// if (getPriority(annotated) > getPriority(cachedClass)) {
		// cachedEvent.veto();
		// cachedEvent = event;
		//
		// } else {
		// event.veto();
		// }
		// }
		// }
		// }
		//
		// private int getPriority(Class<?> type) {
		// int priority = Priority.MAX_PRIORITY;
		//
		// if (type == null) {
		// priority = Priority.MIN_PRIORITY;
		//
		// } else if (type.isAnnotationPresent(Priority.class)) {
		// Priority annotation = type.getAnnotation(Priority.class);
		// priority = annotation.value();
		// }
		//
		// return priority;
	}

}
