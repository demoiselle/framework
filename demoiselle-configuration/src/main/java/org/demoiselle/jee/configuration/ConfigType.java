package org.demoiselle.jee.configuration;

/**
 * Defines the types of sources that can be consumed.
 * 
 */
public enum ConfigType {
	/**
	 * Loaded settings {@link System#getProperties()} or {@link System#getenv()}.
	 */
	SYSTEM,
	
	/**
	 * Settings loaded from an XML file.
	 */
	XML,
	
	/**
	 * Settings loaded from a properties file.
	 */
	PROPERTIES
}
