package br.gov.frameworkdemoiselle.internal.context;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.context.CustomContext;
import br.gov.frameworkdemoiselle.internal.implementation.StrategySelector;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@ApplicationScoped
public class ContextManager2 {
	
	private List<CustomContext> contexts;
	
	@PostConstruct
	private void initialize(){
		if (contexts==null || contexts.isEmpty()){
			Logger logger = getLogger();
			ResourceBundle bundle = getBundle();
			
			CustomContext ctx;
			
			contexts = new ArrayList<CustomContext>();
			
			ctx = new RequestContextImpl();
			contexts.add(ctx);
			logger.debug( bundle.getString("bootstrap-context-added", RequestContextImpl.class.getCanonicalName() , ctx.getScope().getSimpleName() ) );
			
			ctx = new SessionContextImpl();
			contexts.add(ctx);
			logger.debug( bundle.getString("bootstrap-context-added", SessionContextImpl.class.getCanonicalName() , ctx.getScope().getSimpleName() ) );
			
			ctx = new StaticContextImpl();
			contexts.add(ctx);
			logger.debug( bundle.getString("bootstrap-context-added", StaticContextImpl.class.getCanonicalName() , ctx.getScope().getSimpleName() ) );
			
			ctx = new ThreadLocalViewContextImpl();
			contexts.add(ctx);
			logger.debug( bundle.getString("bootstrap-context-added", ThreadLocalViewContextImpl.class.getCanonicalName() , ctx.getScope().getSimpleName() ) );
		}
	}
	
	@PreDestroy
	private void closeContexts(){
		for (CustomContext context : contexts){
			context.deactivate();
		}
		
		contexts.clear();
	}
	
	public void addCustomContext(CustomContext context){
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
	
	@Produces
	public CustomContext getContext(InjectionPoint ip){
		CustomContext producedContext = null;
		
		if (ip!=null){
			Class<?> beanClass = ip.getBean().getBeanClass();
			ArrayList<CustomContext> selectableContexts = new ArrayList<CustomContext>();
			
			for (CustomContext context : contexts){
				if ( beanClass.isAssignableFrom( context.getClass() ) ){
					if (context.isActive()){
						producedContext = context;
						break;
					}
					else{
						selectableContexts.add(context);
					}
				}
			}
			
			producedContext = StrategySelector.selectInstance(CustomContext.class, selectableContexts);
		}
		
		return producedContext;
	}
	
	private ResourceBundle getBundle(){
		return Beans.getReference(ResourceBundle.class , new NameQualifier("demoiselle-core-bundle"));
	}
	
	private Logger getLogger(){
		return Beans.getReference(Logger.class);
	}

}
