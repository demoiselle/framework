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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.demoiselle.jee.configuration.annotation.SuppressConfigurationLogger;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationException;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.configuration.message.ConfigurationMessage;
import org.demoiselle.jee.core.annotation.Ignore;
import org.demoiselle.jee.core.annotation.Name;
import org.demoiselle.jee.core.annotation.Priority;

/**
 * 
 * Class responsible for managing the source of a data extraction, identified which fields to be filled, 
 * find the extractor for each field type, fill and validate the data field.
 * 
 * @author SERPRO
 *
 */
@ApplicationScoped
public class ConfigurationLoader implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Inject
	private ConfigurationMessage message;

	@Inject
	private Logger logger;

	// Object annotated with @Configuration
	private Object targetObject;

	// Class type of object annotated with @Configuration
	private Class<?> baseClass;

	// Type of source
	private ConfigurationType configurationType;

	private String resource;

	private String prefix;

	// Apache configuration 
	private List<Configuration> configurations = new LinkedList<>();

	// Fields of object annotated with @Configuration
	private Collection<Field> fields;

	private final Map<Object, Boolean> loadedCache = new ConcurrentHashMap<>();
	
	/**
	 * <p>
	 * Processes the annotated class with {@link Configuration}.
	 * </p>
	 * 
	 * <p>
	 * After the first class configuration procedure is added to the cache to avoid repeated processing.
	 * </p>
	 * 
	 * @param object Object annotated with {@link Configuration} to be populated
	 * @param baseClass Class type to be populated
	 * @throws DemoiselleConfigurationException When there is a problem in the process 
	 */
	public void load(final Object object, Class<?> baseClass) {
		Boolean isLoaded = loadedCache.get(object);

		if (isLoaded == null || !isLoaded) {
			try {
				processConfiguration(object, baseClass);
				loadedCache.put(object, true);
			} 
			catch (DemoiselleConfigurationException c) {
				loadedCache.put(object, false);
				throw c;
			}
		}
	}

	/**
	 * 
	 * Load values from the selected source and fill object annotated with @Configuration.
	 * 
	 * This method has the engine to process the configuration feature.
	 * 
	 * @param object The object to fill
	 * @param baseClass The class type of object to fill
	 * @throws DemoiselleConfigurationException
	 */
	private void processConfiguration(final Object object, Class<?> baseClass) {
		
		this.logger.info("*******************************************************");
		this.logger.info(this.message.loadConfigurationClass(baseClass.getName()));

		this.targetObject = object;
		this.baseClass = baseClass;

		loadFieldsFromTargetObject();
		validateFieldsFromTargetObject();

		identifyConfigurationType();
		identifyResourceName();
		loadConfigurationType();
		
		if(this.configurations != null && !this.configurations.isEmpty()){
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
		
		this.fields.forEach(field -> {
			
			Object obj = getFieldValueFromObject(field, this.targetObject); 
			
			String strMessage = message.configurationFieldLoaded(this.prefix + getKey(field), obj);
			
			//Check if the field has @SuppressLogger 
			if(suppressAllFields || hasSuppressLogger(field)){
				strMessage = message.configurationFieldSuppress(this.prefix + getKey(field));
			}
			
			this.logger.info(strMessage);
		});
			
	}
	
	private Boolean hasSuppressLogger(){
		return this.targetObject.getClass().getAnnotation(SuppressConfigurationLogger.class) == null ? Boolean.FALSE : Boolean.TRUE;
	}
	
	private Boolean hasSuppressLogger(Field field){
		return field.getAnnotation(SuppressConfigurationLogger.class) == null ? Boolean.FALSE : Boolean.TRUE;
	}
	
	private void loadFieldsFromTargetObject() {
		this.fields = getNonStaticFields(baseClass);
	}

	private void validateFieldsFromTargetObject() {
		this.fields.forEach(this::validateField);
	}

	/**
	 * Check if the field param has {@value Name} annotation, if it has, validate if the value is filled
	 * 
	 * @param field Current field
	 */
	private void validateField(Field field) {
		Name annotation = field.getAnnotation(Name.class);

		if (annotation != null && annotation.value().isEmpty()) {
			throw new DemoiselleConfigurationException(message.configurationNameAttributeCantBeEmpty(), new IllegalArgumentException());
		}
	}

	private void identifyConfigurationType() {
		this.configurationType = baseClass.getAnnotation(org.demoiselle.jee.configuration.annotation.Configuration.class).type();
	}

	/**
	 * Load the name of resource that contains the values to fill object.
	 * Unless with the type of configuration is SYSTEM type.
	 */
	private void identifyResourceName() {
		if (this.configurationType != ConfigurationType.SYSTEM) {
			String name = baseClass.getAnnotation(org.demoiselle.jee.configuration.annotation.Configuration.class).resource();
			String extension = this.configurationType.toString().toLowerCase();

			this.resource = name + "." + extension;
		}
	}

	/**
	 * Identify and create Apache Configuration based on type informed on {@link ConfigurationType}  
	 * 
	 */
	private void loadConfigurationType() {
		BasicConfigurationBuilder<? extends Configuration> builder = createConfiguration();

		this.configurations.clear();
		
		try{
			if (builder instanceof FileBasedConfigurationBuilder) {
				
				Enumeration<URL> urlResources = getResourceAsURL(this.resource);
				
				if(urlResources == null) {
					throw new DemoiselleConfigurationException(message.fileNotFound(this.resource));				
				}
				
				configureFileBuilder(urlResources);
				
			}
			else{
				this.configurations.add(builder.getConfiguration());
			}
		}
		catch (IOException | ConfigurationException e) {
			this.logger.warning(message.failOnCreateApacheConfiguration(e.getMessage()));
		}
	}

	private void configureFileBuilder(Enumeration<URL> urlResources) {
		
		Parameters params = new Parameters();
		
		while(urlResources.hasMoreElements()){
			BasicConfigurationBuilder<? extends Configuration> builder = createConfiguration();
			
			URL url = urlResources.nextElement();
			
			((FileBasedConfigurationBuilder<?>) builder).configure(params.fileBased().setURL(url));
			
			try {
				this.configurations.add(builder.getConfiguration());
			} 
			catch (ConfigurationException e) {
				this.logger.warning(message.failOnCreateApacheConfiguration(e.getMessage()));
			}
		}
		
	}

	private BasicConfigurationBuilder<? extends Configuration> createConfiguration() {
		BasicConfigurationBuilder<? extends Configuration> builder;

		switch (this.configurationType) {
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
		String prefixValue = baseClass.getAnnotation(org.demoiselle.jee.configuration.annotation.Configuration.class).prefix();

		if (prefixValue.endsWith(".")) {
			this.logger.warning(message.configurationDotAfterPrefix(this.resource));
		} 
		else if (!prefixValue.isEmpty()) {
			prefixValue += ".";
		}

		this.prefix = prefixValue;
	}

	private void fillTargetObjectWithValues() {
		this.fields.forEach(this::fillFieldWithValue);
	}

	/**
	 * 
	 * Fill the field informed.
	 * 
	 * Get the default value informed on class and retrieve value from source.
	 * 
	 * If the value is present on field and on source, the value from source is selected and the default value is ignored
	 * 
	 * @param field Current field from targetObject
	 */
	private void fillFieldWithValue(Field field) {
		if (hasIgnoreAnnotation(field)) {
			return;
		}

		Object defaultValue = getFieldValueFromObject(field, this.targetObject);
		Object loadedValue = getValueFromSource(field, getKey(field), defaultValue);
		Object finalValue = loadedValue == null ? defaultValue : loadedValue;

		if (loadedValue == null) {
			this.logger.info(message.configurationKeyNotFoud(this.prefix + getKey(field)));
		}

		setFieldValue(field, this.targetObject, finalValue);
		
	}

	private Object getValueFromSource(Field field, String key, Object defaultValue) {
		Object value = null;
		
		try {
			ConfigurationValueExtractor extractor = getValueExtractor(field);
			for(Configuration config : this.configurations){
				if(value != null) {
					break;
				}
				value = extractor.getValue(this.prefix, key, field, config);
			}
		} 
		catch (DemoiselleConfigurationException cause) {
			throw cause;
		} 
		catch (ConversionException cause) {
			throw new DemoiselleConfigurationException(message.configurationNotConversion(this.prefix + getKey(field), field.getType().toString()), cause);
		} 
		catch (Exception cause) {
			throw new DemoiselleConfigurationException(message.configurationGenericExtractionError(field.getType().toString(), getValueExtractor(field).getClass().getCanonicalName()), cause);
		}

		return value;
	}

	private ConfigurationValueExtractor getValueExtractor(Field field) {
		Collection<ConfigurationValueExtractor> candidates = new HashSet<>();
		
		getExtractors().forEach(extractorClass ->{
			ConfigurationValueExtractor extractor = CDI.current().select(extractorClass).get();
			if (extractor.isSupported(field)) {
				candidates.add(extractor);
			}
		});
				
		ConfigurationValueExtractor elected = selectElectedReference(ConfigurationValueExtractor.class, candidates);
		
		if (elected == null) {
			throw new DemoiselleConfigurationException(message.configurationExtractorNotFound(field.toGenericString(), 					
							ConfigurationValueExtractor.class.getName()), new ClassNotFoundException());
		}
		
		return elected;
	}
	
	private Set<Class<? extends ConfigurationValueExtractor>> getExtractors(){
		Set<Class<? extends ConfigurationValueExtractor>> cache = new HashSet<>();
		
		try{
			ConfigurationBootstrap bootstrap = CDI.current().select(ConfigurationBootstrap.class).get();
			cache = bootstrap.getCache();
		}
		catch(IllegalStateException e){
			//CDI is not already
		}
		
		return cache;
	}

	private String getKey(Field field) {
		String key;

		if (field.isAnnotationPresent(Name.class)) {
			key = field.getAnnotation(Name.class).value();
		} else {
			key = field.getName();
		}

		return key;
	}

	private boolean hasIgnoreAnnotation(Field field) {
		return field.isAnnotationPresent(Ignore.class);
	}

	private void validateValues() {
		for (Field field : this.fields) {
			validateValue(field);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void validateValue(Field field){
		ValidatorFactory dfv = Validation.buildDefaultValidatorFactory();
		Validator validator = dfv.getValidator();

		Set violations = validator.validateProperty(this.targetObject, field.getName());

		StringBuilder messageConstraint = new StringBuilder();

		if (!violations.isEmpty()) {
			for (Iterator iter = violations.iterator(); iter.hasNext(); ) {
				ConstraintViolation violation = (ConstraintViolation) iter.next();
				messageConstraint.append(field.toGenericString() + " " + violation.getMessage() + "\n");
			}

			throw new DemoiselleConfigurationException(messageConstraint.toString(), new ConstraintViolationException(violations));
		}
	}
	
	private List<Field> getNonStaticFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();

		if(type != null) {
			Class<?> currentType = type;
			while(currentType != null && !Object.class.getCanonicalName().equals(currentType.getCanonicalName())) {
				fields.addAll(Arrays.asList(getNonStaticDeclaredFields(currentType)));
				currentType = currentType.getSuperclass();
			}
		}

		return fields;
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
			throw new DemoiselleConfigurationException(message.configurationErrorGetValue(field.getName(), object.getClass().getCanonicalName()), e);
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
			throw new DemoiselleConfigurationException(message.configurationErrorSetValue(value, field.getName(), object.getClass().getCanonicalName()), e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T selectElectedReference(Class<T> type, Collection<? extends T> options) {

		Map<Class<? extends T>, T> map = new HashMap<>();

		options.stream()
				.filter(Objects::nonNull)
				.forEach(instance -> map.put((Class<T>) instance.getClass(), instance));

		Class<? extends T> elected = selectClass(type, map.keySet());
		return map.get(elected);
	}
	
	private <T> Class<? extends T> selectClass(Class<T> type, Collection<Class<? extends T>> options) {
		Class<? extends T> selected = null;

		for (Class<? extends T> option : options) {
			if (selected == null || getPriority(option) < getPriority(selected)) {
				selected = option;
			}
		}

		if (selected != null) {
			performAmbiguityCheck(type, selected, options);
		}

		return selected;
	}
	
	private <T> void performAmbiguityCheck(Class<T> type, Class<? extends T> selected,
			Collection<Class<? extends T>> options) {
		int selectedPriority = getPriority(selected);

		List<Class<? extends T>> ambiguous = new ArrayList<Class<? extends T>>();

		for (Class<? extends T> option : options) {
			if (selected != option && selectedPriority == getPriority(option)) {
				ambiguous.add(option);
			}
		}

		if (!ambiguous.isEmpty()) {
			ambiguous.add(selected);

			String exceptionMessage = getExceptionMessage(type, ambiguous);
			throw new DemoiselleConfigurationException(exceptionMessage, new AmbiguousResolutionException());
		}
	}
	
	private <T> int getPriority(Class<T> type) {
		int result = Priority.MAX_PRIORITY;
		Priority priority = type.getAnnotation(Priority.class);

		if (priority != null) {
			result = priority.value();
		}

		return result;
	}
	
	private <T> String getExceptionMessage(Class<T> type, List<Class<? extends T>> ambiguous) {
		StringBuffer classes = new StringBuffer();

		int i = 0;
		for (Class<? extends T> clazz : ambiguous) {
			if (i++ != 0) {
				classes.append(", ");
			}

			classes.append(clazz.getCanonicalName());
		}

		return message.ambigousStrategyResolution(type.getCanonicalName(), classes.toString());
	}
}

