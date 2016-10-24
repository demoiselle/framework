package org.demoiselle.jee.configuration.extractor.impl;

import java.lang.reflect.Field;

import javax.enterprise.context.Dependent;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.DataConfiguration;
import org.demoiselle.jee.configuration.ConfigType;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.core.annotation.Priority;
import static org.demoiselle.jee.core.annotation.Priority.*;

/**
 * Adiciona a capacibilidade de extração de dados de uma fonte ({@link ConfigType}) para o tipo 
 * de array de objetos {@code Object[]}.
 * 
 * <p>
 * Exemplo:
 * </p>
 * <p>
 * Para a extração de um array de inteiros de um arquivo properties a declaração feita no properties 
 * terá o seguinte formato:
 * </p>
 * 
 * <pre>
 * demoiselle.intergerArray=-1
 * demoiselle.intergerArray=0
 * demoiselle.intergerArray=1
 * </pre>
 * 
 * E a classe de configuração será declara da seguinte forma:
 * 
 * <pre>
 *  
 * &#64;Configuration
 * public class MyConfig {
 *  private Integer[] integerArray;
 * 
 *  public Integer[] getIntegerArray() {
 *    return this.integerArray;
 *  }
 *  
 * }
 * 
 * </pre>
 * 
 */
@Dependent
@Priority(L2_PRIORITY)
public class ConfigurationArrayValueExtractor implements ConfigurationValueExtractor {

	@Override
	public Object getValue(String prefix, String key, Field field, Configuration configuration) throws Exception {
		return new DataConfiguration(configuration).getArray(field.getType().getComponentType(), prefix + key);
	}

	@Override
	public boolean isSupported(Field field) {
		return field.getType().isArray();
	}
}
