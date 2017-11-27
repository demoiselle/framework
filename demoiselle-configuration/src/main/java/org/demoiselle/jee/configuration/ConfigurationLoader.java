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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

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

    @Inject
    private ConfigurationMessage message;

    private static final Logger logger = Logger.getLogger(ConfigurationLoader.class.getName());

    // Object annotated with @Configuration
    private transient Object targetObject;

    // Class type of object annotated with @Configuration
    private Class<?> targetBaseClass;

    // Type of source
    private ConfigurationType configurationType;

    private String resource;

    private String prefix;

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
    public synchronized void load(final Object object, Class<?> baseClass) {
        Boolean isLoaded = loadedCache.get(object);

        if (isLoaded == null || !isLoaded) {
            try {
                processConfiguration(object, baseClass);
                loadedCache.putIfAbsent(object, true);
            } catch (DemoiselleConfigurationException c) {
                loadedCache.putIfAbsent(object, false);
                throw c;
            }
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

        identifyConfigurationType();
        identifyResourceName();
        loadConfigurationType();

        if (configurations != null && !configurations.isEmpty()) {
            identifyPrefix();
            fillTargetObjectWithValues();
        }

        // Validate values using JavaBeans Validation
        validateValues();

        printConfiguration();
    }

    /**
     * Log all configurations
     */
    private void printConfiguration() {

        Boolean suppressAllFields = hasSuppressLogger();

        fields.stream().forEach(field -> {

            if (!hasIgnoreAnnotation(field)) {
                Object obj = getFieldValueFromObject(field, targetObject);

                String strMessage = message.configurationFieldLoaded(prefix + getKey(field), obj);

                // Check if the field has @SuppressLogger
                if (suppressAllFields || hasSuppressLogger(field)) {
                    strMessage = message.configurationFieldSuppress(prefix + getKey(field),
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

    private void identifyConfigurationType() {
        configurationType = targetBaseClass
                .getAnnotation(org.demoiselle.jee.configuration.annotation.Configuration.class).type();
    }

    /**
     * Load the name of resource that contains the values to fill object. Unless
     * with the type of configuration is SYSTEM type.
     */
    private void identifyResourceName() {
        if (configurationType != ConfigurationType.SYSTEM) {
            String name = targetBaseClass.getAnnotation(org.demoiselle.jee.configuration.annotation.Configuration.class)
                    .resource();
            String extension = configurationType.toString().toLowerCase();

            resource = name + "." + extension;
        }
    }

    /**
     * Identify and create Apache Configuration based on type informed on
     * {@link ConfigurationType}
     *
     */
    private void loadConfigurationType() {
        BasicConfigurationBuilder<? extends Configuration> builder = createConfiguration();

        configurations.clear();

        try {
            if (builder instanceof FileBasedConfigurationBuilder) {

                Enumeration<URL> urlResources = getResourceAsURL(resource);

                if (urlResources == null) {
                    throw new DemoiselleConfigurationException(message.fileNotFound(resource));
                }

                configureFileBuilder(urlResources);

            } else {
                configurations.add(builder.getConfiguration());
            }
        } catch (IOException | ConfigurationException e) {
            logger.warning(message.failOnCreateApacheConfiguration(e.getMessage()));
        }
    }

    private void configureFileBuilder(Enumeration<URL> urlResources) {

        Parameters params = new Parameters();

        while (urlResources.hasMoreElements()) {
            BasicConfigurationBuilder<? extends Configuration> builder = createConfiguration();

            URL url = urlResources.nextElement();

            ((FileBasedConfigurationBuilder<?>) builder).configure(params.fileBased().setURL(url));

            try {
                configurations.add(builder.getConfiguration());
            } catch (ConfigurationException e) {
                logger.warning(message.failOnCreateApacheConfiguration(e.getMessage()));
            }
        }

    }

    private BasicConfigurationBuilder<? extends Configuration> createConfiguration() {
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

    private void identifyPrefix() {
        String prefixValue = targetBaseClass
                .getAnnotation(org.demoiselle.jee.configuration.annotation.Configuration.class).prefix();

        if (prefixValue.endsWith(".")) {
            logger.warning(message.configurationDotAfterPrefix(resource));
        } else if (!prefixValue.isEmpty()) {
            prefixValue += ".";
        }

        prefix = prefixValue;
    }

    private void fillTargetObjectWithValues() {
        fields.stream().forEach(this::fillFieldWithValue);
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
     */
    private void fillFieldWithValue(Field field) {
        if (hasIgnoreAnnotation(field)) {
            return;
        }

        Object defaultValue = getFieldValueFromObject(field, targetObject);
        Object loadedValue = getValueFromSource(field, getKey(field));
        Object finalValue = loadedValue == null ? defaultValue : loadedValue;

        if (loadedValue == null) {
            logger.info(message.configurationKeyNotFoud(prefix + getKey(field)));
        }

        setFieldValue(field, targetObject, finalValue);

    }

    private Object getValueFromSource(Field field, String key) {
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
        } catch (Exception cause) {
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

        } catch (Exception e) {
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

        } catch (Exception e) {
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
