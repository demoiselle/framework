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
package br.gov.frameworkdemoiselle.internal.management;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;

import javax.inject.Qualifier;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.ManagedOperation;
import br.gov.frameworkdemoiselle.annotation.ManagedProperty;
import br.gov.frameworkdemoiselle.annotation.ManagedProperty.ManagedPropertyAccess;
import br.gov.frameworkdemoiselle.annotation.OperationParameter;
import br.gov.frameworkdemoiselle.annotation.OperationType;
import br.gov.frameworkdemoiselle.stereotype.ManagementController;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * <p>Package class containing information about a discovered {@link ManagementController}.</p>
 * 
 * <p>Instances if this class are passed to each discovered management extension during bootstrap so they can have
 * enough information to expose all discovered {@link ManagementController}'s to management clients.</p>
 * 
 * @author serpro
 */
public class ManagedType {

	private Class<?> type;
	
	private Annotation[] qualifiers;

	private TreeMap<String, FieldDetail> fields;

	private TreeMap<String, MethodDetail> operationMethods;

	private ResourceBundle bundle;

	private String description;

	public ManagedType(Class<?> type) {
		bundle = new ResourceBundle("demoiselle-core-bundle",Locale.getDefault());

		if (type == null) {
			throw new DemoiselleException(bundle.getString("management-null-class-defined"));
		}
		if (!type.isAnnotationPresent(ManagementController.class)) {
			throw new DemoiselleException(bundle.getString("management-no-annotation-found", type.getCanonicalName()));
		}

		this.type = type;
		fields = new TreeMap<String, FieldDetail>();
		operationMethods = new TreeMap<String, MethodDetail>();
		this.description = type.getAnnotation(ManagementController.class).description();
		this.qualifiers = getQualifierAnnotations(type);

		initialize();
	}

	public Class<?> getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public TreeMap<String, FieldDetail> getFields() {
		return fields;
	}

	public TreeMap<String, MethodDetail> getOperationMethods() {
		return operationMethods;
	}
	
	/**
	 * <p>Return a (possibly empty) list of all qualifiers this type have. Qualifiers
	 * are any annotations marked as {@link Qualifier}.</p>
	 * 
	 * <p>This method returns the true list of qualifiers. If implementators change this list, it will
	 * affect future calls of this method. This is so that resources can be spared by not creating many instances of this list.</p> 
	 * 
	 */
	public Annotation[] getQualifiers(){
		return this.qualifiers;
	}

	private void initialize() {
		// Para cada atributo verifica se ele está anotado com ManagedProperty e extrai as informações dele (método get, set e
		// descrição do atributo).
		Field[] fields = type.getDeclaredFields();
		if (fields != null) {
			for (Field field : fields) {
				if (field.isAnnotationPresent(ManagedProperty.class)) {
					// Obtém os métodos GET e SET para esta propriedade
					Method getterMethod = getGetterMethod(field);
					Method setterMethod = getSetterMethod(field);
					if (getterMethod == null && setterMethod == null) {
						throw new DemoiselleException(bundle.getString("management-invalid-property-no-getter-setter",
								type.getSimpleName(), field.getName()));
					} else if ((getterMethod != null && getterMethod.isAnnotationPresent(ManagedOperation.class))
							|| (setterMethod != null && setterMethod.isAnnotationPresent(ManagedOperation.class))) {
						throw new DemoiselleException(bundle.getString("management-invalid-property-as-operation",
								type.getSimpleName()));
					}

					String propertyDescription = field.getAnnotation(ManagedProperty.class).description();

					this.fields.put(field.getName(), new FieldDetail(field, propertyDescription, getterMethod,
							setterMethod));
				}
			}
		}

		// Para cada metodo verifica se ele está anotado com ManagedOperation e cria um MBeanOperationInfo para ele.
		Method[] methodList = type.getMethods();
		if (methodList != null) {
			for (Method method : methodList) {
				ManagedOperation opAnnotation = method.getAnnotation(ManagedOperation.class);

				if (opAnnotation != null) {
					// Lemos as informações sobre o método e criamos uma instância
					// de MethodDetail para representar este método como uma
					// operação.

					Class<?>[] parameterTypes = method.getParameterTypes();
					Annotation[][] parameterAnnotations = method.getParameterAnnotations();
					ParameterDetail[] parameterDetails = new ParameterDetail[parameterTypes.length];
					OperationType operationType = opAnnotation.type();

					for (int i = 0; i < parameterTypes.length; i++) {
						OperationParameter paramAnnotation = null;
						for (Annotation annotation : parameterAnnotations[i]) {
							if (annotation.annotationType() == OperationParameter.class) {
								paramAnnotation = (OperationParameter) annotation;
								break;
							}
						}

						String name = paramAnnotation != null ? paramAnnotation.name() : ("arg" + i);
						String description = paramAnnotation != null ? paramAnnotation.description() : null;

						parameterDetails[i] = new ParameterDetail(parameterTypes[i], name, description);
					}

					// Com todas as informações, criamos nossa instância de MethodDetail e
					// acrescentamos na lista de todas as operações.
					MethodDetail detail = new MethodDetail(method, opAnnotation.description(), operationType, parameterDetails);
					operationMethods.put(method.getName(), detail);
				}
			}
		}
	}

