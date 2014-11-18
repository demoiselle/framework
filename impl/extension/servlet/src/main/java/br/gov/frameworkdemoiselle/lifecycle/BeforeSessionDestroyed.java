package br.gov.frameworkdemoiselle.lifecycle;

/**
 * This interface represents an event fired before an HTTP session is destroyed.
 * 
 * @author serpro
 *
 */
public interface BeforeSessionDestroyed {
	
	/**
	 * 
	 * @return The session ID of the session about to be destroyed
	 */
	public String getSessionId();

}
