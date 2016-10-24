package org.demoiselle.jee.configuration;

/**
 * Define os tipos de fontes que podem ser consumidos.
 * 
 */
public enum ConfigType {
	/**
	 * Configurações carregadas do método {@link System#getProperties()} ou {@link System#getenv()}.
	 */
	SYSTEM,
	
	/**
	 * Configurações carregadas de um arquivo XML.
	 */
	XML,
	
	/**
	 * Configurações carregadas de um arquivo properties.
	 */
	PROPERTIES
}
