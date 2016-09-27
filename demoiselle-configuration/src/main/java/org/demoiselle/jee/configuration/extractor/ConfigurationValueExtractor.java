package org.demoiselle.jee.configuration.extractor;

import java.lang.reflect.Field;

import org.apache.commons.configuration2.Configuration;

/**
 * <p>
 * Interface that defines how to convert values extracted from configuration
 * files to fields in a class annotated with {@link Configuration}.
 * </p>
 *
 * <p>
 * Primitive types like <code>int</code> and <code>float</code>, their wrapper
 * counterparts like {@link Integer} and {@link Float} and the {@link String} class
 * can already be converted by the framework, this interface is reserved for specialized
 * classes.
 * </p>
 * 
 * @author SERPRO
 */
public interface ConfigurationValueExtractor {

	/**
	 * Method that must appropriately extract the value from a property file and set this value to a 
	 * field in a configuration class.
	 * 
	 * @param prefix
	 * 			optional parte of property name that must be concatenated with <b>key</b> to form the whole 
	 * 			property name.
	 * @param key
	 * 			key of the property.
	 * @param field
	 * 			configuration field to be setted.
	 * @param configuration
	 * 			a configuration object.
	 * @return current value of this property
	 * @throws Exception if the value can't be extracted from the property file
	 */
	Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception;

	/**
	 * Checks if the extractor class is appropriate to extract values to the type of deffined by parameter
	 * <b>field</b>.
	 * 
	 * @param field
	 * 			field to be checked.
	 * @return <code>true</code> if this extractor can convert this field into the extractor's final type
	 */
	boolean isSupported(Field field);
}
