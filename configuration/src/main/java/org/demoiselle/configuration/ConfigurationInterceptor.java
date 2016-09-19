package org.demoiselle.configuration;

import org.demoiselle.annotation.literal.NamedQualifier;
import org.demoiselle.internal.implementation.ConfigurationLoader;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Named;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * <p>
 * Interceptor class that loads the values of configuration files
 * into it's mapped class.
 * </p>
 */
@Dependent
@Configuration
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class ConfigurationInterceptor {
	@AroundInvoke
	public static Object constructConfiguration(final InvocationContext ic) throws Exception {
		final ConfigurationLoader configurationLoader = CDI.current().select(ConfigurationLoader.class, new NamedQualifier("demoiselle-configuration-loader")).get();

		final Class<?> baseClass = ic.getMethod().getDeclaringClass();
		configurationLoader.load(ic.getTarget(), baseClass);
		return ic.proceed();
	}
}
