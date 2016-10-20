package org.demoiselle.jee.configuration;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.CDI;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.demoiselle.jee.configuration.annotation.Configuration;

@Dependent
@Configuration
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class ConfigurationInterceptor {
	
	@AroundInvoke
	public Object constructConfiguration(final InvocationContext ic) throws Exception {
		final ConfigurationLoader configurationLoader = CDI.current().select(ConfigurationLoader.class).get();

		final Class<?> baseClass = ic.getMethod().getDeclaringClass();
		configurationLoader.load(ic.getTarget(), baseClass, Boolean.TRUE);
		return ic.proceed();
	}
}
