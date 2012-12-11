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

import java.security.Principal;

import javax.inject.Inject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import br.gov.frameworkdemoiselle.internal.producer.LoginContextFactory;

//@SessionScoped
public class JAASAuthenticator implements Authenticator {

	private static final long serialVersionUID = 1L;

	private transient LoginContext loginContext;

	private User user;

	@Inject
	private Credentials credentials;

	@Override
	public boolean authenticate() {
		boolean result = false;

		try {
			getLoginContext().login();
			getLoginContext().getSubject().getPrincipals().add(new Principal() {

				@Override
				public String getName() {
					return credentials.getUsername();
				}
			});

			this.credentials.clear();
			result = true;

		} catch (LoginException cause) {
			result = false;
		}

		return result;
	}

	@Override
	public void unAuthenticate() {
		try {
			getLoginContext().logout();
			user = null;

		} catch (LoginException cause) {
			cause.printStackTrace();
		}
	}

	@Override
	public User getUser() {
		if (this.user == null && getLoginContext().getSubject() != null
				&& !getLoginContext().getSubject().getPrincipals().isEmpty()) {
			this.user = new User() {

				private static final long serialVersionUID = 1L;

				@Override
				public String getId() {
					return getLoginContext().getSubject().getPrincipals().iterator().next().getName();
				}

				@Override
				public Object getAttribute(Object key) {
					return null;
				}

				@Override
				public void setAttribute(Object key, Object value) {
				}
			};
		}

		return this.user;
	}

	public LoginContext getLoginContext() {
		if (this.loginContext == null) {
			this.loginContext = LoginContextFactory.createLoginContext();
		}

		return this.loginContext;
	}

	//
	// protected LoginContext createLoginContext() {
	// LoginContext result = null;
	//
	// try {
	// result = new LoginContext(this.config.getLoginModuleName(), createCallbackHandler());
	//
	// } catch (LoginException cause) {
	// throw new SecurityException(cause);
	// }
	//
	// return result;
	// }

	// protected CallbackHandler createCallbackHandler() {
	// return new CallbackHandler() {
	//
	// public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
	// for (int i = 0; i < callbacks.length; i++) {
	// if (callbacks[i] instanceof NameCallback) {
	// ((NameCallback) callbacks[i]).setName(credentials.getUsername());
	//
	// } else if (callbacks[i] instanceof PasswordCallback) {
	// ((PasswordCallback) callbacks[i]).setPassword(credentials.getPassword().toCharArray());
	//
	// } else {
	// System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXX Unsupported callback " + callbacks[i]);
	// }
	// }
	// }
	// };
	// }
}
