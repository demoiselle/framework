package org.demoiselle.jee.configuration.extractor.impl;

import static org.demoiselle.jee.core.annotation.Priority.L2_PRIORITY;

import java.lang.reflect.Field;

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.ConfigType;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.core.annotation.Priority;

/**
 * Adiciona a capacibilidade de extração de dados de uma fonte ({@link ConfigType}) para o tipo 
 * de {@link String}.
 * 
 * <p>
 * Exemplo:
 * </p>
 * <p>
 * Para a extração de um String de um arquivo properties a declaração feita no properties 
 * terá o seguinte formato:
 * </p>
 * 
 * <pre>
 * demoiselle.ip=192.168.0.120 
 * </pre>
 * 
 * E a classe de configuração será declara da seguinte forma:
 * 
 * <pre>
 *  
 * &#64;Configuration
 * public class BookmarkConfig {
 *
 *  private String ip;
 *
 *  public String getIp() {
 *    return ip;
 *  }
 *
 * }
 * 
 * </pre>
 * 
 */
@Dependent
@Priority(L2_PRIORITY)
public class ConfigurationStringValueExtractor implements ConfigurationValueExtractor {

	@Override
	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception {
		return configuration.getString(prefix + key);
	}
	
	@Override
	public boolean isSupported(Field field) {
		return field.getType() == String.class;
	}
}
