package org.demoiselle.jee.configuration;

import org.demoiselle.jee.core.exception.DemoiselleException;

/**
 * Exception class intended to be used by configuration components.
 * 
 * @author SERPRO
 */
public class ConfigurationException extends DemoiselleException{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor with message.
	 * 
	 * @param message
	 *            exception message
	 */
	public ConfigurationException(String message) {
		super(message);
	}

	/**
	 * Constructor with message and cause.
	 * 
	 * @param message
	 *            exception message
	 * @param cause
	 *            exception cause
	 */
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
