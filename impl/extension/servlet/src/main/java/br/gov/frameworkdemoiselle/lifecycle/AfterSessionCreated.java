package br.gov.frameworkdemoiselle.lifecycle;

/**
 * This interface represents an event fired before a new HTTP session is created.
 * 
 * @author serpro
 *
 */
public interface AfterSessionCreated {
	
	public String getSessionId();

}
