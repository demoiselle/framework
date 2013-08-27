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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public final class StrategySelector implements Serializable {

	private static final long serialVersionUID = 1L;

	private StrategySelector() {
	}

	@SuppressWarnings("unchecked")
	public static <T> T selectInstance(Class<T> type, Collection<? extends T> options) {

		Map<Class<? extends T>, T> map = new HashMap<Class<? extends T>, T>();

		for (T instance : options) {
			if (instance != null) {
				map.put((Class<T>) instance.getClass(), instance);
			}
		}

		Class<? extends T> elected = selectClass(type, map.keySet());
		return map.get(elected);
	}

	private static <T> Class<? extends T> selectClass(Class<T> type, Collection<Class<? extends T>> options) {
		Class<? extends T> selected = null;

		for (Class<? extends T> option : options) {
			if (selected == null || getPriority(option) < getPriority(selected)) {
				selected = option;
			}
		}

		if (selected != null) {
			performAmbiguityCheck(type, selected, options);
		}

		return selected;
	}

	public static <T> Class<? extends T> selectClass(Class<T> type) {
		return selectClass(type, getOptions(type));
	}

	@SuppressWarnings("unchecked")
	private static <T> Collection<Class<? extends T>> getOptions(Class<T> type) {
		Set<Class<? extends T>> result = new HashSet<Class<? extends T>>();

		for (Bean<?> bean : Beans.getBeanManager().getBeans(type)) {
			result.add((Class<? extends T>) bean.getBeanClass());
		}

		return result;
	}

	private static <T> void performAmbiguityCheck(Class<T> type, Class<? extends T> selected,
			Collection<Class<? extends T>> options) {
		int selectedPriority = getPriority(selected);

		List<Class<? extends T>> ambiguous = new ArrayList<Class<? extends T>>();

		for (Class<? extends T> option : options) {
			if (selected != option && selectedPriority == getPriority(option)) {
				ambiguous.add(option);
			}
		}

		if (!ambiguous.isEmpty()) {
			ambiguous.add(selected);

			String message = getExceptionMessage(type, ambiguous);
			throw new DemoiselleException(message, new AmbiguousResolutionException());
		}
	}

	private static <T> String getExceptionMessage(Class<T> type, List<Class<? extends T>> ambiguous) {
		StringBuffer classes = new StringBuffer();

		int i = 0;
		for (Class<? extends T> clazz : ambiguous) {
			if (i++ != 0) {
				classes.append(", ");
			}

			classes.append(clazz.getCanonicalName());
		}

		return getBundle().getString("ambiguous-strategy-resolution", type.getCanonicalName(), classes.toString());
	}

	private static <T> int getPriority(Class<T> type) {
		int result = Priority.MAX_PRIORITY;
		Priority priority = type.getAnnotation(Priority.class);

		if (priority != null) {
			result = priority.value();
		}

		return result;
	}

	private static ResourceBundle getBundle() {
		return Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle"));
	}
}
