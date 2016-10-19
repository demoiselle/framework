package org.demoiselle.jee.configuration.extractor.impl;

import java.lang.reflect.Field;

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConversionException;//
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.configuration.message.ConfigurationMessage;
import org.demoiselle.jee.core.annotation.Priority;
import static org.demoiselle.jee.core.annotation.Priority.*;

@Dependent
@Priority(L2_PRIORITY)
public class ConfigurationEnumValueExtractor implements ConfigurationValueExtractor {

	private transient ConfigurationMessage bundle;

	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception {
		String value = configuration.getString(prefix + key);

		if (value != null && !value.trim().equals("")) {
			Object enums[] = field.getType().getEnumConstants();

			for (int i = 0; i < enums.length; i++) {
				if (((Enum<?>) enums[i]).name().equals(value)) {
					return enums[i];
				}
			}
		} 
		
		throw new ConversionException(bundle.configurationNotConversion(value, field.getDeclaringClass().getCanonicalName()));
		
		
	}

	public boolean isSupported(Field field) {
		return field.getType().isEnum();
	}

}
