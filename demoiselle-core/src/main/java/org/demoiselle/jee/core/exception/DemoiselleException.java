/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.exception;

/**
 * Exception class intended to be used by framework configuration and to be derived by other framework exceptions.
 * 
 * @author SERPRO
 */
public class DemoiselleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String errorCode;

	/**
	 * Constructor .
	 * 
	 */
	public DemoiselleException() {
		this.errorCode = null;
	}
	
	/**
	 * Constructor with message.
	 * 
	 * @param message
	 *            exception message
	 */
	public DemoiselleException(String message) {
		super(message);
		this.errorCode = null;
	}

	/**
	 * Constructor with cause.
	 * 
	 * @param cause
	 *            exception cause
	 */
	public DemoiselleException(Throwable cause) {
		super(cause);
		this.errorCode = null;
	}

	/**
	 * Constructor with message and cause.
	 * 
	 * @param message
	 *            exception message
	 * @param cause
	 *            exception cause
	 */
	public DemoiselleException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = null;
	}

	/**
	 * Constructor with message and error code.
	 * 
	 * @param message
	 *            exception message
	 * @param errorCode
	 *            structured error code in format DEMOISELLE-&lt;MODULE&gt;-&lt;NUMBER&gt;
	 */
	public DemoiselleException(String message, String errorCode) {
		super(message);
		validateErrorCode(errorCode);
		this.errorCode = errorCode;
	}

	/**
	 * Constructor with message, error code and cause.
	 * 
	 * @param message
	 *            exception message
	 * @param errorCode
	 *            structured error code in format DEMOISELLE-&lt;MODULE&gt;-&lt;NUMBER&gt;
	 * @param cause
	 *            exception cause
	 */
	public DemoiselleException(String message, String errorCode, Throwable cause) {
		super(message, cause);
		validateErrorCode(errorCode);
		this.errorCode = errorCode;
	}

	/**
	 * Returns the structured error code, or {@code null} if none was set.
	 * 
	 * @return the error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Validates the error code format.
	 * 
	 * @param errorCode the error code to validate
	 * @throws IllegalArgumentException if the error code is non-null and does not match the expected format
	 */
	private void validateErrorCode(String errorCode) {
		if (errorCode != null && !errorCode.matches("^DEMOISELLE-[A-Z]{2,4}-\\d{3}$")) {
			throw new IllegalArgumentException(
				"Invalid errorCode format: '" + errorCode + "'. Expected format: DEMOISELLE-<MODULE>-<NUMBER> (e.g., DEMOISELLE-SEC-001)");
		}
	}

	@Override
	public String toString() {
		if (errorCode != null) {
			return super.toString() + " [errorCode=" + errorCode + "]";
		}
		return super.toString();
	}
}
