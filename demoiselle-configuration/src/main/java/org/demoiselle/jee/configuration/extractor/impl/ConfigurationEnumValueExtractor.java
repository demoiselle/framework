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
 * de {@link Enum}.
 * 
 * <p>
 * Exemplo:
 * </p>
 * <p>
 * Para a extração de um Enum de um arquivo properties a declaração feita no properties 
 * terá o seguinte formato:
 * </p>
 * 
 * <pre>
 * demoiselle.loadedConfigurationType=SYSTEM
 * </pre>
 * 
 * E a classe de configuração será declara da seguinte forma:
 * 
 * <pre>
 *  
 * &#64;Configuration
 * public class BookmarkConfig {
 *
 *  private {@link ConfigType} loadedConfigurationType;
 *
 *  public ConfigType getLoadedConfigurationType(){
 *    return loadedConfigurationType;
 *  }
 *
 * }
 * 
 * </pre>
 * 
 */
@Dependent
@Priority(L2_PRIORITY)
public class ConfigurationEnumValueExtractor implements ConfigurationValueExtractor {

	@Override
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
		
		return null;
		
	}

	@Override
	public boolean isSupported(Field field) {
		return field.getType().isEnum();
	}

}
