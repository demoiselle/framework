package org.demoiselle.jee.configuration.extractor;

import java.lang.reflect.Field;

import org.apache.commons.configuration2.Configuration;

public interface ConfigurationValueExtractor {
	Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception;
	boolean isSupported(Field field);
}
