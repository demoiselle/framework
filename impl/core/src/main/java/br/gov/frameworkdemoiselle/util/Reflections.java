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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.ldap.exception.EntryException;

public class Reflections {

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getGenericTypeArgument(final Class<?> clazz, final int idx) {
		final Type type = clazz.getGenericSuperclass();

		ParameterizedType paramType;
		try {
			paramType = (ParameterizedType) type;
		} catch (ClassCastException cause) {
			paramType = (ParameterizedType) ((Class<T>) type).getGenericSuperclass();
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

	/**
	 * Build a super classes List<Class<?>>
	 * 
	 * @param entry
	 * @return List of Super Classes
	 */
	public static List<Class<?>> getSuperClasses(Class<?> beanClass) {
		List<Class<?>> list = new ArrayList<Class<?>>();
		Class<?> superClazz = beanClass.getSuperclass();
		while (superClazz != null) {
			list.add(superClazz);
			superClazz = superClazz.getSuperclass();
		}
		return list;
	}

	/**
	 * Build a array of super classes fields and include entryClass fields
	 * 
	 * @param entry
	 * @return Array of Super Classes Fields
	 */
	public static Field[] getSuperClassesFields(Class<?> beanClass) {
		Field[] fieldArray = null;
		fieldArray = (Field[]) ArrayUtils.addAll(fieldArray, beanClass.getDeclaredFields());
		Class<?> superClazz = beanClass.getSuperclass();
		while (superClazz != null && !"java.lang.Object".equals(superClazz.getName())) {
			fieldArray = (Field[]) ArrayUtils.addAll(fieldArray, superClazz.getDeclaredFields());
			superClazz = superClazz.getSuperclass();
		}
		return fieldArray;
	}

	/**
	 * Verify if annotation is present
	 * 
	 * @param entry
	 * @param clazz
	 */
	public static boolean isAnnotationPresent(Class<?> beanClass, Class<? extends Annotation> clazz) {
		for (Class<?> claz : getSuperClasses(beanClass))
			if (claz.isAnnotationPresent(clazz))
				return true;
		return false;
	}

	/**
	 * Verify if annotation is present on entry and when false throw
	 * DemoiselleException
	 * 
	 * @param entry
	 * @param clazz
	 */
	public static void requireAnnotation(Class<?> beanClass, Class<? extends Annotation> clazz) {
		if (!isAnnotationPresent(beanClass, clazz))
			throw new DemoiselleException("Class " + beanClass.getSimpleName() + " and yours superclasses doesn't have @" + clazz.getName());
	}

	/**
	 * If @Name present returns field.getAnnotation(Name.class).value(),
	 * otherwise field.getName();
	 * 
	 * @param field
	 * @return @Name annotation value or object attribute name;
	 */
	public static String getFieldName(Field field) {
		if (field.isAnnotationPresent(Name.class)) {
			String name = field.getAnnotation(Name.class).value();
			if (StringUtils.isBlank(name))
				throw new DemoiselleException("Annotation @Name must have a value");
			return name;
		} else
			return field.getName();
	}


	/**
	 * Get Field with annotation
	 * 
	 * @param claz
	 * @param clazz
	 */
	public static Field getFieldAnnotatedAs(Class<?> claz, Class<? extends Annotation> clazz) {
		Field[] fields = getSuperClassesFields(claz);
		for (Field field : fields)
			if (field.isAnnotationPresent(clazz))
				return field;
		return null;
	}

	/**
	 * Get Field Value with annotation
	 * 
	 * @param object
	 * @param clazz
	 */
	public static Object getAnnotatedValue(Object object, Class<? extends Annotation> clazz) {
		Field field = getFieldAnnotatedAs(object.getClass(), clazz);
		if (field != null)
			return getFieldValue(field, object);
		return null;
	}

	/**
	 * Get annotated value and when null throw EntryException
	 * 
	 * @param object
	 * @param clazz
	 * @return
	 */
	public static Object getRequiredAnnotatedValue(Object object, Class<? extends Annotation> clazz) {
		Object value = getAnnotatedValue(object, clazz);
		if (value != null && !value.toString().trim().isEmpty()) {
			return value;
		}
		throw new DemoiselleException("Class " + object.getClass().getSimpleName() + " doesn't have a valid value for @" + clazz.getSimpleName());
	}
	
}
