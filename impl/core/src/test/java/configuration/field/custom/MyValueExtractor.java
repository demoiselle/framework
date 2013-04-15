package configuration.field.custom;

import java.lang.reflect.Field;

import org.apache.commons.configuration.Configuration;

import br.gov.frameworkdemoiselle.configuration.ConfigurationValueExtractor;

public class MyValueExtractor implements ConfigurationValueExtractor {

	@Override
	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception {
		return new MappedClass();
	}

	@Override
	public boolean isSupported(Field field) {
		return field.getType() == MappedClass.class;
	}
}
