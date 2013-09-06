package br.gov.frameworkdemoiselle.internal.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.context.CustomContext;
import br.gov.frameworkdemoiselle.context.RequestContext;
import br.gov.frameworkdemoiselle.context.SessionContext;
import br.gov.frameworkdemoiselle.context.StaticContext;
import br.gov.frameworkdemoiselle.context.ViewContext;
import br.gov.frameworkdemoiselle.internal.bootstrap.CustomContextBootstrap;
import br.gov.frameworkdemoiselle.internal.implementation.StrategySelector;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Produces instances of {@link CustomContext} to control contexts not active
 * by the container
 * 
 * @author serpro
 *
 */
@ApplicationScoped
public class CustomContextProducer {
	
	private Logger logger;

	private transient ResourceBundle bundle;
	
	private List<CustomContext> contexts = new ArrayList<CustomContext>();
	
	/**
	 * Store a list of contexts into this producer. The contexts must have
	 * been registered into CDI by a portable extension, this method will not do this.
	 *  
	 */
	public void addRegisteredContexts(Collection<CustomContext> contexts){
		for (CustomContext context : contexts){
			addRegisteredContext(context);
		}
	}
	
	/**
	 * Store a context into this producer. The context must have
	 * been registered into CDI by a portable extension, this method will not do this.
	 *  
	 */
	public void addRegisteredContext(CustomContext context){
		Logger logger = getLogger();
		ResourceBundle bundle = getBundle();
		
		if (!contexts.contains(context)){
			contexts.add(context);
			logger.debug( bundle.getString("bootstrap-context-added", context.getClass().getCanonicalName() , context.getScope().getSimpleName() ) );
		}
		else{
			logger.debug( bundle.getString("bootstrap-context-already-managed", context.getClass().getCanonicalName() , context.getScope().getSimpleName() ) );
		}
	}
	
	/**
	 * Deactivates all registered contexts and clear the context collection
	 */
	@PreDestroy
	public void closeContexts(){
		//Desativa todos os contextos registrados.
		for (CustomContext context : contexts){
			context.deactivate();
		}
		
		contexts.clear();
	}
	
	@Produces
	public RequestContext getRequestContext(InjectionPoint ip , CustomContextBootstrap extension){
		return getContext(ip, extension);
	}
	
	@Produces
	public SessionContext getSessionContext(InjectionPoint ip , CustomContextBootstrap extension){
		return getContext(ip, extension);
	}
	
	@Produces
	public ViewContext getViewContext(InjectionPoint ip , CustomContextBootstrap extension){
		return getContext(ip, extension);
	}
	
	@Produces
	public StaticContext getStaticContext(InjectionPoint ip , CustomContextBootstrap extension){
		return getContext(ip, extension);
	}
	
	
	@SuppressWarnings("unchecked")
	private <T extends CustomContext> T getContext(InjectionPoint ip , CustomContextBootstrap extension){
		T producedContext = null;
		
		if (ip!=null){
			Class<T> beanClass = (Class<T>) ip.getType();
			producedContext = (T) getContext(beanClass);
		}
		
		if (producedContext!=null){
			getLogger().debug( getBundle().getString("custom-context-selected" , producedContext.getClass().getCanonicalName()) );
		}
		
		return producedContext;
	}
	
	private CustomContext getContext(Class<? extends CustomContext> contextClass){
		CustomContext producedContext = null;
		
		ArrayList<CustomContext> selectableContexts = new ArrayList<CustomContext>();
		
		for (CustomContext context : contexts){
			if ( contextClass.isAssignableFrom( context.getClass() ) ){
				if (context.isActive()){
					producedContext = context;
					break;
				}
				else{
					selectableContexts.add(context);
				}
			}
		}
		
		if (producedContext==null && !selectableContexts.isEmpty()){
			producedContext = StrategySelector.selectInstance(CustomContext.class, selectableContexts);
		}
		
		return producedContext;
	}
	
	private Logger getLogger() {
		if (this.logger == null) {
			this.logger = LoggerProducer.create(this.getClass());
		}

		return this.logger;
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = new ResourceBundle("demoiselle-core-bundle", Locale.getDefault());
		}

		return bundle;
	}

}
