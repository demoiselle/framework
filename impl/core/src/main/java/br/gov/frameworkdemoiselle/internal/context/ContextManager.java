package br.gov.frameworkdemoiselle.internal.context;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.StaticScoped;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * <p>
 * Manage custom contexts relevant to Demoiselle operations.
 * </p>
 * <p>
 * When starting, the ContextManager must be initialized by calling {@link #initialize(AfterBeanDiscovery event)} inside
 * any methods observing the {@link AfterBeanDiscovery} event. Upon initialization a {@link StaticContext} will be
 * created to handle {@link StaticScoped} beans (but not activated, you must call
 * {@link #activate(Class customContextClass, Class scope)} to activate this context).
 * </p>
 * <p>
 * If an extension wants to manage another custom context, it must first call
 * {@link #add(CustomContext context, AfterBeanDiscovery event)} to add it's context to the list of managed contexts and
 * then call {@link #activate(Class customContextClass, Class scope)} whenever it wants to activate this added context
 * (contexts added through the {@link #add(CustomContext context, AfterBeanDiscovery event)} method are also not
 * activated upon adding).
 * </p>
 * 
 * @author serpro
 */
public class ContextManager {

	private static List<CustomContextCounter> contexts = Collections
			.synchronizedList(new ArrayList<CustomContextCounter>());

	private static boolean initialized = false;

	private static ResourceBundle bundle;

	private static Logger logger;
	
	private ContextManager(){}

	/**
	 * <p>
	 * Initializes this manager and adds the {@link StaticContext} context to the list of managed contexts. Other
	 * contexts must be added before they can be activated.
	 * </p>
	 * <p>
	 * It's OK to call this method multiple times, it will be initialized only once.
	 * </p>
	 * 
	 * @param event
	 *            The CDI event indicating all beans have been discovered.
	 */
	public static void initialize(AfterBeanDiscovery event) {
		if (initialized) {
			return;
		}

		add(new StaticContext(), event);
		initialized = true;
	}

	/**
	 * <p>
	 * Adds a context to the list of managed contexts.
	 * </p>
	 * <p>
	 * A context added through this method will be deactivated before management can start. Only after calling
	 * {@link #activate(Class customContextClass, Class scope)} the context will be activated.
	 * </p>
	 * <p>
	 * Trying to add a context already managed will result in this method call being ignored.
	 * </p>
	 * 
	 * @param context
	 *            The context to be added
	 * @param event
	 *            The CDI event indicating all beans have been discovered.
	 */
	public static void add(CustomContext context, AfterBeanDiscovery event) {
		for (CustomContextCounter contextCounter : contexts) {
			if (contextCounter.isSame(context.getClass(), context.getScope())) {

				ContextManager.getLogger().trace(
						ContextManager.getBundle().getString("bootstrap-context-already-managed",
								context.getClass().getCanonicalName(), context.getScope().getCanonicalName()));

				return;
			}
		}

		context.setActive(false);
		event.addContext(context);
		contexts.add(new CustomContextCounter(context));
	}

	/**
	 * <p>
	 * Activates a managed context.
	 * </p>
	 * <p>
	 * To be activated, a context must fulfill the following requisites:
	 * <ul>
	 * <li>Must be managed by this class (be of type {@link StaticScoped} or be added with
	 * {@link #add(CustomContext context, AfterBeanDiscovery event)})</li>
	 * <li>Must be of a scope not already attached to another active context</li>
	 * </ul>
	 * </p>
	 * 
	 * @param customContextClass
	 *            Type of context to activate
	 * @param scope
	 *            The scope to activate this context for
	 * @return <code>true</code> if there is a managed context of the provided type and scope and no other context is
	 *         active for the provided scope, <code>false</code> if there is a managed context of the provided type and
	 *         scope but another context is active for the provided scope.
	 * @throws DemoiselleException
	 *             if there is no managed context of the provided type and scope.
	 */
	public static synchronized void activate(Class<? extends CustomContext> customContextClass,
			Class<? extends Annotation> scope) {
		if (!initialized) {
			throw new DemoiselleException(getBundle().getString("custom-context-manager-not-initialized"));
		}

		for (CustomContextCounter context : contexts) {
			if (context.isSame(customContextClass, scope)) {
				context.activate();
				return;
			}
		}

		throw new DemoiselleException(getBundle().getString("custom-context-not-found",
				customContextClass.getCanonicalName(), scope.getSimpleName()));
	}

	/**
	 * <p>
	 * Deactivates a managed context.
	 * </p>
	 * <p>
	 * To be deactivated, a context must fulfill the following requisites:
	 * <ul>
	 * <li>Must be managed by this class (be of type {@link StaticScoped} or be added with
	 * {@link #add(CustomContext context, AfterBeanDiscovery event)})</li>
	 * <li>Must have been activated by a previous call to {@link #activate(Class customContextClass, Class scope)}</li>
	 * <li>This previous call must have returned <code>true</code>.
	 * </ul>
	 * </p>
	 * 
	 * @param customContextClass
	 *            Type of context to deactivate
	 * @param scope
	 *            The scope the context controled when it was active
	 * @return <code>true</code> if there was an active context of this type and scope and it was activated by a
	 *         previous call to {@link #activate(Class customContextClass, Class scope)}
	 * @throws DemoiselleException
	 *             if there is no managed context of the provided type and scope.
	 */
	public static synchronized void deactivate(Class<? extends CustomContext> customContextClass,
			Class<? extends Annotation> scope) {
		if (!initialized) {
			throw new DemoiselleException(getBundle().getString("custom-context-manager-not-initialized"));
		}

		for (CustomContextCounter context : contexts) {
			if (context.isSame(customContextClass, scope)) {
				context.deactivate();
				return;
			}
		}

		throw new DemoiselleException(getBundle().getString("custom-context-not-found",
				customContextClass.getCanonicalName(), scope.getSimpleName()));
	}

	public static synchronized void shutdown() {
		for (CustomContextCounter context : contexts) {
			context.deactivate();
		}

		contexts.clear();
		initialized = false;
	}

	static Logger getLogger() {
		if (logger == null) {
			logger = LoggerProducer.create(ContextManager.class);
		}

		return logger;
	}

	static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = ResourceBundleProducer.create("demoiselle-core-bundle", Locale.getDefault());
		}

		return bundle;
	}
}

/**
 * Class that counts how many attemps to activate and deactivate this context received, avoiding cases where one client
 * activates given context and another one deactivates it, leaving the first client with no active context before
 * completion.
 * 
 * @author serpro
 */
class CustomContextCounter {

	private CustomContext context;

	private int activationCounter = 0;

	public CustomContextCounter(CustomContext customContext) {
		this.context = customContext;
	}

	public boolean isSame(Class<? extends CustomContext> customContextClass, Class<? extends Annotation> scope) {
		if (context.getClass().getCanonicalName().equals(customContextClass.getCanonicalName())
				&& context.getScope().equals(scope)) {
			return true;
		}

		return false;
	}

	public CustomContext getInternalContext() {
		return this.context;
	}

	public synchronized void activate() {
		try {
			BeanManager beanManager = Beans.getReference(BeanManager.class);
			Context c = beanManager.getContext(context.getScope());
			if (c == context) {
				activationCounter++;
			}
		} catch (ContextNotActiveException ce) {
			context.setActive(true);
			activationCounter++;
			ContextManager.getLogger().trace(
					ContextManager.getBundle().getString("custom-context-was-activated",
							context.getClass().getCanonicalName(), context.getScope().getCanonicalName()));
		}
	}

	public synchronized void deactivate() {
		try {
			Context c = Beans.getBeanManager().getContext(context.getScope());
			if (c == context) {
				activationCounter--;
				if (activationCounter == 0) {
					context.setActive(false);
					ContextManager.getLogger().trace(
							ContextManager.getBundle().getString("custom-context-was-deactivated",
									context.getClass().getCanonicalName(), context.getScope().getCanonicalName()));
				}
			}
		} catch (ContextNotActiveException ce) {
		}
	}
}
