/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;

/**
 * 
 * Class responsible for loading all extractors classes that implement the
 * {@link ConfigurationValueExtractor} interface.
 * 
 * @author SERPRO
 *
 */
public class ConfigurationBootstrap implements Extension {

    private Set<Class<? extends ConfigurationValueExtractor>> cache;

    protected Set<Class<? extends ConfigurationValueExtractor>> getCache() {
        if (this.cache == null) {
            this.cache = ConcurrentHashMap.newKeySet();
        }

        return this.cache;
    }

    /**
     * Process all classes that extends {@link ConfigurationValueExtractor} and
     * add the own class type on cache.
     * 
     * @param pat
     *            ProcessAnnotatedType used by CDI
     */
    public void processAnnotatedType(@Observes final ProcessAnnotatedType<? extends ConfigurationValueExtractor> pat) {

        Class<? extends ConfigurationValueExtractor> pcsClass = pat.getAnnotatedType().getJavaClass();

        if (pcsClass.isAnnotation() || pcsClass.isInterface() || pcsClass.isSynthetic() || pcsClass.isArray()
                || pcsClass.isEnum()) {
            return;
        }

        this.getCache().add(pat.getAnnotatedType().getJavaClass());
    }

}
