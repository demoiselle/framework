package br.gov.frameworkdemoiselle.internal.bootstrap;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import br.gov.frameworkdemoiselle.context.CustomContext;
import br.gov.frameworkdemoiselle.context.StaticContext;
import br.gov.frameworkdemoiselle.internal.context.TemporaryRequestContextImpl;
import br.gov.frameworkdemoiselle.internal.context.TemporarySessionContextImpl;
import br.gov.frameworkdemoiselle.internal.context.StaticContextImpl;
import br.gov.frameworkdemoiselle.internal.context.TemporaryConversationContextImpl;
import br.gov.frameworkdemoiselle.internal.context.TemporaryViewContextImpl;

/**
 * This portable extension registers and starts custom contexts used by
 * the framework.
 * 
 * @author serpro
 *
 */
public class CustomContextBootstrap implements Extension{
	
	private List<CustomContext> contexts;
	
	public <T extends CustomContext> void vetoCustomContexts(@Observes ProcessAnnotatedType<T> event){
		//Veta os subtipos de CustomContext, para que não conflitem com o produtor de contextos personalizados. 
		if( CustomContext.class.isAssignableFrom( event.getAnnotatedType().getJavaClass() )){
			event.veto();
		}
	}
	
	public void initializeContexts(@Observes AfterBeanDiscovery event){
		//Cadastra os contextos contidos no demoiselle-core
		if (contexts==null || contexts.isEmpty()){
			CustomContext ctx;
			
			contexts = new ArrayList<CustomContext>();
			
			ctx = new TemporaryRequestContextImpl();
			contexts.add(ctx);
			
			ctx = new TemporarySessionContextImpl();
			contexts.add(ctx);
			
			ctx = new StaticContextImpl();
			contexts.add(ctx);
			
			ctx = new TemporaryViewContextImpl();
			contexts.add(ctx);
			
			ctx = new TemporaryConversationContextImpl();
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
	
	public List<CustomContext> getCustomContexts(){
		return this.contexts;
	}
	
	/*public void storeContexts(@Observes AfterDeploymentValidation event){
		CustomContextProducer producer = Beans.getReference(CustomContextProducer.class);
		producer.addRegisteredContexts(contexts);
	}*/
	
}
