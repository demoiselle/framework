package br.gov.frameworkdemoiselle.management.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.management.ReflectionException;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.context.ContextManager;
import br.gov.frameworkdemoiselle.internal.context.ManagedContext;
import br.gov.frameworkdemoiselle.management.annotation.Managed;
import br.gov.frameworkdemoiselle.management.annotation.Property;
import br.gov.frameworkdemoiselle.management.extension.ManagementExtension;
import br.gov.frameworkdemoiselle.management.internal.ManagedType.MethodDetail;
import br.gov.frameworkdemoiselle.management.notification.AttributeChangeNotification;
import br.gov.frameworkdemoiselle.management.notification.NotificationManager;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * A manager that helps implementators of the management framework to obtain a
 * list of managed classes, set or obtain values from them or invoke operations
 * over them.
 * 
 * @author serpro
 */
@ApplicationScoped
public class MonitoringManager {

	@Inject
	private Logger logger;

	@Inject
	private ResourceBundle bundle;

	private final List<ManagedType> managedTypes = new ArrayList<ManagedType>();

	public void addManagedType(ManagedType managedType) {
		managedTypes.add(managedType);
	}

	/**
	 * @return A list all managed types, classes annotated with {@link Managed}.
	 *         The returned list is a shallow copy of the internal list, so you
	 *         are free to modify it.
	 * 
	 *         TODO precisamos desse clone na lista?
	 */
	public List<ManagedType> getManagedTypes() {
		ArrayList<ManagedType> cloneList = new ArrayList<ManagedType>();
		cloneList.addAll(managedTypes);
		return cloneList;
	}

	/**
	 * Invoke an operation over a managed type.
	 * 
	 * @param managedType
	 *            A type annotated with {@link Managed}. This method will create
	 *            an (or obtain an already created) instance of this type and
	 *            invoke the operation over it.
	 * @param actionName
	 *            Name of method to be invoked, the type must have this
	 *            operation on it's list
	 * @param params
	 *            List of values for the operation parameters. Can be
	 *            <code>null</code> if the operation require no parameters.
	 * @return The return value of the original invoked operation. Methods of
	 *         return type <code>void</code> will return the {@link Void} type.
	 * @throws ReflectionException
	 *             In case the operation doesn't exist or have a different
	 *             signature
	 */
	public synchronized Object invoke(ManagedType managedType, String actionName,
			Object[] params) {
		if ( managedTypes.contains(managedType) ) {
			activateContexts(managedType.getType());

			Object delegate = Beans.getReference(managedType.getType());

			MethodDetail method = managedType.getOperationMethods().get(actionName);

			if (method != null) {
				try {
					logger.debug(bundle
							.getString("management-debug-invoking-operation",actionName,managedType.getType().getCanonicalName()));
					return method.getMethod().invoke(delegate, params);
				} catch (Exception e) {
					throw new DemoiselleException(bundle.getString(
							"management-invoke-error", actionName), e);
				} finally {
					deactivateContexts(managedType.getType());
				}
			} else {
				throw new DemoiselleException(bundle.getString(
						"management-invoke-error", actionName));
			}
		} else {
			throw new DemoiselleException(
					bundle.getString("management-type-not-found"));
		}
	}

	/**
	 * Retrieve the current value of a property from a managed type. Properties
	 * are attributes annotated with {@link Property}.
	 * 
	 * @param managedType The type that has the property the client wants to know the value of.
	 * @param propertyName The name of the property
	 * @return The current value of the property
	 */
	public synchronized Object getProperty(ManagedType managedType, String propertyName) {
		
		if ( managedTypes.contains(managedType) ) {
			Method getterMethod = managedType.getFields().get(propertyName).getGetterMethod();

			if (getterMethod != null) {
				logger.debug(bundle.getString(
						"management-debug-acessing-property", getterMethod
								.getName(), managedType.getType().getCanonicalName()));

				activateContexts(managedType.getType());

				try {
					Object delegate = Beans.getReference(managedType.getType());

					return getterMethod.invoke(delegate, (Object[]) null);
				} catch (Exception e) {
					throw new DemoiselleException(bundle.getString(
							"management-invoke-error", getterMethod.getName()),
							e);
				} finally {
					deactivateContexts(managedType.getType());
				}
			} else {
				throw new DemoiselleException(bundle.getString(
						"management-invoke-error", propertyName));
			}
		} else {
			throw new DemoiselleException(
					bundle.getString("management-type-not-found"));
		}
	}
	
