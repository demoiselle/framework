package br.gov.frameworkdemoiselle.lifecycle;

/**
 * This interface represents an event fired after a new HTTP session is created.
 * 
 * @author serpro
 *
 */
public interface AfterSessionCreated {
	
	/**
	 * 
	 * @return The ID of the recently created session
	 */
	public String getSessionId();

}
