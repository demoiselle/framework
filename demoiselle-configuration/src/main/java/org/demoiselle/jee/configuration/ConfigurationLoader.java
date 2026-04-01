/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.demoiselle.jee.configuration.annotation.ConfigurationIgnore;
import org.demoiselle.jee.configuration.annotation.ConfigurationName;
import org.demoiselle.jee.configuration.annotation.ConfigurationSuppressLogger;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationException;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationValueExtractorException;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationInternalDemoiselleValueExtractor;
import org.demoiselle.jee.configuration.message.ConfigurationMessage;

/**
 *
 * Class responsible for managing the source of a data extraction, identified
 * which fields to be filled, find the extractor for each field type, fill and
 * validate the data field.
 *
 * @author SERPRO
 *
 */
@ApplicationScoped
public class ConfigurationLoader implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Internal metadata record for configuration field information.
     *
     * @param key the configuration key for this field
     * @param field the reflected field
     * @param ignored whether the field is annotated with @ConfigurationIgnore
     * @param suppressLog whether the field is annotated with @ConfigurationSuppressLogger
     */
    record ConfigFieldMeta(String key, Field field, boolean ignored, boolean suppressLog) {
        ConfigFieldMeta {
            Objects.requireNonNull(key);
            Objects.requireNonNull(field);
        }
    }

    /**
     * Internal metadata record for configuration source information.
     *
     * @param type the configuration type (PROPERTIES, XML, SYSTEM)
     * @param resource the resource file name
     * @param prefix the configuration key prefix
     */
    record ConfigSourceMeta(ConfigurationType type, String resource, String prefix) {
        ConfigSourceMeta {
            Objects.requireNonNull(type);
            Objects.requireNonNull(resource);
        }
    }

    @Inject
    private ConfigurationMessage message;

    private static final Logger logger = Logger.getLogger(ConfigurationLoader.class.getName());

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    // Object annotated with @Configuration
    private transient Object targetObject;

    // Class type of object annotated with @Configuration
    private Class<?> targetBaseClass;

    // Apache configuration
    private transient List<Configuration> configurations = null;

    // Fields of object annotated with @Configuration
    private transient List<Field> fields;

    private transient Map<Object, Boolean> loadedCache = null;

    @PostConstruct
    private void init() {
        configurations = new ArrayList<>();
        loadedCache = new ConcurrentHashMap<>();
    }

    /**
     * <p>
     * Processes the annotated class with {@link Configuration}.
     * </p>
     *
     * <p>
     * After the first class configuration procedure is added to the cache to
     * avoid repeated processing.
     * </p>
     *
     * @param object Object annotated with {@link Configuration} to be populated
     * @param baseClass Class type to be populated
     * @throws DemoiselleConfigurationException When there is a problem in the
     * process
     */
    public void load(final Object object, Class<?> baseClass) {
        // Fast read path — multiple threads can read simultaneously
        rwLock.readLock().lock();
        try {
            Boolean isLoaded = loadedCache.get(object);
            if (isLoaded != null && isLoaded) {
                return;  // already loaded, exit fast
            }
        } finally {
            rwLock.readLock().unlock();
        }

        // Write path — only one thread at a time
        rwLock.writeLock().lock();
        try {
            // Double-check after acquiring write lock
            Boolean isLoaded = loadedCache.get(object);
            if (isLoaded == null || !isLoaded) {
                try {
                    processConfiguration(object, baseClass);
                    loadedCache.put(object, true);
                } catch (DemoiselleConfigurationException c) {
                    loadedCache.put(object, false);
                    throw c;
                }
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     *
     * Load values from the selected source and fill object annotated with
     *
     * @Configuration.
     *
     * This method has the engine to process the configuration feature.
     *
     * @param targetObject The object to fill
     * @param targetBaseClass The class type of object to fill
     * @throws DemoiselleConfigurationException
     */
    private void processConfiguration(final Object targetObject, Class<?> targetBaseClass) {

        logger.info("*******************************************************");
        logger.info(message.loadConfigurationClass(targetBaseClass.getName()));

        this.targetObject = targetObject;
        this.targetBaseClass = targetBaseClass;

        loadFieldsFromTargetObject();
        validateFieldsFromTargetObject();

        ConfigSourceMeta sourceMeta = identifySource();
        loadConfigurationType(sourceMeta);

        if (configurations != null && !configurations.isEmpty()) {
            String prefix = identifyPrefix(sourceMeta);
            fillTargetObjectWithValues(prefix);

            // Validate values using JavaBeans Validation
            validateValues();

            printConfiguration(prefix);
        } else {
            // Validate values using JavaBeans Validation
            validateValues();

            printConfiguration("");
        }
    }

    /**
     * Log all configurations
     */
    private void printConfiguration(String prefix) {

        Boolean suppressAllFields = hasSuppressLogger();

        List<ConfigFieldMeta> fieldMetas = buildFieldMetas(prefix);

        fieldMetas.stream().forEach(meta -> {

            if (!meta.ignored()) {
                Object obj = getFieldValueFromObject(meta.field(), targetObject);

                String strMessage = message.configurationFieldLoaded(prefix + meta.key(), obj);

                // Check if the field has @SuppressLogger
                if (suppressAllFields || meta.suppressLog()) {
                    strMessage = message.configurationFieldSuppress(prefix + meta.key(),
                            ConfigurationSuppressLogger.class.getSimpleName());
                }

                logger.info(strMessage);
            }
        });

    }

    private Boolean hasSuppressLogger() {
        return targetObject.getClass().getAnnotation(ConfigurationSuppressLogger.class) == null ? Boolean.FALSE
                : Boolean.TRUE;
    }

    private Boolean hasSuppressLogger(Field field) {
        return field.getAnnotation(ConfigurationSuppressLogger.class) == null ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * Build a ConfigFieldMeta for a single field.
     */
    private ConfigFieldMeta buildFieldMeta(Field field) {
        return new ConfigFieldMeta(
                getKey(field),
                field,
                hasIgnoreAnnotation(field),
                hasSuppressLogger(field)
        );
    }

    /**
     * Build ConfigFieldMeta list for all fields (used by printConfiguration).
     */
    private List<ConfigFieldMeta> buildFieldMetas(String prefix) {
        List<ConfigFieldMeta> metas = new ArrayList<>();
        for (Field field : fields) {
            metas.add(buildFieldMeta(field));
        }
        return metas;
    }

    private void loadFieldsFromTargetObject() {
        fields = getNonStaticFields(targetBaseClass);
    }

    private void validateFieldsFromTargetObject() {
        fields.stream().forEach(this::validateField);
    }

    /**
     * Check if the field param has {@value Name} annotation, if it has,
     * validate if the value is filled
     *
     * @param field Current field
     */
    private void validateField(Field field) {
        ConfigurationName annotation = field.getAnnotation(ConfigurationName.class);

        if (annotation != null && annotation.value().isEmpty()) {
            throw new DemoiselleConfigurationException(
                    message.configurationNameAttributeCantBeEmpty(ConfigurationName.class.getSimpleName()),
                    new IllegalArgumentException());
        }
    }

    /**
     * Identify the configuration source metadata from the @Configuration annotation.
     *
     * @return a ConfigSourceMeta with type, resource and a raw prefix
     */
    private ConfigSourceMeta identifySource() {
        org.demoiselle.jee.configuration.annotation.Configuration annotation =
                targetBaseClass.getAnnotation(org.demoiselle.jee.configuration.annotation.Configuration.class);

        ConfigurationType type = annotation.type();

        String resource;
        if (type != ConfigurationType.SYSTEM) {
            String name = annotation.resource();
            String extension = type.toString().toLowerCase();
            resource = name + "." + extension;
        } else {
            resource = "";
        }

        String prefix = annotation.prefix();

        return new ConfigSourceMeta(type, resource, prefix);
    }

    /**
     * Identify and create Apache Configuration based on type informed on
     * {@link ConfigurationType}
     *
     */
    private void loadConfigurationType(ConfigSourceMeta sourceMeta) {
        BasicConfigurationBuilder<? extends Configuration> builder = createConfiguration(sourceMeta.type());

        configurations.clear();

        try {
            if (builder instanceof FileBasedConfigurationBuilder) {

                Enumeration<URL> urlResources = getResourceAsURL(sourceMeta.resource());

                if (urlResources == null) {
                    throw new DemoiselleConfigurationException(message.fileNotFound(sourceMeta.resource()));
                }

                configureFileBuilder(urlResources, sourceMeta.type());

            } else {
                configurations.add(builder.getConfiguration());
            }
        } catch (IOException | ConfigurationException e) {
            logger.warning(message.failOnCreateApacheConfiguration(e.getMessage()));
        }
    }

    private void configureFileBuilder(Enumeration<URL> urlResources, ConfigurationType configurationType) {

        Parameters params = new Parameters();

        while (urlResources.hasMoreElements()) {
            BasicConfigurationBuilder<? extends Configuration> builder = createConfiguration(configurationType);

            URL url = urlResources.nextElement();

            ((FileBasedConfigurationBuilder<?>) builder).configure(params.fileBased().setURL(url));

            try {
                configurations.add(builder.getConfiguration());
            } catch (ConfigurationException e) {
                logger.warning(message.failOnCreateApacheConfiguration(e.getMessage()));
            }
        }

    }

    private BasicConfigurationBuilder<? extends Configuration> createConfiguration(ConfigurationType configurationType) {
        BasicConfigurationBuilder<? extends Configuration> builder;

        switch (configurationType) {
            case XML:
                builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class);
                break;

            case SYSTEM:
                builder = new BasicConfigurationBuilder<>(SystemConfiguration.class);
                break;

            default:
                builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
        }

        return builder;
    }

    private String identifyPrefix(ConfigSourceMeta sourceMeta) {
        String prefixValue = sourceMeta.prefix();

        if (prefixValue.endsWith(".")) {
            logger.warning(message.configurationDotAfterPrefix(sourceMeta.resource()));
        } else if (!prefixValue.isEmpty()) {
            prefixValue += ".";
        }

        return prefixValue;
    }

    private void fillTargetObjectWithValues(String prefix) {
        fields.stream().forEach(field -> fillFieldWithValue(field, prefix));
    }

    /**
     *
     * Fill the field informed.
     *
     * Get the default value informed on class and retrieve value from source.
     *
     * If the value is present on field and on source, the value from source is
     * selected and the default value is ignored
     *
     * @param field Current field from targetObject
     * @param prefix The configuration key prefix
     */
    private void fillFieldWithValue(Field field, String prefix) {
        ConfigFieldMeta meta = buildFieldMeta(field);

        if (meta.ignored()) {
            return;
        }

        Object defaultValue = getFieldValueFromObject(field, targetObject);
        Object loadedValue = getValueFromSource(field, meta.key(), prefix);
        Object finalValue = loadedValue == null ? defaultValue : loadedValue;

        if (loadedValue == null) {
            logger.info(message.configurationKeyNotFoud(prefix + meta.key()));
        }

        setFieldValue(field, targetObject, finalValue);

    }

    private Object getValueFromSource(Field field, String key, String prefix) {
        Object value = null;

        try {
            ConfigurationValueExtractor extractor = getValueExtractor(field);
            for (Configuration config : configurations) {
                if (value != null) {
                    break;
                }
                value = extractor.getValue(prefix, key, field, config);
            }
        } catch (DemoiselleConfigurationException cause) {
            throw cause;
        } catch (DemoiselleConfigurationValueExtractorException cause) {
            throw new DemoiselleConfigurationException(
                    message.configurationNotConversion(prefix + getKey(field), field.getType().toString()), cause);
        } catch (IllegalStateException | IllegalArgumentException cause) {
            throw new DemoiselleConfigurationException(message.configurationGenericExtractionError(
                    field.getType().toString(), getValueExtractor(field).getClass().getCanonicalName()), cause);
        }

        return value;
    }

    private ConfigurationValueExtractor getValueExtractor(Field field) {
        Set<ConfigurationValueExtractor> candidates = new HashSet<>();

        getExtractors().stream().forEach(extractorClass -> {
            ConfigurationValueExtractor extractor = CDI.current().select(extractorClass).get();
            if (extractor.isSupported(field)) {
                candidates.add(extractor);
            }
        });

        ConfigurationValueExtractor elected = selectValueExtractorElected(candidates);

        if (elected == null) {
            throw new DemoiselleConfigurationException(message.configurationExtractorNotFound(field.toGenericString(),
                    ConfigurationValueExtractor.class.getName()), new ClassNotFoundException());
        }

        return elected;
    }

    private Set<Class<? extends ConfigurationValueExtractor>> getExtractors() {
        Set<Class<? extends ConfigurationValueExtractor>> cache = new HashSet<>();

        try {
            ConfigurationBootstrap bootstrap = CDI.current().select(ConfigurationBootstrap.class).get();
            cache = bootstrap.getCache();
        } catch (IllegalStateException e) {
            // CDI is not already
            logger.finest(message.cdiNotAlready());
        }

        return cache;
    }

    private String getKey(Field field) {
        String key;

        if (field.isAnnotationPresent(ConfigurationName.class)) {
            key = field.getAnnotation(ConfigurationName.class).value();
        } else {
            key = field.getName();
        }

        return key;
    }

    private boolean hasIgnoreAnnotation(Field field) {
        return field.isAnnotationPresent(ConfigurationIgnore.class);
    }

    private void validateValues() {
        fields.stream().forEach((field) -> {
            validateValue(field);
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void validateValue(Field field) {
        ValidatorFactory dfv = Validation.buildDefaultValidatorFactory();
        Validator validator = dfv.getValidator();

        Set violations = validator.validateProperty(targetObject, field.getName());

        StringBuilder messageConstraint = new StringBuilder();

        if (!violations.isEmpty()) {
            for (Iterator iter = violations.iterator(); iter.hasNext();) {
                ConstraintViolation violation = (ConstraintViolation) iter.next();
                messageConstraint.append(field.toGenericString() + " " + violation.getMessage() + "\n");
            }

            throw new DemoiselleConfigurationException(messageConstraint.toString(),
                    new ConstraintViolationException(violations));
        }
    }

    private List<Field> getNonStaticFields(Class<?> type) {
        List<Field> nonStaticfields = new ArrayList<>();

        if (type != null) {
            Class<?> currentType = type;
            while (currentType != null && !Object.class.getCanonicalName().equals(currentType.getCanonicalName())) {
                nonStaticfields.addAll(Arrays.asList(getNonStaticDeclaredFields(currentType)));
                currentType = currentType.getSuperclass();
            }
        }

        return nonStaticfields;
    }

    private Field[] getNonStaticDeclaredFields(Class<?> type) {
        List<Field> nonStaticfields = new ArrayList<>();

        if (type != null) {
            for (Field field : type.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !field.getType().equals(type.getDeclaringClass())) {
                    nonStaticfields.add(field);
                }
            }
        }

        return nonStaticfields.toArray(new Field[0]);
    }

    private Enumeration<URL> getResourceAsURL(final String resource) throws IOException {
        ClassLoader classLoader = getClassLoaderForResource(resource);
        return classLoader != null ? classLoader.getResources(resource) : null;
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

    @SuppressWarnings("unchecked")
    private <T> T getFieldValueFromObject(Field field, Object object) {
        T result = null;

        try {
            boolean acessible = field.isAccessible();
            field.setAccessible(true);
            result = (T) field.get(object);
            field.setAccessible(acessible);

        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new DemoiselleConfigurationException(
                    message.configurationErrorGetValue(field.getName(), object.getClass().getCanonicalName()), e);
        }

        return result;
    }

    private void setFieldValue(Field field, Object object, Object value) {
        try {
            boolean acessible = field.isAccessible();
            field.setAccessible(true);
            field.set(object, value);
            field.setAccessible(acessible);

        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new DemoiselleConfigurationException(
                    message.configurationErrorSetValue(value, field.getName(), object.getClass().getCanonicalName()),
                    e);
        }
    }

    private ConfigurationValueExtractor selectValueExtractorElected(Set<ConfigurationValueExtractor> candidates) {

        Map<Class<? extends ConfigurationValueExtractor>, ConfigurationValueExtractor> map = new ConcurrentHashMap<>();

        candidates.stream().filter(Objects::nonNull).forEach(candidate -> map.putIfAbsent(candidate.getClass(), candidate));

        Class<? extends ConfigurationValueExtractor> elected = selectClass(map.keySet());
        return map.get(elected);
    }

    private Class<? extends ConfigurationValueExtractor> selectClass(
            Set<Class<? extends ConfigurationValueExtractor>> candidates) {

        Class<? extends ConfigurationValueExtractor> selected = null;

        for (Class<? extends ConfigurationValueExtractor> candidate : candidates) {
            Boolean isCandidateInternalValueExtractor = isIntenalValueExtractor(candidate);
            Boolean isSelectedInternalValueExtractor = isIntenalValueExtractor(selected);

            if (selected == null || (isSelectedInternalValueExtractor == Boolean.TRUE
                    && isCandidateInternalValueExtractor == Boolean.FALSE)) {
                selected = candidate;
            }
        }

        if (selected != null) {
            performAmbiguityCheck(selected, candidates);
        }

        return selected;
    }

    private Boolean isIntenalValueExtractor(Class<? extends ConfigurationValueExtractor> candidate) {
        if (candidate == null) {
            return Boolean.FALSE;
        }

        ConfigurationInternalDemoiselleValueExtractor frameworkPackageExtractor = candidate.getPackage()
                .getAnnotation(ConfigurationInternalDemoiselleValueExtractor.class);
        return frameworkPackageExtractor != null ? Boolean.TRUE : Boolean.FALSE;
    }

    private void performAmbiguityCheck(Class<? extends ConfigurationValueExtractor> selected,
            Set<Class<? extends ConfigurationValueExtractor>> candidates) {

        Set<Class<? extends ConfigurationValueExtractor>> ambiguous = new HashSet<>();

        for (Class<? extends ConfigurationValueExtractor> candidate : candidates) {
            Boolean isCandidateInternalValueExtractor = isIntenalValueExtractor(candidate);
            Boolean isSelectedInternalValueExtractor = isIntenalValueExtractor(selected);

            if (selected != candidate && (isSelectedInternalValueExtractor == Boolean.TRUE
                    && isCandidateInternalValueExtractor == Boolean.FALSE)) {
                ambiguous.add(candidate);
            }
        }

        if (!ambiguous.isEmpty()) {
            ambiguous.add(selected);

            String exceptionMessage = getExceptionMessage(ambiguous);
            throw new DemoiselleConfigurationException(exceptionMessage, new AmbiguousResolutionException());
        }
    }

    private String getExceptionMessage(Set<Class<? extends ConfigurationValueExtractor>> ambiguousCandidates) {
        StringBuilder classes = new StringBuilder();

        Class<? extends ConfigurationValueExtractor> type = ambiguousCandidates.iterator().next();
        int i = 0;
        for (Class<? extends ConfigurationValueExtractor> clazz : ambiguousCandidates) {
            if (i++ != 0) {
                classes.append(", ");
            }

            classes.append(clazz.getCanonicalName());
        }

        return message.ambigousStrategyResolution(type.getCanonicalName(), classes.toString());
    }
}