	/**
	 * Sets a new value for a property contained inside a managed type. A property
	 * is an attribute annotated with {@link Property}.
	 * 
	 * @param managedType The type that has access to the property
	 * @param propertyName The name of the property
	 * @param newValue The new value of the property
	 */
	public synchronized void setProperty(ManagedType managedType, String propertyName,
			Object newValue) {

		if ( managedTypes.contains(managedType) ) {
			// Procura o método set do atributo em questão
			Method method = managedType.getFields().get(propertyName).getSetterMethod();
			if (method != null) {
				logger.debug(bundle.getString(
						"management-debug-setting-property", method.getName(),
						managedType.getType().getCanonicalName()));

				// Obtém uma instância da classe gerenciada, lembrando que
				// classes
				// anotadas com @Managed são sempre singletons.
				activateContexts(managedType.getType());
				try {
					Object delegate = Beans.getReference(managedType.getType());

					Method getterMethod = managedType.getFields().get(propertyName).getGetterMethod();
					Object oldValue;
					try{
						oldValue = getterMethod.invoke(delegate, (Object[])null);
					}
					catch(Exception e){
						oldValue = null;
					}

					method.invoke(delegate, new Object[] { newValue });

					//Manda uma notificação de mudança de atributo 
					NotificationManager notificationManager = Beans.getReference(NotificationManager.class);
					Class<? extends Object> attributeType = newValue!=null ? newValue.getClass() : null;
					AttributeChangeNotification notification = new AttributeChangeNotification(bundle.getString(""), propertyName, attributeType, oldValue, newValue);
					notificationManager.sendAttributeChangedMessage(notification);

				} catch (Exception e) {
					throw new DemoiselleException(bundle.getString(
							"management-invoke-error", method.getName()), e);
				} finally {
					deactivateContexts(managedType.getType());
				}

			} else {
				throw new DemoiselleException(bundle.getString(
						"management-invoke-error", propertyName));
			}
		} else {
			throw new DemoiselleException(
					bundle.getString("management-type-not-found"));
		}

	}

	private void activateContexts(Class<?> managedType) {
		logger.debug(bundle.getString("management-debug-starting-custom-context",
				ManagedContext.class.getCanonicalName(),
				managedType.getCanonicalName()));
		
		ContextManager.activate(ManagedContext.class,RequestScoped.class);
	}

	private void deactivateContexts(Class<?> managedType) {
		logger.debug(bundle.getString("management-debug-stoping-custom-context",
				ManagedContext.class.getCanonicalName(),
				managedType.getCanonicalName()));
		
		ContextManager.deactivate(ManagedContext.class,RequestScoped.class);
	}

	public void shutdown(Collection<Class<? extends ManagementExtension>> monitoringExtensions) {

		for (Class<? extends ManagementExtension> monitoringExtensionClass : monitoringExtensions) {

			ManagementExtension monitoringExtension = Beans.getReference(monitoringExtensionClass);

			monitoringExtension.shutdown(this.getManagedTypes());

		}

	}

	public void initialize(Collection<Class<? extends ManagementExtension>> monitoringExtensions) {

		for (Class<? extends ManagementExtension> monitoringExtensionClass : monitoringExtensions) {

			ManagementExtension monitoringExtension = Beans
					.getReference(monitoringExtensionClass);

			monitoringExtension.initialize(this.getManagedTypes());

		}

	}

}
