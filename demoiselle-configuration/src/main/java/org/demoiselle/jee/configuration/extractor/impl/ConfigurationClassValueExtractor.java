package org.demoiselle.jee.configuration.extractor.impl;

import java.lang.reflect.Field;

import java.net.URL;

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.ConfigType;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.core.annotation.Priority;

import static org.demoiselle.jee.core.annotation.Priority.*;

/**
 * Adiciona a capacibilidade de extração de dados de uma fonte ({@link ConfigType}) para o tipo 
 * de {@link Class}.
 * 
 * <p>
 * Exemplo:
 * </p>
 * <p>
 * Para a extração de um Class de um arquivo properties a declaração feita no properties 
 * terá o seguinte formato:
 * </p>
 * 
 * <pre>
 * typedClass=package.MyClass
 * untypedClass=package.MyOtherClass
 * </pre>
 * 
 * E a classe de configuração será declara da seguinte forma:
 * 
 * <pre>
 *  
 * &#64;Configuration
 * public class BookmarkConfig {
 *
 *   private Class&#60;MyClass&#62; typedClass;
 *   private Class&#60;?&#62; untypedClass;
 *   
 *   public Class&#60;MyClass&#62; getTypedClass() {
 *     return typedClass;
 *   }
 *
 *   public Class&#60;?&#62; getUntypedClass() {
 *     return untypedClass;
 *   }
 *
 *}
 * 
 * </pre>
 * 
 */
@Dependent
@Priority(L2_PRIORITY)
public class ConfigurationClassValueExtractor implements ConfigurationValueExtractor {

	@Override
	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception {
		Object value = null;
		String canonicalName = configuration.getString(prefix + key);

		if (canonicalName != null) {
			value = forName(canonicalName);
		}

		return value;
	}

	@Override
	public boolean isSupported(Field field) {
		return field.getType() == Class.class;
	}
	
	@SuppressWarnings("unchecked")
	//TODO Export to a common class to treat the Reflection
	private <T> Class<T> forName(final String className) throws ClassNotFoundException {
		ClassLoader classLoader = getClassLoaderForClass(className);
		return (Class<T>) Class.forName(className, true, classLoader);
	}
	
	//TODO Export to a common class to treat the Reflection
	private ClassLoader getClassLoaderForClass(final String canonicalName) {
		return getClassLoaderForResource(canonicalName.replaceAll("\\.", "/") + ".class");
	}
	
	//TODO Export to a common class to treat the Reflection
	private ClassLoader getClassLoaderForResource(final String resource) {
		final String stripped = resource.charAt(0) == '/' ? resource.substring(1) : resource;

		URL url = null;
		ClassLoader result = Thread.currentThread().getContextClassLoader();

		if (result != null) {
			url = result.getResource(stripped);
		}

		if (url == null) {
			result =  getClass().getClassLoader();
			url = getClass().getClassLoader().getResource(stripped);
		}

		if (url == null) {
			result = null;
		}

		return result;
	}
}
