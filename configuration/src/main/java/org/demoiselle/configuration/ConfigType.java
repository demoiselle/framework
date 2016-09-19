package org.demoiselle.configuration;

/**
 * Defines configuration types to be loaded.
 * 
 * @author SERPRO
 */
@SuppressWarnings("WeakerAccess")
public enum ConfigType {

	/**
	 * Configuration loaded on {@link System#getProperties()} or {@link System#getenv()}.
	 */
	@SuppressWarnings("unused") SYSTEM,

	/**
	 * Configuration loaded on XML resources.
	 */
	@SuppressWarnings("unused") XML,

	/**
	 * Configuration loaded on properties resources.
	 */
	@SuppressWarnings("unused") PROPERTIES

}
