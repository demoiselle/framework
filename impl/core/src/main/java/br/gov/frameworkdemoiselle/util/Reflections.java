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
package br.gov.frameworkdemoiselle.util;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class Reflections {

	private Reflections() {
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getGenericTypeArgument(final Class<?> clazz, final int idx) {
		final Type type = clazz.getGenericSuperclass();

		ParameterizedType paramType;
		try {
			paramType = (ParameterizedType) type;
		} catch (ClassCastException cause) {
			return getGenericTypeArgument((Class<T>) type, idx);
		}

		return (Class<T>) paramType.getActualTypeArguments()[idx];
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getGenericTypeArgument(final Field field, final int idx) {
		final Type type = field.getGenericType();
		final ParameterizedType paramType = (ParameterizedType) type;

		return (Class<T>) paramType.getActualTypeArguments()[idx];
	}

	public static <T> Class<T> getGenericTypeArgument(final Member member, final int idx) {
		Class<T> result = null;

		if (member instanceof Field) {
			result = getGenericTypeArgument((Field) member, idx);
		} else if (member instanceof Method) {
			result = getGenericTypeArgument((Method) member, idx);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getGenericTypeArgument(final Method method, final int pos) {
		return (Class<T>) method.getGenericParameterTypes()[pos];
	}

	public static Object getFieldValue(Field field, Object object) {
		Object result = null;

		try {
			boolean acessible = field.isAccessible();
			field.setAccessible(true);
			result = field.get(object);
			field.setAccessible(acessible);

		} catch (Exception e) {
			Exceptions.handleToRuntimeException(e);
		}

		return result;
	}

	public static void setFieldValue(Field field, Object object, Object value) {
		try {
			boolean acessible = field.isAccessible();
			field.setAccessible(true);
			field.set(object, value);
			field.setAccessible(acessible);

		} catch (Exception e) {
			Exceptions.handleToRuntimeException(e);
		}
	}

	public static Field[] getNonStaticDeclaredFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();

		if (type != null) {
			for (Field field : type.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers()) && !field.getType().equals(type.getDeclaringClass())) {
					fields.add(field);
				}
			}
		}

		return fields.toArray(new Field[0]);
	}

	public static <T> T instantiate(Class<T> clasz) {
		T object = null;
		try {
			object = clasz.newInstance();
		} catch (InstantiationException e) {
			Exceptions.handleToRuntimeException(e);
		} catch (IllegalAccessException e) {
			Exceptions.handleToRuntimeException(e);
		}
		return object;
	}

	public static boolean isOfType(Class<?> clazz, Class<?> type) {
		return type.isAssignableFrom(clazz) && clazz != type;
	}
}
