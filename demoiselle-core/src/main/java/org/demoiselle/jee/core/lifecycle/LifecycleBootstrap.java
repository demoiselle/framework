/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.lifecycle;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

import org.demoiselle.jee.core.exception.DemoiselleLifecycleException;
import org.demoiselle.jee.core.lifecycle.annotation.Shutdown;
import org.demoiselle.jee.core.lifecycle.annotation.Startup;
import org.demoiselle.jee.core.message.DemoiselleMessage;
import org.demoiselle.jee.core.message.MessageBundleLiteral;

/**
 * This class is responsible for managing the execution of methods annotated
 * with @Startup and @Shutdown
 *
 * @author SERPRO
 */ 
public class LifecycleBootstrap implements Extension {

	private static final Logger logger = Logger.getLogger(LifecycleBootstrap.class.getName());
	private DemoiselleMessage message;
	
	private List<AnnotatedMethodProcessor> methodsWithStartup = Collections.synchronizedList(new ArrayList<>());
	private List<AnnotatedMethodProcessor> methodsWithShutdown = Collections.synchronizedList(new ArrayList<>());

	protected void startup(@Observes ProcessAnnotatedType<?> event) {

		final AnnotatedType<?> annotatedType = event.getAnnotatedType();

		for (AnnotatedMethod<?> am : annotatedType.getMethods()) {
			if (am.isAnnotationPresent(Startup.class)) {
				methodsWithStartup.add(new AnnotatedMethodProcessor(am));
			}

			if (am.isAnnotationPresent(Shutdown.class)) {
				methodsWithShutdown.add(new AnnotatedMethodProcessor(am));
			}
		}
	}

	protected void processStartup(@Observes @Initialized(ApplicationScoped.class) Object o) {
	    logger.info("====================================================");
        logger.info(getMessage().startMessage());
        logger.info(getMessage().frameworkName() + " " + getMessage().version());
        logger.info(getMessage().engineOn());
        logger.info("====================================================");
        
		execute(this.methodsWithStartup);
	}

	protected void processShutdown(@Observes @Destroyed(ApplicationScoped.class) Object o) {
	    
	    logger.info("====================================================");
        logger.info(getMessage().frameworkName() + " " + getMessage().version());
        logger.info(getMessage().engineOff());
        logger.info("====================================================");
        
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

		methods.stream().forEach((amp) -> {
			String cn = amp.getAnnotatedMethod().getDeclaringType().getJavaClass().getCanonicalName();

			ClassLoader classLoader = getClassLoaderForResource(cn.replaceAll("\\.", "/") + ".class");

			if (Thread.currentThread().getContextClassLoader().equals(classLoader)) {
				try {

					logger.info(getMessage().executingMethod(amp.getAnnotatedMethod().toString()));
					amp.getAnnotatedMethod().getJavaMember().invoke(
							CDI.current().select(amp.getAnnotatedMethod().getJavaMember().getDeclaringClass()).get(),
							new Object[] {});

				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					logger.severe(e.getMessage());
					throw new DemoiselleLifecycleException(e);
				} catch (IllegalStateException e) {
					// CDI container may not be available during shutdown in SE environments
					logger.warning("CDI container not available, skipping lifecycle method: " + amp.getAnnotatedMethod().toString());
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

	private DemoiselleMessage getMessage() {
		if (this.message == null) {
			this.message = CDI.current().select(DemoiselleMessage.class, new MessageBundleLiteral()).get();
		}

		return this.message;
	}

}
