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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides some features to do some operations relating to java reflection.
 * 
 * @author SERPRO
 */
public final class Reflections {

	private Reflections() {
	}

	/**
	 * TODO 
	 */
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

	/**
	 * TODO 
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getGenericTypeArgument(final Field field, final int idx) {
		final Type type = field.getGenericType();
		final ParameterizedType paramType = (ParameterizedType) type;

		return (Class<T>) paramType.getActualTypeArguments()[idx];
	}

	/**
	 * TODO 
	 */
	public static <T> Class<T> getGenericTypeArgument(final Member member, final int idx) {
		Class<T> result = null;

		if (member instanceof Field) {
			result = getGenericTypeArgument((Field) member, idx);
		} else if (member instanceof Method) {
			result = getGenericTypeArgument((Method) member, idx);
		}

		return result;
	}

	/**
	 * TODO 
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getGenericTypeArgument(final Method method, final int pos) {
		return (Class<T>) method.getGenericParameterTypes()[pos];
	}

	/**
	 * Returns the value contained in the given field. 
	 * 
	 * @param field
	 * 			field to be extracted the value.
	 * @param object
	 * 			object that contains the field.
	 * @return value of the field.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Field field, Object object) {
		T result = null;

		try {
			boolean acessible = field.isAccessible();
			field.setAccessible(true);
			result = (T) field.get(object);
			field.setAccessible(acessible);

		} catch (Exception e) {
			Exceptions.handleToRuntimeException(e);
		}

		return result;
	}

	/**
	 * Sets a value in a field.
	 * 
	 * @param field
	 * 			field to be setted.
	 * @param object
	 * 			object that contains the field.
	 * @param value
	 * 			value to be setted in the field.
	 */
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

	/**
	 * TODO 
	 */
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

	/**
	 * TODO 
	 */
	public static List<Field> getNonStaticFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();

		if (type != null) {
			fields.addAll(Arrays.asList(getNonStaticDeclaredFields(type)));
			fields.addAll(getNonStaticFields(type.getSuperclass()));
		}

		return fields;
	}

	/**
	 * TODO 
	 */
	public static <T> T instantiate(Class<T> clazz) {
		T object = null;
		try {
			object = clazz.newInstance();
		} catch (InstantiationException e) {
			Exceptions.handleToRuntimeException(e);
		} catch (IllegalAccessException e) {
			Exceptions.handleToRuntimeException(e);
		}
		return object;
	}

	/**
	 * Verifies if a given class could be converted to a given type. 
	 * 
	 * @param clazz
	 * 			class to be checked.
	 * @param type
	 * 			type to be checked.
	 * @return {@link Boolean}
	 * 			true if the given class can be converted to a given type, and false otherwise.
	 */
	public static boolean isOfType(Class<?> clazz, Class<?> type) {
		return type.isAssignableFrom(clazz) && clazz != type;
	}

	/**
	 * Obtains the {@link ClassLoader} for the given class, from his canonical name.
	 * 
	 * @param canonicalName
	 * 			canonical name of the the given class.
	 * @return {@link ClassLoader}
	 * 			ClassLoader for the given class.
	 */
	public static ClassLoader getClassLoaderForClass(final String canonicalName) {
		return Reflections.getClassLoaderForResource(canonicalName.replaceAll("\\.", "/") + ".class");
	}

	/**
	 * Obtains the {@link ClassLoader} for the given resource.
	 * 
	 * @param resource
	 * 			
	 * @return {@link ClassLoader}
	 * 			ClassLoader for the given resource.
	 */
	public static ClassLoader getClassLoaderForResource(final String resource) {
		final String stripped = resource.charAt(0) == '/' ? resource.substring(1) : resource;

		URL url = null;
		ClassLoader result = Thread.currentThread().getContextClassLoader();

		if (result != null) {
			url = result.getResource(stripped);
		}

		if (url == null) {
			result = Reflections.class.getClassLoader();
			url = Reflections.class.getClassLoader().getResource(stripped);
		}

		if (url == null) {
			result = null;
		}

		return result;
	}

	/**
	 * TODO 
	 */
	public static URL getResourceAsURL(final String resource) {
		ClassLoader classLoader = getClassLoaderForResource(resource);
		return classLoader != null ? classLoader.getResource(resource) : null;
	}

	/**
	 * TODO 
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> forName(final String className) throws ClassNotFoundException {
		ClassLoader classLoader = getClassLoaderForClass(className);
		return (Class<T>) Class.forName(className, true, classLoader);
	}
}
