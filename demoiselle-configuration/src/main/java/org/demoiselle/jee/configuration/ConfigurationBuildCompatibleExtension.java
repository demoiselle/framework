/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;

/**
 * CDI 4.0 Lite Build-Compatible Extension that discovers implementations of
 * {@link ConfigurationValueExtractor} and registers a synthetic bean containing
 * the complete set of discovered extractor classes.
 * <p>
 * This extension replaces the portable extension {@link ConfigurationBootstrap}
 * for containers that support CDI Lite (build-time processing). The original
 * portable extension is kept as fallback for containers that only support CDI Full.
 * </p>
 *
 * @author SERPRO
 * @see ConfigurationBootstrap
 */
public class ConfigurationBuildCompatibleExtension implements BuildCompatibleExtension {

    private static final Logger logger = Logger.getLogger(ConfigurationBuildCompatibleExtension.class.getName());

    private final Set<String> extractorClassNames = new HashSet<>();

    /**
     * Discovery phase — ensures ConfigurationValueExtractor subtypes are scanned.
     */
    @Discovery
    public void discovery(ScannedClasses scan) {
        logger.fine("ConfigurationBuildCompatibleExtension: discovery phase started");
    }

    /**
     * Enhancement phase — collects classes that implement {@link ConfigurationValueExtractor}.
     * Filters out annotations, interfaces, synthetic classes, arrays, and enums,
     * matching the same filtering logic as {@link ConfigurationBootstrap#processAnnotatedType}.
     */
    @Enhancement(types = ConfigurationValueExtractor.class)
    public void collectExtractors(ClassInfo classInfo) {
        if (classInfo.isAnnotation() || classInfo.isInterface()) {
            return;
        }
        String className = classInfo.name();
        logger.fine("Discovered ConfigurationValueExtractor via BCE: " + className);
        extractorClassNames.add(className);
    }

    /**
     * Synthesis phase — registers a synthetic ApplicationScoped bean that holds
     * the {@code Set<Class<? extends ConfigurationValueExtractor>>} cache.
     * This bean is functionally equivalent to the cache maintained by
     * {@link ConfigurationBootstrap}.
     */
    @Synthesis
    public void registerExtractorRegistry(SyntheticComponents syn) {
        syn.addBean(ExtractorRegistry.class)
            .type(ExtractorRegistry.class)
            .qualifier(Default.class)
            .scope(ApplicationScoped.class)
            .createWith(ExtractorRegistryCreator.class)
            .withParam("classNames", extractorClassNames.toArray(new String[0]));
        logger.fine("Registered synthetic ExtractorRegistry bean via BCE with "
            + extractorClassNames.size() + " extractors");
    }

    /**
     * Registry bean that holds the set of discovered {@link ConfigurationValueExtractor}
     * implementation classes. Injected by {@link ConfigurationLoader} as an alternative
     * to {@link ConfigurationBootstrap#getCache()}.
     */
    public static class ExtractorRegistry {

        private final Set<Class<? extends ConfigurationValueExtractor>> cache;

        public ExtractorRegistry(Set<Class<? extends ConfigurationValueExtractor>> cache) {
            this.cache = ConcurrentHashMap.newKeySet();
            this.cache.addAll(cache);
        }

        public Set<Class<? extends ConfigurationValueExtractor>> getCache() {
            return cache;
        }
    }

    /**
     * Synthetic bean creator that produces an {@link ExtractorRegistry} instance
     * containing all discovered ConfigurationValueExtractor classes.
     */
    public static class ExtractorRegistryCreator implements SyntheticBeanCreator<ExtractorRegistry> {

        @SuppressWarnings("unchecked")
        @Override
        public ExtractorRegistry create(jakarta.enterprise.inject.Instance<Object> lookup, Parameters params) {
            String[] classNames = params.get("classNames", String[].class);
            Set<Class<? extends ConfigurationValueExtractor>> classes = new HashSet<>();
            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className);
                    if (ConfigurationValueExtractor.class.isAssignableFrom(clazz)) {
                        classes.add((Class<? extends ConfigurationValueExtractor>) clazz);
                    }
                } catch (ClassNotFoundException e) {
                    Logger.getLogger(ConfigurationBuildCompatibleExtension.class.getName())
                        .log(Level.WARNING,
                            "Could not load ConfigurationValueExtractor class: " + className, e);
                }
            }
            return new ExtractorRegistry(classes);
        }
    }
}
