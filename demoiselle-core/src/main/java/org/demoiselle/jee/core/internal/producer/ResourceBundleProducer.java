/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.internal.producer;

import static java.util.Locale.getDefault;
import static javax.enterprise.inject.spi.CDI.current;
import static org.demoiselle.jee.core.util.CDIUtils.getQualifier;

import java.util.Locale;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.demoiselle.jee.core.annotation.Name;
import org.demoiselle.jee.core.util.ResourceBundle;

/**
 *
 * @author 70744416353
 */
@Dependent
public class ResourceBundleProducer {

    /**
     *
     * @return ResourceBundle
     */
    @Default
    @Produces
    public ResourceBundle createDefault() {
        return create((String) null);
    }

    /*
    * Produces a {@link java.util.ResourceBundle} instance loading the
    * properties file whose name is defined by the {@link Name} literal. If no
    * value is specified then the default "messages.properties" file is loaded.
    * @param ip
    * @return
     */
    @Name
    @Produces
    public ResourceBundle create(InjectionPoint ip) {
        String baseName = null;
        if (ip != null && ip.getQualifiers() != null) {
            Name nameQualifier = getQualifier(Name.class, ip);
            if (nameQualifier != null) {
                baseName = nameQualifier.value();
                if ("".equals(baseName)) {
                    baseName = null;
                }
            }
        }

        return create(baseName);
    }

    /**
     *
     * @param baseName Base name
     * @return {@link ResourceBundle}
     */
    @SuppressWarnings("serial")
    public static ResourceBundle create(String baseName) {
        ResourceBundle bundle;

        try {
            bundle = baseName != null ? new ResourceBundle(baseName, current().select(Locale.class).get()) {
            } : new ResourceBundle("messages", current().select(Locale.class).get());
        } catch (RuntimeException e) {
            bundle = baseName != null ? new ResourceBundle(baseName, getDefault())
                    : new ResourceBundle("messages", getDefault());
        }

        return bundle;
    }
}
