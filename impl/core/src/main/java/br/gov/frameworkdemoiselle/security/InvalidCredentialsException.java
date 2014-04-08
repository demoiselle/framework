package br.gov.frameworkdemoiselle.security;

/**
 * Thrown when the user's credentials are invalid.
 * 
 * @author SERPRO
 */
public class InvalidCredentialsException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an <code>InvalidCredentialsException</code> with a message.
	 */
	public InvalidCredentialsException(String message) {
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
	public InvalidCredentialsException(String message, Throwable cause) {
		super(message, cause);
	}
}
