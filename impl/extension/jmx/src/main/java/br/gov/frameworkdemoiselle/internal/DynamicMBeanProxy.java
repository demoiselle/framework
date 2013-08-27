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
package br.gov.frameworkdemoiselle.internal;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map.Entry;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.management.ManagedType;
import br.gov.frameworkdemoiselle.internal.management.ManagedType.FieldDetail;
import br.gov.frameworkdemoiselle.internal.management.ManagedType.MethodDetail;
import br.gov.frameworkdemoiselle.internal.management.ManagedType.ParameterDetail;
import br.gov.frameworkdemoiselle.internal.management.Management;
import br.gov.frameworkdemoiselle.management.ManagedAttributeNotFoundException;
import br.gov.frameworkdemoiselle.management.ManagedInvokationException;
import br.gov.frameworkdemoiselle.stereotype.ManagementController;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * <p>
 * This class is a MBean that gets registered everytime you mark a class with {@link ManagementController}. It dynamicaly reads the
 * fields and operations contained in a {@link ManagementController} class and exposes them to the MBean server. Everytime a client
 * tries to call an operation or read/write a property inside a ManagementController class, this class will call the appropriate
 * method and pass the result to the MBean client.
 * </p>
 * 
 * @author SERPRO
 */
public class DynamicMBeanProxy implements DynamicMBean {

	private MBeanInfo delegateInfo;
	
	private ManagedType managedType;

	private ResourceBundle bundle;

	public DynamicMBeanProxy(ManagedType type) {
		if (type == null) {
			throw new NullPointerException(getBundle().getString("mbean-null-type-defined"));
		}
		managedType = type;
	}

	@Override
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		// Se o bean ainda não foi lido para determinar seus atributos, o faz agora.
		if (delegateInfo == null) {
			initializeMBeanInfo();
		}

		Management manager = Beans.getReference(Management.class);
		
		try{
			return manager.getProperty(managedType, attribute);
		}
		catch(DemoiselleException de){
			if (ManagedAttributeNotFoundException.class.isInstance(de)){
				throw new AttributeNotFoundException(de.getMessage());
			}
			else if (ManagedInvokationException.class.isInstance(de)){
				throw new MBeanException(new Exception(de.getMessage()));
			}
			else{
				throw de;
			}
		}
	}

	@Override
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {

		// Se o bean ainda não foi lido para determinar seus atributos, o faz agora.
		if (delegateInfo == null) {
			initializeMBeanInfo();
		}

		Management manager = Beans.getReference(Management.class);
		
		try{
			manager.setProperty(managedType, attribute.getName(), attribute.getValue());
		}
		catch(DemoiselleException de){
			if (ManagedAttributeNotFoundException.class.isInstance(de)){
				throw new AttributeNotFoundException(de.getMessage());
			}
			else if (ManagedInvokationException.class.isInstance(de)){
				throw new MBeanException(new Exception(de.getMessage()));
			}
			else{
				throw de;
			}
		}
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		if (attributes != null) {
			AttributeList list = new AttributeList();
			for (String attribute : attributes) {
				try {
					Object value = getAttribute(attribute);
					list.add(new Attribute(attribute, value));
				} catch (Throwable t) {
				}
			}

			return list;
		}

		return null;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		AttributeList settedAttributes = new AttributeList();
		if (attributes != null) {
			for (Attribute attribute : attributes.asList()) {
				try {
					setAttribute(attribute);

					// A razão para separarmos a criação do atributo de sua adição na lista é que
					// caso a obtenção do novo valor do atributo dispare uma exceção então o atributo não será
					// adicionado na lista de atributos que foram afetados.
					Attribute attributeWithNewValue = new Attribute(attribute.getName(),
							getAttribute(attribute.getName()));
					settedAttributes.add(attributeWithNewValue);
				} catch (Throwable t) {
				}
			}
		}

		return settedAttributes;
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
			ReflectionException {

		// Se o bean ainda não foi lido para determinar seus atributos, o faz agora.
		if (this.delegateInfo == null) {
			initializeMBeanInfo();
		}

		Management manager = Beans.getReference(Management.class);
		
		try{
			return manager.invoke(managedType, actionName, params);
		}
		catch(DemoiselleException de){
			throw new MBeanException(new Exception(de.getMessage()));
		}
	}

	/**
	 * Initialize the Managed information for this instance of Managed
	 */
	private void initializeMBeanInfo() {
		// Aqui vamos armazenar nossos atributos
		ArrayList<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();

		// Aqui vamos armazenar nossas operações
		ArrayList<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();

		// Para cada propriedade descoberta no ManagementController, cria um attributeInfo correspondente
		for (Entry<String, FieldDetail> fieldEntry : managedType.getFields().entrySet()) {

			try {

				MBeanAttributeInfo attributeInfo = new MBeanAttributeInfo(fieldEntry.getKey(), fieldEntry.getValue()
						.getDescription(), fieldEntry.getValue().getGetterMethod(), fieldEntry.getValue()
						.getSetterMethod());
				attributes.add(attributeInfo);

			} catch (javax.management.IntrospectionException e) {
				throw new DemoiselleException(getBundle().getString("mbean-introspection-error", managedType.getType()
						.getSimpleName()));
			}
		}

		// Para cada operação descoberta no ManagementController, cria um operationInfo correspondente
		for (Entry<String, MethodDetail> methodEntry : managedType.getOperationMethods().entrySet()) {

			MethodDetail methodDetail = methodEntry.getValue();

			ParameterDetail[] parameterTypes = methodDetail.getParameterTypers();

			MBeanParameterInfo[] parameters = parameterTypes.length > 0 ? new MBeanParameterInfo[parameterTypes.length]
					: null;

			if (parameters != null) {

				for (int i = 0; i < parameterTypes.length; i++) {

					parameters[i] = new MBeanParameterInfo(parameterTypes[i].getParameterName(), parameterTypes[i]
							.getParameterType().getCanonicalName(), parameterTypes[i].getParameterDescription());
				}
			}

			// Com todas as informações, criamos nossa instância de MBeanOperationInfo e
			// acrescentamos na lista de todas as operações.
			int operationType = 0;
			switch(methodDetail.getType()){
				case ACTION:
					operationType = MBeanOperationInfo.ACTION;
					break;
					
				case INFO:
					operationType = MBeanOperationInfo.INFO;
					break;
					
				case ACTION_INFO:
					operationType = MBeanOperationInfo.ACTION_INFO;
					break;
					
				default:
					operationType = MBeanOperationInfo.UNKNOWN;
			}
			
			MBeanOperationInfo operation = new MBeanOperationInfo(methodDetail.getMethod().getName(),
					methodDetail.getDescription(), parameters, methodDetail.getMethod().getReturnType().getName(),
					operationType);

			operations.add(operation);

		}

		// Por fim criamos nosso bean info.
		delegateInfo = new MBeanInfo(managedType.getType().getCanonicalName(), managedType.getDescription(),
				attributes.toArray(new MBeanAttributeInfo[0]), null, operations.toArray(new MBeanOperationInfo[0]),
				null);

	}

	@Override
	public MBeanInfo getMBeanInfo() {
		if (delegateInfo == null) {
			initializeMBeanInfo();
		}

		return delegateInfo;
	}
	
	public ResourceBundle getBundle(){
		if (bundle==null){
			bundle = new ResourceBundle("demoiselle-jmx-bundle", Locale.getDefault());
		}
		
		return bundle;
	}

}
