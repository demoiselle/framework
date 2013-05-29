/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.security;

import static br.gov.frameworkdemoiselle.internal.implementation.StrategySelector.EXTENSIONS_L1_PRIORITY;

import java.io.IOException;
import java.security.Principal;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.internal.configuration.JAASConfig;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.ResourceBundle;
import br.gov.frameworkdemoiselle.util.Strings;

@SessionScoped
@Priority(EXTENSIONS_L1_PRIORITY)
public class JAASAuthenticator implements Authenticator {

	private static final long serialVersionUID = 1L;

	private static ResourceBundle bundle;

	private static Logger logger;

	private Principal user;

	private final Subject subject;

	@Inject
	private JAASConfig config;

	@Inject
	private Credentials credentials;

	public JAASAuthenticator() {
		this.subject = new Subject();
	}

	@Override
	public void authenticate() throws AuthenticationException {
		try {
			LoginContext loginContext = createLoginContext();

			if (loginContext != null) {
				loginContext.login();

				this.user = createUser(this.credentials.getUsername());
				this.credentials.clear();
			}

		} catch (LoginException cause) {
			throw new AuthenticationException(cause);
		}
	}

	@Override
	public void unAuthenticate() {
		this.user = null;
	}

	private Principal createUser(final String username) {
		return new Principal() {

			// TODO Tornar esta classe serializável
			// private static final long serialVersionUID = 1L;

			@Override
			public String getName() {
				return username;
			}
		};
	}

	@Override
	public Principal getUser() {
		try {

			// LoginContext

			// AbstractSecurityContext.

			// Object securityContext = System.getSecurityManager().getSecurityContext();

			// System.out.println(securityContext.toString());

			String name = config.getLoginModuleName();
			LoginContext loginContext = new LoginContext(name, this.subject);
			loginContext.login();

			Subject subject2 = loginContext.getSubject();
			
			//subject2.get

			//System.out.println(subject2.toString());

		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return this.user;
	}

	@Produces
	public Subject getSubject() {
		return this.subject;
	}

	public LoginContext createLoginContext() throws LoginException {
		String name = config.getLoginModuleName();

		if (Strings.isEmpty(name)) {
			throw new SecurityException(getBundle().getString("required-login-module-name"));
		}

		return new LoginContext(name, this.subject, createCallbackHandler());
	}

	private CallbackHandler createCallbackHandler() {
		return new CallbackHandler() {

			public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
				for (int i = 0; i < callbacks.length; i++) {
					if (callbacks[i] instanceof NameCallback) {
						((NameCallback) callbacks[i]).setName(credentials.getUsername());

					} else if (callbacks[i] instanceof PasswordCallback) {
						((PasswordCallback) callbacks[i]).setPassword(credentials.getPassword().toCharArray());
						
					} else {
						getLogger().error(getBundle().getString("unsupported-callback", callbacks[i]));
					}
				}
			}
		};
	}

	private static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = ResourceBundleProducer.create("demoiselle-jaas-bundle");
		}

		return bundle;
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = LoggerProducer.create(JAASAuthenticator.class);
		}

		return logger;
	}
}
