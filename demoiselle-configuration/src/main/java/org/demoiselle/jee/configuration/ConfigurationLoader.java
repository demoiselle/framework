/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import static org.demoiselle.jee.configuration.ConfigurationType.SYSTEM;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
 */
@ApplicationScoped
public class ConfigurationLoader implements Serializable {
	//TODO comentar os metodos explicando em que situacao sao usados

	private static final long serialVersionUID = 1L;

	@Inject
	private ConfigurationMessage bundle;

	@Inject
	private Logger logger;

	//TODO explicar
	private Object object;

	private Class<?> baseClass;

	private ConfigurationType type;

	private String resource;

	private String prefix;

	private Configuration configuration;

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
	 * @param logLoadingProcess Enable logging or not the process
	 * @throws DemoiselleConfigurationException When there is a problem in the process 
	 */
	public void load(final Object object, Class<?> baseClass, boolean logLoadingProcess) throws DemoiselleConfigurationException {
		Boolean isLoaded = loadedCache.get(object);

		if (isLoaded == null || !isLoaded) {
			try {
				loadConfiguration(object, baseClass, logLoadingProcess);
				loadedCache.put(object, true);
			} catch (DemoiselleConfigurationException c) {
				loadedCache.put(object, false);
				throw c;
			}
		}
	}

	//TODO comentar
	private void loadConfiguration(final Object object, Class<?> baseClass, boolean logLoadingProcess)
			throws DemoiselleConfigurationException {
		
		//TODO avaliar finest
		if (logLoadingProcess) {
			logger.fine(bundle.loadConfigurationClass(baseClass.getName()));
		}

		this.object = object;
		this.baseClass = baseClass;

		loadFields();
		validateFields();

		loadType();
		loadResource();
		loadConfiguration();

		if (this.configuration != null) {
			loadPrefix();
			loadValues();
		}

		validateValues();
	}

	private void loadFields() {
		this.fields = getNonStaticFields(baseClass);
	}

	private void validateFields() {
		this.fields.forEach(this::validateField);
	}

	private void validateField(Field field) {
		Name annotation = field.getAnnotation(Name.class);

		if (annotation != null && annotation.value().isEmpty()) {
			throw new DemoiselleConfigurationException(bundle.configurationNameAttributeCantBeEmpty(), new IllegalArgumentException());
		}
	}

	private void loadType() {
		this.type = baseClass.getAnnotation(org.demoiselle.jee.configuration.annotation.Configuration.class).type();
	}

	private void loadResource() {
		if (this.type != SYSTEM) {
			String name = baseClass.getAnnotation(org.demoiselle.jee.configuration.annotation.Configuration.class).resource();
			String extension = this.type.toString().toLowerCase();

			this.resource = name + "." + extension;
		}
	}

	private void loadConfiguration() {
		Configuration config;
		BasicConfigurationBuilder<? extends Configuration> builder = createConfiguration();

		if (builder instanceof FileBasedConfigurationBuilder) {
			Parameters params = new Parameters();

			URL urlResource = getResourceAsURL(this.resource);

			if(urlResource == null) {
				throw new DemoiselleConfigurationException(bundle.fileNotFound(this.resource));				
			}
			
			((FileBasedConfigurationBuilder<?>) builder).configure(params.fileBased().setURL(getResourceAsURL(this.resource)));
			
		}

		try {
			config = builder.getConfiguration();
		} 
		catch (ConfigurationException e) {
			//TODO THROW Ou Log warn
			config = null;
		}

		this.configuration = config;
	}