	/**
	 * Returns the public getter method for a given field, or <code>null</code> if no getter method can be found.
	 */
	private Method getGetterMethod(Field field) {
		StringBuffer getterMethodName = new StringBuffer()
			.append("get")
			.append(field.getName().substring(0, 1).toUpperCase())
			.append(field.getName().substring(1));
		
		//Se propriedade está anotada como WRITE-ONLY, ignora essa etapa.
		ManagedProperty annotation = field.getAnnotation(ManagedProperty.class);
		if (annotation.accessLevel() == ManagedPropertyAccess.WRITE_ONLY){
			return null;
		}

		Method getterMethod;

		try {
			getterMethod = type.getMethod(getterMethodName.toString());
		} catch (Exception e) {
			getterMethod = null;
		}

		// Se atributo for boolean, procura método getter no formato "isAttribute".
		if (getterMethod == null
				&& (Boolean.TYPE.isAssignableFrom(field.getType()) || Boolean.class.isAssignableFrom(field.getType()))) {
			// Boolean.TYPE representa o tipo primitivo "boolean", Boolean.class é a classe wrapper.
			getterMethodName = new StringBuffer()
				.append("is")
				.append(field.getName().substring(0, 1).toUpperCase())
				.append(field.getName().substring(1).toUpperCase());

			try {
				getterMethod = type.getMethod(getterMethodName.toString());
			} catch (Exception e) {
				getterMethod = null;
			}
		}

		return getterMethod;
	}

	/**
	 * Returns the public setter method for a given field, or <code>null</code> if no setter method can be found.
	 */
	private Method getSetterMethod(Field field) {
		
		//Se propriedade está anotada como READ-ONLY, ignora essa etapa.
		ManagedProperty annotation = field.getAnnotation(ManagedProperty.class);
		if (annotation.accessLevel() == ManagedPropertyAccess.READ_ONLY){
			return null;
		}
		
		StringBuffer setterMethodName = new StringBuffer()
			.append("set")
			.append(field.getName().substring(0, 1).toUpperCase())
			.append(field.getName().substring(1));

		Method setterMethod;

		try {
			setterMethod = type.getMethod(setterMethodName.toString() , field.getType());
		} catch (Exception e) {
			setterMethod = null;
		}

		return setterMethod;
	}
	
	/**
	 * Indicates another {@link ManagedType} represents the same {@link Class} as this one. This method also supports a
	 * {@link Class} as a parameter, in this case it will return <code>true</code> if the passed class is exactly the
	 * same Java class represented by this {@link ManagedType}.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}

		return ((ManagedType) other).getType().getCanonicalName().equals(this.getType().getCanonicalName());
	}
	
	private synchronized Annotation[] getQualifierAnnotations(Class<?> beanClass){
		Annotation[] annotations = beanClass.getAnnotations();
		ArrayList<Annotation> qualifiers = new ArrayList<Annotation>(annotations.length);

		for (int i=0; i<annotations.length; i++){
			if (annotations[i].annotationType().getAnnotation(Qualifier.class) != null){
				qualifiers.add(annotations[i]);
			}
		}
		
		return qualifiers.toArray(new Annotation[0]);
	}

	public final class FieldDetail {

		private final Field field;

		private final String description;

		private Method getterMethod;

		private Method setterMethod;

		public FieldDetail(Field field, String description, Method getterMethod, Method setterMethod) {
			super();
			this.field = field;
			this.description = description;
			this.getterMethod = getterMethod;
			this.setterMethod = setterMethod;
		}

		public Field getField() {
			return field;
		}

		public String getDescription() {
			return description;
		}

		public Method getGetterMethod() {
			return getterMethod;
		}

		public Method getSetterMethod() {
			return setterMethod;
		}

	}

	public final class MethodDetail {

		private final Method method;

		private final ParameterDetail[] parameterTypers;

		private final String description;
		
		private final OperationType type;

		public MethodDetail(Method method, String description, OperationType type,ParameterDetail[] parameterTypers) {
			super();
			this.method = method;
			this.description = description;
			this.type = type;
			this.parameterTypers = parameterTypers;
		}

		public Method getMethod() {
			return method;
		}

		public ParameterDetail[] getParameterTypers() {
			return parameterTypers;
		}

		public String getDescription() {
			return description;
		}

		public OperationType getType() {
			return type;
		}
		
	}

	public final class ParameterDetail {

		private final Class<?> parameterType;

		private final String parameterName;

		private final String parameterDescription;

		public ParameterDetail(Class<?> parameterType, String parameterName, String parameterDescription) {
			super();
			this.parameterType = parameterType;
			this.parameterName = parameterName;
			this.parameterDescription = parameterDescription;
		}

		public Class<?> getParameterType() {
			return parameterType;
		}

		public String getParameterName() {
			return parameterName;
		}

		public String getParameterDescription() {
			return parameterDescription;
		}
	}
}
