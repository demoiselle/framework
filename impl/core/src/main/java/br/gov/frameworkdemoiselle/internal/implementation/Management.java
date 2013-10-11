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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.ManagedProperty;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.context.ConversationContext;
import br.gov.frameworkdemoiselle.context.RequestContext;
import br.gov.frameworkdemoiselle.context.SessionContext;
import br.gov.frameworkdemoiselle.context.ViewContext;
import br.gov.frameworkdemoiselle.internal.implementation.ManagedType.MethodDetail;
import br.gov.frameworkdemoiselle.management.AttributeChangeMessage;
import br.gov.frameworkdemoiselle.management.DefaultNotification;
import br.gov.frameworkdemoiselle.management.ManagedAttributeNotFoundException;
import br.gov.frameworkdemoiselle.management.ManagedInvokationException;
import br.gov.frameworkdemoiselle.management.ManagementExtension;
import br.gov.frameworkdemoiselle.management.Notification;
import br.gov.frameworkdemoiselle.management.NotificationManager;
import br.gov.frameworkdemoiselle.stereotype.ManagementController;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Central class used by management extensions to obtain information, access properties and call operations over
 * discovered {@link ManagementController} classes.
 * 
 * @author SERPRO
 */
@ApplicationScoped
public class Management implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	@Name("demoiselle-core-bundle")
	private ResourceBundle bundle;

	private final List<ManagedType> managedTypes = new ArrayList<ManagedType>();

	private Validator validator;

	public void addManagedType(ManagedType managedType) {
		managedTypes.add(managedType);
		logger.debug(bundle.getString("management-debug-registering-managed-type", managedType.getType()
				.getCanonicalName()));
	}

	/**
	 * @return List all discovered {@link ManagementController} classes. The returned list is a shallow copy of the
	 *         internal list, so you are free to modify it. TODO precisamos desse clone na lista?
	 */
	public List<ManagedType> getManagedTypes() {
		ArrayList<ManagedType> cloneList = new ArrayList<ManagedType>();
		cloneList.addAll(managedTypes);
		return cloneList;
	}

	/**
	 * <p>
	 * Invoke an operation over a {@link ManagementController}.
	 * </p>
	 * <p>
	 * This method is not thread-safe, it's the user's responsibility to make the operations of the managed type
	 * synchronized if necessary.
	 * </p>
	 * 
	 * @param managedType
	 *            A type annotated with {@link ManagementController}. This method will create an (or obtain an already
	 *            created) instance of this type and invoke the operation over it.
	 * @param actionName
	 *            AmbiguousQualifier of method to be invoked, the type must have this operation on it's list
	 * @param params
	 *            List of values for the operation parameters. Can be <code>null</code> if the operation require no
	 *            parameters.
	 * @return The return value of the original invoked operation. Methods of return type <code>void</code> will return
	 *         the {@link Void} type.
	 * @throws ManagedInvokationException
	 *             In case the operation doesn't exist or have a different signature
	 */
	public Object invoke(ManagedType managedType, String actionName, Object[] params) {
		if (managedTypes.contains(managedType)) {
			activateContexts(managedType.getType());

			try {
				Object delegate = Beans.getReference(managedType.getType() , managedType.getQualifiers());
				MethodDetail method = managedType.getOperationMethods().get(actionName);

				if (method != null) {
					try {
						logger.debug(bundle.getString("management-debug-invoking-operation", actionName, managedType
								.getType().getCanonicalName()));
						return method.getMethod().invoke(delegate, params);
					} catch (Exception e) {
						throw new ManagedInvokationException(bundle.getString("management-invoke-error", actionName), e);
					}
				} else {
					throw new ManagedInvokationException(bundle.getString("management-invoke-error", actionName));
				}
			} finally {
				deactivateContexts(managedType.getType());
			}
		} else {
			throw new ManagedInvokationException(bundle.getString("management-type-not-found"));
		}
	}

	/**
	 * <p>
	 * Retrieve the current value of a property from a managed type. Properties are attributes annotated with
	 * {@link ManagedProperty}.
	 * </p>
	 * <p>
	 * This method is not thread-safe, it's the user's responsibility to create the property's access methods from the
	 * managed type synchronized if necessary.
	 * </p>
	 * 
	 * @param managedType
	 *            The type that has the property the client wants to know the value of.
	 * @param propertyName
	 *            The name of the property
	 * @return The current value of the property
	 * @throws ManagedAttributeNotFoundException If the given property doesn't exist or there was a problem trying to read the property value.
	 * @throws ManagedInvokationException If there was an error trying to invoke the getter method to read the propery value. 
	 */
	public Object getProperty(ManagedType managedType, String propertyName) {

		if (managedTypes.contains(managedType)) {
			Method getterMethod = managedType.getFields().get(propertyName).getGetterMethod();

			if (getterMethod != null) {
				logger.debug(bundle.getString("management-debug-acessing-property", getterMethod.getName(), managedType
						.getType().getCanonicalName()));

				activateContexts(managedType.getType());

				try {
					Object delegate = Beans.getReference(managedType.getType() , managedType.getQualifiers());

					return getterMethod.invoke(delegate, (Object[]) null);
				} catch (Exception e) {
					throw new ManagedInvokationException(bundle.getString("management-invoke-error", getterMethod.getName()),
							e);
				} finally {
					deactivateContexts(managedType.getType());
				}
			} else {
				throw new ManagedAttributeNotFoundException(bundle.getString("management-read-value-error", propertyName));
			}
		} else {
			throw new ManagedInvokationException(bundle.getString("management-type-not-found"));
		}
	}

	/**
	 * <p>
	 * Sets a new value for a property contained inside a managed type. A property is an attribute annotated with
	 * {@link ManagedProperty}.
	 * </p>
	 * <p>
	 * This method is not thread-safe, it's the user's responsibility to create the property's access methods from the
	 * managed type synchronized if necessary.
	 * </p>
	 * 
	 * @param managedType
	 *            The type that has access to the property
	 * @param propertyName
	 *            The name of the property
	 * @param newValue
	 *            The new value of the property
	 * @throws ManagedInvokationException If there was an error trying to call the setter method for this property.
	 * @throws ManagedAttributeNotFoundException If the giver property doesn't exist or could'n be written to.
	 * @throws ConstraintViolationException If the property defined one or more validation constraints and setting this value violates some of those constraints.
	 */
	@SuppressWarnings("unchecked")
	public void setProperty(ManagedType managedType, String propertyName, Object newValue) {

		if (managedTypes.contains(managedType)) {
			// Procura o método set do atributo em questão
			Method method = managedType.getFields().get(propertyName).getSetterMethod();
			if (method != null) {
				logger.debug(bundle.getString("management-debug-setting-property", method.getName(), managedType
						.getType().getCanonicalName()));

				activateContexts(managedType.getType());
				try {
					// Obtém uma instância da classe gerenciada, lembrando que
					// classes
					// anotadas com @ManagementController são sempre singletons.
					Object delegate = Beans.getReference(managedType.getType() ,  managedType.getQualifiers() );

					// Se houver um validador anexado à propriedade alterada, executa o validador sobre
					// o novo valor.
					Validator validator = getDefaultValidator();
					if (validator != null) {
						Set<?> violations = validator.validateValue(managedType.getType(), propertyName, newValue);
						if (violations.size() > 0) {
							StringBuffer errorBuffer = new StringBuffer();
							for (Object objectViolation : violations) {
								ConstraintViolation<?> violation = (ConstraintViolation<?>) objectViolation;
								errorBuffer.append(violation.getMessage()).append('\r').append('\n');
							}

							if (errorBuffer.length() > 0) {
								errorBuffer.insert(0, "\r\n");
								errorBuffer.insert(errorBuffer.length(), "\r\n");
							}

							throw new ConstraintViolationException(bundle.getString("management-validation-constraint-violation"
										, managedType.getType().getCanonicalName(), propertyName, errorBuffer.toString())
									, (Set<ConstraintViolation<?>>) violations);
						}
					} else {
						logger.warn(bundle.getString("management-validation-validator-not-found"));
					}

					Method getterMethod = managedType.getFields().get(propertyName).getGetterMethod();
					Object oldValue;
					try {
						oldValue = getterMethod.invoke(delegate, (Object[]) null);
					} catch (Exception e) {
						oldValue = null;
					}

					method.invoke(delegate, new Object[] { newValue });

					// Manda uma notificação de mudança de atributo
					NotificationManager notificationManager = Beans.getReference(NotificationManager.class);
					Class<? extends Object> attributeType = newValue != null ? newValue.getClass() : null;

					Notification notification = new DefaultNotification( new AttributeChangeMessage(
							bundle.getString("management-notification-attribute-changed", propertyName, managedType.getType().getCanonicalName())
							, propertyName
							, attributeType
							, oldValue
							, newValue) );
					notificationManager.sendNotification(notification);

				} catch (ConstraintViolationException ce) {
					throw ce;
				} catch (Exception e) {
					throw new ManagedInvokationException(bundle.getString("management-invoke-error", method.getName()), e);
				} finally {
					deactivateContexts(managedType.getType());
				}

			} else {
				throw new ManagedAttributeNotFoundException(bundle.getString("management-write-value-error", propertyName));
			}
		} else {
			throw new ManagedInvokationException(bundle.getString("management-type-not-found"));
		}

	}

	private void activateContexts(Class<?> managedType) {
		
		RequestContext requestContext = Beans.getReference(RequestContext.class);
		ViewContext viewContext = Beans.getReference(ViewContext.class);
		SessionContext sessionContext = Beans.getReference(SessionContext.class);
		ConversationContext conversationContext = Beans.getReference(ConversationContext.class);
		
		if (!requestContext.isActive()){
			logger.debug(bundle.getString("management-debug-starting-custom-context",
					requestContext.getClass().getCanonicalName(), managedType.getCanonicalName()));
			
			requestContext.activate();
		}
		
		if (!viewContext.isActive()){
			logger.debug(bundle.getString("management-debug-starting-custom-context",
					viewContext.getClass().getCanonicalName(), managedType.getCanonicalName()));
			
			viewContext.activate();
		}

		if (!sessionContext.isActive()){
			logger.debug(bundle.getString("management-debug-starting-custom-context",
					sessionContext.getClass().getCanonicalName(), managedType.getCanonicalName()));
			
			sessionContext.activate();
		}
		
		if (!conversationContext.isActive()){
			logger.debug(bundle.getString("management-debug-starting-custom-context",
					conversationContext.getClass().getCanonicalName(), managedType.getCanonicalName()));
			
			conversationContext.activate();
		}
	}

	private void deactivateContexts(Class<?> managedType) {
		RequestContext requestContext = Beans.getReference(RequestContext.class);
		ViewContext viewContext = Beans.getReference(ViewContext.class);
		SessionContext sessionContext = Beans.getReference(SessionContext.class);
		ConversationContext conversationContext = Beans.getReference(ConversationContext.class);
		
		if (requestContext.isActive()){
			logger.debug(bundle.getString("management-debug-stoping-custom-context",
					requestContext.getClass().getCanonicalName(), managedType.getCanonicalName()));
			
			requestContext.deactivate();
		}
		
		if (!viewContext.isActive()){
			logger.debug(bundle.getString("management-debug-stoping-custom-context",
					viewContext.getClass().getCanonicalName(), managedType.getCanonicalName()));
			
			viewContext.deactivate();
		}

		if (!sessionContext.isActive()){
			logger.debug(bundle.getString("management-debug-stoping-custom-context",
					sessionContext.getClass().getCanonicalName(), managedType.getCanonicalName()));
			
			sessionContext.deactivate();
		}
		
		if (!conversationContext.isActive()){
			logger.debug(bundle.getString("management-debug-starting-custom-context",
					conversationContext.getClass().getCanonicalName(), managedType.getCanonicalName()));
			
			conversationContext.activate();
		}
	}

	public void shutdown(Collection<Class<? extends ManagementExtension>> monitoringExtensions) {
		for (Class<? extends ManagementExtension> monitoringExtensionClass : monitoringExtensions) {

			ManagementExtension monitoringExtension = Beans.getReference(monitoringExtensionClass);
			monitoringExtension.shutdown(this.getManagedTypes());
			logger.debug(bundle.getString("management-debug-removing-management-extension", monitoringExtension
					.getClass().getCanonicalName()));

		}
	}

	public void initialize(Collection<Class<? extends ManagementExtension>> monitoringExtensions) {
		for (Class<? extends ManagementExtension> monitoringExtensionClass : monitoringExtensions) {
			ManagementExtension monitoringExtension = Beans.getReference(monitoringExtensionClass);

			logger.debug(bundle.getString("management-debug-processing-management-extension", monitoringExtension
					.getClass().getCanonicalName()));

			monitoringExtension.initialize(this.getManagedTypes());
		}
	}

	private Validator getDefaultValidator() {
		if (validator == null) {
			try {
				this.validator = Validation.buildDefaultValidatorFactory().getValidator();
			} catch (ValidationException e) {
				this.validator = null;
			}
		}

		return this.validator;
	}
	

}
