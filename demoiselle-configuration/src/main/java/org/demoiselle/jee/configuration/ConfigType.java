package org.demoiselle.jee.configuration;

/**
 * Defines configuration types to be loaded.
 * 
 * @author SERPRO
 */
public enum ConfigType {

	/**
	 * Configuration loaded on {@link System#getProperties()} or {@link System#getenv()}.
	 */
	SYSTEM,

	/**
	 * Configuration loaded on XML resources.
	 */
	XML,

	/**
	 * Configuration loaded on properties resources.
	 */
	PROPERTIES

}
