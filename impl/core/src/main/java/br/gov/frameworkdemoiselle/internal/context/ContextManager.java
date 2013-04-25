package br.gov.frameworkdemoiselle.internal.context;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.StaticScoped;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * <p>Manage custom contexts relevant to Demoiselle operations.</p>
 * 
 * <p>When starting, the ContextManager must be initialized by calling {@link #initialize(AfterBeanDiscovery event)}
 * inside any methods observing the {@link AfterBeanDiscovery} event. Upon initialization a {@link StaticContext} will be
 * created to handle {@link StaticScoped} beans (but not activated, you must call {@link #activate(Class customContextClass, Class scope)}
 * to activate this context).</p>
 * 
 * <p>If an extension wants to manage another custom context, it must first call {@link #add(CustomContext context, AfterBeanDiscovery event)}
 * to add it's context to the list of managed contexts and then call {@link #activate(Class customContextClass, Class scope)} whenever
 * it wants to activate this added context (contexts added through the {@link #add(CustomContext context, AfterBeanDiscovery event)} method are also
 * not activated upon adding).</p>
 * 
 * @author serpro
 *
 */
public class ContextManager {
	
	private static List<CustomContext> contexts = Collections.synchronizedList(new ArrayList<CustomContext>());
	
	private static List<CustomContext> activatedCustomContexts = Collections.synchronizedList(new ArrayList<CustomContext>());
	
	private static boolean initialized = false;
	
	private static ResourceBundle bundle = ResourceBundleProducer.create("demoiselle-core-bundle");
	
	private static Logger logger = LoggerProducer.create(ContextManager.class);
	
	/**
	 * <p>Initializes this manager and adds the {@link StaticContext} context to the list of managed contexts. Other
	 * contexts must be added before they can be activated.</p>
	 * 
	 * <p>It's OK to call this method multiple times, it will be initialized only once.</p>
	 * 
	 * @param event The CDI event indicating all beans have been discovered.
	 */
	public static void initialize(AfterBeanDiscovery event){
		if (initialized){
			return;
		}
		
		add(new StaticContext(),event);
		initialized=true;
	}
	
	/**
	 * <p>Adds a context to the list of managed contexts.</p>
	 * 
	 * <p>A context added through this method will be deactivated before management can start. Only after calling
	 * {@link #activate(Class customContextClass, Class scope)} the context will be activated.</p>
	 * 
	 * <p>Trying to add a context already managed will result in this method call being ignored.</p>
	 * 
	 * @param context The context to be added
	 * @param event The CDI event indicating all beans have been discovered.
	 */
	public static void add(CustomContext context,AfterBeanDiscovery event){
		context.setActive(true);
		event.addContext(context);
	}
	
	/**
	 * <p>Activates a managed context.</p>
	 * 
	 * <p>To be activated, a context must fulfill the following requisites:
	 * <ul>
	 * 
	 * <li>Must be managed by this class (be of type {@link StaticScoped} or be added with {@link #add(CustomContext context, AfterBeanDiscovery event)})</li>
	 * <li>Must be of a scope not already attached to another active context</li>
	 * 
	 * </ul>
	 * </p>
	 * 
	 * @param customContextClass Type of context to activate
	 * @param scope The scope to activate this context for
	 * @return <code>true</code> if there is a managed context of the provided type and scope and no other context is active for the provided scope,
	 * <code>false</code> if there is a managed context of the provided type and scope but another context is active for the provided scope.
	 * 
	 * @throws DemoiselleException if there is no managed context of the provided type and scope. 
	 */
	public static boolean activate(Class<? extends CustomContext> customContextClass , Class<? extends Annotation> scope){
		if (!initialized){
			throw new DemoiselleException(getBundle().getString("custom-context-manager-not-initialized"));
		}
		
		for (CustomContext context : contexts){
			if (context.getClass().getCanonicalName().equals( customContextClass.getCanonicalName() )
					&& context.getScope().equals(scope)){
				if (!context.isActive()){
					return activate(context);
				}
			}
		}

		throw new DemoiselleException(getBundle().getString("custom-context-not-found",customContextClass.getCanonicalName(),scope.getSimpleName()));
	}
	
	/**
	 * <p>Deactivates a managed context.</p>
	 * 
	 * <p>To be deactivated, a context must fulfill the following requisites:
	 * <ul>
	 * 
	 * <li>Must be managed by this class (be of type {@link StaticScoped} or be added with {@link #add(CustomContext context, AfterBeanDiscovery event)})</li>
	 * <li>Must have been activated by a previous call to {@link #activate(Class customContextClass, Class scope)}</li>
	 * <li>This previous call must have returned <code>true</code>.
	 * 
	 * </ul>
	 * </p>
	 * 
	 * @param customContextClass Type of context to deactivate
	 * @param scope The scope the context controled when it was active
	 * @return <code>true</code> if there was an active context of this type and scope and it was activated by a previous
	 * call to {@link #activate(Class customContextClass, Class scope)}
	 * 
	 * @throws DemoiselleException if there is no managed context of the provided type and scope. 
	 */
	public static boolean deactivate(Class<? extends CustomContext> customContextClass,Class<? extends Annotation> scope){
		if (!initialized){
			throw new DemoiselleException(getBundle().getString("custom-context-manager-not-initialized"));
		}
		
		for (CustomContext context : activatedCustomContexts){
			if (context.getClass().getCanonicalName().equals( customContextClass.getCanonicalName() )
					&& context.getScope().equals(scope)){

				if (context.isActive()){
					return deactivate(context);
				}
			}
		}

		throw new DemoiselleException(getBundle().getString("custom-context-not-found",customContextClass.getCanonicalName(),scope.getSimpleName()));
	}
	
	private static boolean activate(CustomContext context){
		try{
			Beans.getBeanManager().getContext(context.getScope());
			return false;
		}
		catch(ContextNotActiveException ce){
			context.setActive(true);
			activatedCustomContexts.add(context);
			getLogger().trace(getBundle().getString("custom-context-was-activated", context.getClass().getCanonicalName(),context.getScope().getCanonicalName()));
			return true;
		}
	}
	
	private static boolean  deactivate(CustomContext context){
		try{
			Context activeContext = Beans.getBeanManager().getContext(context.getScope());
			if (activeContext.equals(context)){
				context.setActive(false);
				activatedCustomContexts.remove(context);
				return true;
			}
		}
		catch(ContextNotActiveException e){
		}
		
		return false;
	}
	
	private static Logger getLogger(){
		if (logger==null){
			logger = LoggerProducer.create(ContextManager.class);
		}
		
		return logger;
	}
	
	private static ResourceBundle getBundle(){
		if (bundle==null){
			bundle = ResourceBundleProducer.create("demoiselle-core-bundle");
		}
		
		return bundle;
	}

}
