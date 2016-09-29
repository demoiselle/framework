/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.internal.producer;

import java.io.Serializable;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 *
 * @author 70744416353
 */
@Dependent
public class LoggerProducer implements Serializable {

    private static final long serialVersionUID = 1L;

    /*
	 * Produces a default {@link Logger} instance. If it's possible
	 * to infer the injection point's parent class then this class'es
	 * name will be used to categorize the logger, if not then
	 * the logger won't be categorized.
	 *
     */
    @Default
    @Produces
    public static final Logger create(final InjectionPoint ip) {
        String name;

        if (ip != null && ip.getMember() != null) {
            name = ip.getMember().getDeclaringClass().getName();
        } else {
            name = "not.categorized";
        }

        return getLogger(name);
    }

}
