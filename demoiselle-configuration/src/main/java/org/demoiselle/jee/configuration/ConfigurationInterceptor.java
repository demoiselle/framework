/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.CDI;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * 
 * Intercepts annotated class with {@link Configuration} and delegates
 * processing to the class {@link ConfigurationLoader}
 * 
 * @author SERPRO
 */
@Dependent
@Configuration
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class ConfigurationInterceptor {

    @AroundInvoke
    public Object constructConfiguration(final InvocationContext ic) throws Exception {
        final ConfigurationLoader configurationLoader = CDI.current().select(ConfigurationLoader.class).get();

        final Class<?> baseClass = ic.getMethod().getDeclaringClass();
        configurationLoader.load(ic.getTarget(), baseClass);
        return ic.proceed();
    }
}
