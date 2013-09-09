package br.gov.frameworkdemoiselle.internal.bootstrap;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Extension;

import br.gov.frameworkdemoiselle.context.CustomContext;
import br.gov.frameworkdemoiselle.context.StaticContext;
import br.gov.frameworkdemoiselle.internal.context.CustomContextProducer;
import br.gov.frameworkdemoiselle.internal.context.RequestContextImpl;
import br.gov.frameworkdemoiselle.internal.context.SessionContextImpl;
import br.gov.frameworkdemoiselle.internal.context.StaticContextImpl;
import br.gov.frameworkdemoiselle.internal.context.ThreadLocalViewContextImpl;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * This portable extension registers and starts custom contexts used by
 * the framework.
 * 
 * @author serpro
 *
 */
public class CustomContextBootstrap implements Extension{
	
	private List<CustomContext> contexts;
	
	/*private Logger logger;

	private transient ResourceBundle bundle;
	
	private Logger getLogger() {
		if (this.logger == null) {
			this.logger = LoggerProducer.create(CoreBootstrap.class);
		}

		return this.logger;
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = new ResourceBundle("demoiselle-core-bundle", Locale.getDefault());
		}

		return bundle;
	}*/
	
	public void initializeContexts(@Observes AfterBeanDiscovery event){
		//Cadastra os contextos contidos no demoiselle-core
		if (contexts==null || contexts.isEmpty()){
			CustomContext ctx;
			
			contexts = new ArrayList<CustomContext>();
			
			ctx = new RequestContextImpl();
			contexts.add(ctx);
			
			ctx = new SessionContextImpl();
			contexts.add(ctx);
			
			ctx = new StaticContextImpl();
			contexts.add(ctx);
			
			ctx = new ThreadLocalViewContextImpl();
			contexts.add(ctx);
			
			for (CustomContext c : contexts){
				event.addContext(c);
			}
		}
		
		//Ativa um contexto para gerenciar o StaticScope, um escopo criado para gerenciar classes de configuração.
		for (CustomContext ctx : contexts){
			if (ctx instanceof StaticContext){
				StaticContext staticContext = (StaticContext)ctx;
				staticContext.activate();
				break;
			}
		}
	}
	
	public void storeContexts(@Observes AfterDeploymentValidation event){
		CustomContextProducer producer = Beans.getReference(CustomContextProducer.class);
		producer.addRegisteredContexts(contexts);
	}
	
}
