/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor.impl;

import java.lang.reflect.Field;

import java.net.URL;

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.core.annotation.Priority;

import static org.demoiselle.jee.core.annotation.Priority.*;

/**
 * Adds the data extraction capability of a source ({@link ConfigurationType}) for the type of {@link Class}.
 * 
 * <p>
 * Sample:
 * </p>
 * 
 * <p>
 * For the extraction of a Class type of a properties file the statement made in the properties will have the following format:
 * </p>
 * 
 * <pre>
 * typedClass=package.MyClass
 * untypedClass=package.MyOtherClass
 * </pre>
 * 
 * And the configuration class will be declared as follows:
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
 * @author SERPRO
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
