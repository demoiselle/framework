package br.gov.frameworkdemoiselle.lifecycle;

/**
 * This interface represents an event fired after a new HTTP session is destroyed.
 * 
 * @author serpro
 *
 */
public interface BeforeSessionDestroyed {
	
	public String getSessionId();

}
