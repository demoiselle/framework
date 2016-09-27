package org.demoiselle.jee.configuration.extractor.impl;

import java.lang.reflect.Field;

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.core.annotation.Priority;

import static org.demoiselle.jee.core.annotation.Priority.*;

@Dependent
@Priority(L2_PRIORITY)
public class ConfigurationStringValueExtractor implements ConfigurationValueExtractor {

	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception {
		return configuration.getString(prefix + key);
	}

	public boolean isSupported(Field field) {
		return field.getType() == String.class;
	}
}
