package org.demoiselle.jee.core.lifecycle;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.demoiselle.jee.core.lifecycle.annotation.Shutdown;
import org.demoiselle.jee.core.lifecycle.annotation.Startup;

/**
 * This class is responsible for managing the execution of methods annotated with @Startup and @Shutdown
 * 
 * @author SERPRO
 */
public class LifecycleBootstrap implements Extension {
	
	private Logger logger;
	private LifecycleMessage message;
	
	private List<AnnotatedMethodProcessor> methodsWithStartup = Collections.synchronizedList(new ArrayList<>());
	private List<AnnotatedMethodProcessor> methodsWithShutdown = Collections.synchronizedList(new ArrayList<>());
	
	protected void startup(@Observes ProcessAnnotatedType<?> event){
		
		final AnnotatedType<?> annotatedType = event.getAnnotatedType();
		
		for (AnnotatedMethod<?> am : annotatedType.getMethods()){
			if(am.isAnnotationPresent(Startup.class)) {
				methodsWithStartup.add(new AnnotatedMethodProcessor(am));
			}
			
			if(am.isAnnotationPresent(Shutdown.class)){
				methodsWithShutdown.add(new AnnotatedMethodProcessor(am));
			}
		}	
	}
		
	protected void processStartup(@Observes @Initialized(ApplicationScoped.class) Object o){
		execute(this.methodsWithStartup);
	}
	
	protected void processShutdown(@Observes @Destroyed(ApplicationScoped.class) Object o){
		execute(this.methodsWithShutdown);
	}
	
	private <T> void execute(List<AnnotatedMethodProcessor> methods) {
		Collections.sort(methods, new Comparator<AnnotatedMethodProcessor>() {

			@Override
			public int compare(AnnotatedMethodProcessor o1, AnnotatedMethodProcessor o2) {
				Integer orderThis = o1.getPriority(o1.getAnnotatedMethod());
				Integer orderOther = o2.getPriority(o2.getAnnotatedMethod());

				return orderThis.compareTo(orderOther);
			}
		});
		
		methods.forEach((amp) -> {
			String cn = amp.getAnnotatedMethod().getDeclaringType().getJavaClass().getCanonicalName();
			
			ClassLoader classLoader = getClassLoaderForResource(cn.replaceAll("\\.", "/") + ".class");
			
			if(Thread.currentThread().getContextClassLoader().equals(classLoader)){
				try {
					
					getLogger().info(getMessage().executingMethod(amp.getAnnotatedMethod().toString()));
					amp.getAnnotatedMethod().getJavaMember().invoke(CDI.current().select(amp.getAnnotatedMethod().getJavaMember().getDeclaringClass()).get(), new Object[]{});
					
				} 
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					getLogger().severe(e.getMessage());
					throw new LifecycleException(e);
				}
			}
		});
	}
	
	private ClassLoader getClassLoaderForResource(final String resource) {
		final String stripped = resource.charAt(0) == '/' ? resource.substring(1) : resource;

		URL url = null;
		ClassLoader result = Thread.currentThread().getContextClassLoader();

		if (result != null) {
			url = result.getResource(stripped);
		}

		if (url == null) {
			result = getClass().getClassLoader();
			url = getClass().getClassLoader().getResource(stripped);
		}

		if (url == null) {
			result = null;
		}

		return result;
	}

	private Logger getLogger(){
		if(this.logger == null){
			this.logger = CDI.current().select(Logger.class).get();
		}
		
		return this.logger;
	}
	
	private LifecycleMessage getMessage(){
		if(this.message == null){
			this.message = CDI.current().select(LifecycleMessage.class).get();
		}
		
		return this.message;
	}

}