	private BasicConfigurationBuilder<? extends Configuration> createConfiguration() {
		BasicConfigurationBuilder<? extends Configuration> builder;

		switch (this.type) {
			case XML:
				builder = new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class);
				break;

			case SYSTEM:
				builder = new BasicConfigurationBuilder<>(SystemConfiguration.class);
				break;

			default:
				builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class);
		}

		return builder;
	}

	private void loadPrefix() {
		String prefix = baseClass.getAnnotation(org.demoiselle.jee.configuration.annotation.Configuration.class).prefix();

		if (prefix.endsWith(".")) {
			logger.warning(bundle.configurationDotAfterPrefix(this.resource));
		} else if (!prefix.isEmpty()) {
			prefix += ".";
		}

		this.prefix = prefix;
	}

	private void loadValues() {
		this.fields.forEach(this::loadValue);
	}

	private void loadValue(Field field) {
		if (hasIgnore(field)) {
			return;
		}

		Object defaultValue = getFieldValue(field, this.object);
		Object loadedValue = getValue(field, field.getType(), getKey(field), defaultValue);
		Object finalValue = (loadedValue == null ? defaultValue : loadedValue);

		if (loadedValue == null) {
			logger.fine(bundle.configurationKeyNotFoud(this.prefix + getKey(field)));
		}

		setFieldValue(field, this.object, finalValue);
		logger.finer(bundle.configurationFieldLoaded(this.prefix + getKey(field), finalValue == null ? "null" : finalValue));
	}

	private Object getValue(Field field, Class<?> type, String key, Object defaultValue) {
		Object value = null;
		
		try {
			ConfigurationValueExtractor extractor = getValueExtractor(field);
			value = extractor.getValue(this.prefix, key, field, this.configuration);
		} 
		catch (DemoiselleConfigurationException cause) {
			throw cause;
		} 
		catch (ConversionException cause) {
			throw new DemoiselleConfigurationException(bundle.configurationNotConversion(this.prefix + getKey(field), field.getType().toString()), cause);
		} 
		catch (Exception cause) {
			throw new DemoiselleConfigurationException(bundle.configurationGenericExtractionError(field.getType().toString(), getValueExtractor(field).getClass().getCanonicalName()), cause);
		}

		return value;
	}

	private ConfigurationValueExtractor getValueExtractor(Field field) {
		Collection<ConfigurationValueExtractor> candidates = new HashSet<ConfigurationValueExtractor>();
		ConfigurationBootstrap bootstrap = CDI.current().select(ConfigurationBootstrap.class).get();
		//TODO rever lambda paralelismo
		for (Class<? extends ConfigurationValueExtractor> extractorClass : bootstrap.getCache()) {
			ConfigurationValueExtractor extractor = CDI.current().select(extractorClass).get();

			if (extractor.isSupported(field)) {
				candidates.add(extractor);
			}
		}

		ConfigurationValueExtractor elected = selectReference(ConfigurationValueExtractor.class, candidates);
		
		if (elected == null) {
			throw new DemoiselleConfigurationException(bundle.configurationExtractorNotFound(field.toGenericString(), 					
							ConfigurationValueExtractor.class.getName()), new ClassNotFoundException());
		}
		
		return elected;
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

	private boolean hasIgnore(Field field) {
		return field.isAnnotationPresent(Ignore.class);
	}

	private void validateValues() {
		for (Field field : this.fields) {
			validateValue(field, getFieldValue(field, this.object));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void validateValue(Field field, Object value) {
		ValidatorFactory dfv = Validation.buildDefaultValidatorFactory();
		Validator validator = dfv.getValidator();

		Set violations = validator.validateProperty(this.object, field.getName());

		StringBuilder message = new StringBuilder();

		if (!violations.isEmpty()) {
			for (Iterator iter = violations.iterator(); iter.hasNext(); ) {
				ConstraintViolation violation = (ConstraintViolation) iter.next();
				message.append(field.toGenericString() + " " + violation.getMessage() + "\n");
			}

			throw new DemoiselleConfigurationException(message.toString(), new ConstraintViolationException(violations));
		}
	}
	
	private List<Field> getNonStaticFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();

		if (type != null) {
			Class<?> currentType = type;
			while (currentType != null && !"java.lang.Object".equals(currentType.getCanonicalName())) {
				fields.addAll(Arrays.asList(getNonStaticDeclaredFields(currentType)));
				currentType = currentType.getSuperclass();
			}
		}

		return fields;
	}
	
	private Field[] getNonStaticDeclaredFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();

		if (type != null) {
			for (Field field : type.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers()) && !field.getType().equals(type.getDeclaringClass())) {
					fields.add(field);
				}
			}
		}

		return fields.toArray(new Field[0]);
	}
	
	private URL getResourceAsURL(final String resource) {
		ClassLoader classLoader = getClassLoaderForResource(resource);
		return classLoader != null ? classLoader.getResource(resource) : null;
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
	private <T> T getFieldValue(Field field, Object object) {
		T result = null;

		try {
			boolean acessible = field.isAccessible();
			field.setAccessible(true);
			result = (T) field.get(object);
			field.setAccessible(acessible);

		} catch (Exception e) {
			throw new DemoiselleConfigurationException(bundle.configurationErrorGetValue(field.getName(), object.getClass().getCanonicalName()), e);
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
			throw new DemoiselleConfigurationException(bundle.configurationErrorSetValue(value, field.getName(), object.getClass().getCanonicalName()), e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T selectReference(Class<T> type, Collection<? extends T> options) {

		Map<Class<? extends T>, T> map = new HashMap<>();

		options.stream()
				.filter(instance -> instance != null)
				.forEach(instance -> {
					map.put((Class<T>) instance.getClass(), instance);
				});

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

			String message = getExceptionMessage(type, ambiguous);
			throw new DemoiselleConfigurationException(message, new AmbiguousResolutionException());
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

		return bundle.ambigousStrategyResolution(type.getCanonicalName(), classes.toString());
	}
}

