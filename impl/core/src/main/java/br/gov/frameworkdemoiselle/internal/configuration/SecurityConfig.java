package br.gov.frameworkdemoiselle.internal.configuration;

import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Authorizer;

/**
 * A <code>SecurityConfig</code> object is responsible for specifying which security configurations should be used for a
 * particular application.
 * 
 * @author SERPRO
 */
public interface SecurityConfig {

	/**
	 * Tells whether or not the security is enabled for the current application. This value could be defined in the
	 * <b>demoiselle.properties</b> file, using the key <i>frameworkdemoiselle.security.enabled</i>.
	 * 
	 * @return the value defined for the key <i>frameworkdemoiselle.security.enabled</i> in the
	 *         <b>demoiselle.properties</b> file. If there is no value defined, returns the default value <tt>true</tt>
	 */
	boolean isEnabled();

	void setEnabled(boolean enabled);

	Class<? extends Authenticator> getAuthenticatorClass();

	void setAuthenticatorClass(Class<? extends Authenticator> authenticatorClass);

	Class<? extends Authorizer> getAuthorizerClass();

	void setAuthorizerClass(Class<? extends Authorizer> authorizerClass);
}
