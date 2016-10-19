package org.demoiselle.jee.configuration.extractor.impl;

import static org.demoiselle.jee.core.annotation.Priority.L2_PRIORITY;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.DataConfiguration;
import org.apache.commons.lang3.ClassUtils;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.core.annotation.Priority;

@Dependent
@Priority(L2_PRIORITY)
public class ConfigurationPrimitiveOrWrapperValueExtractor implements ConfigurationValueExtractor {

	private static final Set<Object> wrappers = new HashSet<Object>();

	static {
		wrappers.add(Boolean.class);
		wrappers.add(Byte.class);
		wrappers.add(Character.class);
		wrappers.add(Short.class);
		wrappers.add(Integer.class);
		wrappers.add(Long.class);
		wrappers.add(Double.class);
		wrappers.add(Float.class);
		wrappers.add(Void.TYPE);
	}

	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception {
		return new DataConfiguration(configuration).get(ClassUtils.primitiveToWrapper(field.getType()), prefix + key);
	}

	public boolean isSupported(Field field) {
		return field.getType().isPrimitive() || wrappers.contains(field.getType());
	}
}
