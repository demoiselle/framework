package org.demoiselle.jee.configuration.extractor.impl;

import static org.demoiselle.jee.core.annotation.Priority.L2_PRIORITY;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.DataConfiguration;
import org.apache.commons.lang3.ClassUtils;
import org.demoiselle.jee.configuration.ConfigType;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.core.annotation.Priority;

/**
 * Adiciona a capacibilidade de extração de dados de uma fonte ({@link ConfigType}) para os tipos 
 * <ul>
 * 	<li>{@link Boolean}</li>
 *  <li>{@link Byte}</li>
 *  <li>{@link Character}</li>
 *  <li>{@link Short}</li>
 *  <li>{@link Integer}</li>
 *  <li>{@link Long}</li>
 *  <li>{@link Double}</li>
 *  <li>{@link Float}</li>
 *  <li>{@link Void}</li>
 * </ul>
 * 
 * <p>
 * Exemplo:
 * </p>
 * <p>
 * Para a extração de um int de um arquivo properties a declaração feita no properties 
 * terá o seguinte formato:
 * </p>
 * 
 * <pre>
 * demoiselle.pageSize = 10
 * </pre>
 * 
 * E a classe de configuração será declara da seguinte forma:
 * 
 * <pre>
 *  
 * &#64;Configuration
 * public class BookmarkConfig {
 *
 *  private int pageSize;
 *
 *  public String getPageSize() {
 *    return pageSize;
 *  }
 *
 * }
 * 
 * </pre>
 * 
 */
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

	@Override
	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception {
		return new DataConfiguration(configuration).get(ClassUtils.primitiveToWrapper(field.getType()), prefix + key);
	}

	@Override
	public boolean isSupported(Field field) {
		return field.getType().isPrimitive() || wrappers.contains(field.getType());
	}
}
