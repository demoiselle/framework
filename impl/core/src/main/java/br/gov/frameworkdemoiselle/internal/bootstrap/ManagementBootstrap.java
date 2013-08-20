package br.gov.frameworkdemoiselle.internal.bootstrap;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.context.ContextManager;
import br.gov.frameworkdemoiselle.internal.context.ManagedContext;
import br.gov.frameworkdemoiselle.internal.management.ManagedType;
import br.gov.frameworkdemoiselle.internal.management.Management;
import br.gov.frameworkdemoiselle.lifecycle.AfterShutdownProccess;
import br.gov.frameworkdemoiselle.lifecycle.ManagementExtension;
import br.gov.frameworkdemoiselle.stereotype.ManagementController;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public class ManagementBootstrap implements Extension {

	protected static List<AnnotatedType<?>> types = Collections.synchronizedList(new ArrayList<AnnotatedType<?>>());

	private List<Class<? extends ManagementExtension>> managementExtensionCache = Collections
			.synchronizedList(new ArrayList<Class<? extends ManagementExtension>>());

	public <T> void detectAnnotation(@Observes final ProcessAnnotatedType<T> event, final BeanManager beanManager) {
		if (event.getAnnotatedType().isAnnotationPresent(ManagementController.class)) {
			types.add(event.getAnnotatedType());
		}
	}

	public void activateContexts(@Observes final AfterBeanDiscovery event) {
		ContextManager.initialize(event);
		ContextManager.add(new ManagedContext(), event);
	}

	@SuppressWarnings("unchecked")
	public void registerAvailableManagedTypes(@Observes final AfterDeploymentValidation event, BeanManager beanManager) {
		ResourceBundle bundle = new ResourceBundle("demoiselle-core-bundle", Locale.getDefault());

		Management monitoringManager = Beans.getReference(Management.class);
		for (AnnotatedType<?> type : types) {
			if (type.getJavaClass().isInterface() || Modifier.isAbstract(type.getJavaClass().getModifiers())) {
				throw new DemoiselleException(bundle.getString("management-abstract-class-defined", type.getJavaClass()
						.getCanonicalName()));
			}

			ManagedType managedType = new ManagedType(type.getJavaClass());
			monitoringManager.addManagedType(managedType);
		}

		Set<Bean<?>> extensionBeans = beanManager.getBeans(ManagementExtension.class);
		if (extensionBeans != null) {
			for (Bean<?> bean : extensionBeans) {
				Class<?> extensionConcreteClass = bean.getBeanClass();
				managementExtensionCache.add((Class<? extends ManagementExtension>) extensionConcreteClass);
			}
		}

		monitoringManager.initialize(managementExtensionCache);
	}

	public void unregisterAvailableManagedTypes(@Observes final AfterShutdownProccess event) {

		Management manager = Beans.getReference(Management.class);
		manager.shutdown(managementExtensionCache);

		managementExtensionCache.clear();
		types.clear();
	}

}
