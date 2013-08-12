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
package br.gov.frameworkdemoiselle.internal.implementation;

import java.io.Serializable;
import java.security.Principal;

import javax.inject.Named;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.configuration.SecurityConfig;
import br.gov.frameworkdemoiselle.security.AfterLoginSuccessful;
import br.gov.frameworkdemoiselle.security.AfterLogoutSuccessful;
import br.gov.frameworkdemoiselle.security.AuthenticationException;
import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Authorizer;
import br.gov.frameworkdemoiselle.security.NotLoggedInException;
import br.gov.frameworkdemoiselle.security.SecurityContext;
import br.gov.frameworkdemoiselle.security.User;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * This is the default implementation of {@link SecurityContext} interface.
 * 
 * @author SERPRO
 */
@SuppressWarnings("deprecation")
@Named("securityContext")
public class SecurityContextImpl implements SecurityContext {

	private static final long serialVersionUID = 1L;

	private transient static ResourceBundle bundle;

	private Authenticator authenticator;

	private Authorizer authorizer;

	private Authenticator getAuthenticator() {
		if (this.authenticator == null) {
			Class<? extends Authenticator> clazz = getConfig().getAuthenticatorClass();

			if (clazz == null) {
				clazz = StrategySelector.selectClass(Authenticator.class);
			}

			this.authenticator = Beans.getReference(clazz);
		}

		return this.authenticator;
	}

	private Authorizer getAuthorizer() {
		if (this.authorizer == null) {
			Class<? extends Authorizer> clazz = getConfig().getAuthorizerClass();

			if (clazz == null) {
				clazz = StrategySelector.selectClass(Authorizer.class);
			}

			this.authorizer = Beans.getReference(clazz);
		}

		return this.authorizer;
	}

	/**
	 * @see br.gov.frameworkdemoiselle.security.SecurityContext#hasPermission(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean hasPermission(String resource, String operation) throws NotLoggedInException {
		if (getConfig().isEnabled()) {
			checkLoggedIn();
			return getAuthorizer().hasPermission(resource, operation);

		} else {
			return true;
		}
	}

	/**
	 * @see br.gov.frameworkdemoiselle.security.SecurityContext#hasRole(java.lang.String)
	 */
	@Override
	public boolean hasRole(String role) throws NotLoggedInException {
		boolean result = true;

		if (getConfig().isEnabled()) {
			checkLoggedIn();
			result = getAuthorizer().hasRole(role);
		}

		return result;
	}

	/**
	 * @see br.gov.frameworkdemoiselle.security.SecurityContext#isLoggedIn()
	 */
	@Override
	public boolean isLoggedIn() {
		boolean result = true;

		if (getConfig().isEnabled()) {
			result = getCurrentUser() != null;
		}

		return result;
	}

	/**
	 * @see br.gov.frameworkdemoiselle.security.SecurityContext#login()
	 */
	@Override
	public void login() throws AuthenticationException {
		if (getConfig().isEnabled()) {
			getAuthenticator().authenticate();

			Beans.getBeanManager().fireEvent(new AfterLoginSuccessful() {

				private static final long serialVersionUID = 1L;

			});
		}
	}

	/**
	 * @see br.gov.frameworkdemoiselle.security.SecurityContext#logout()
	 */
	@Override
	public void logout() throws NotLoggedInException {
		if (getConfig().isEnabled()) {
			checkLoggedIn();
			getAuthenticator().unAuthenticate();

			Beans.getBeanManager().fireEvent(new AfterLogoutSuccessful() {

				private static final long serialVersionUID = 1L;
			});
		}
	}

	/**
	 * @deprecated Use {@link #getCurrentUser()} instead.
	 * @see br.gov.frameworkdemoiselle.security.SecurityContext#getUser()
	 */
	@Override
	public User getUser() {
		throw new DemoiselleException("Utilize o método getCurrentUser() ao invés do getUser()");
	}

	@Override
	public Principal getCurrentUser() {
		Principal user = getAuthenticator().getUser();

		if (!getConfig().isEnabled() && user == null) {
			user = new EmptyUser();
		}

		return user;
	}

	private SecurityConfig getConfig() {
		return Beans.getReference(SecurityConfig.class);
	}

	public void checkLoggedIn() throws NotLoggedInException {
		if (!isLoggedIn()) {
			throw new NotLoggedInException(getBundle().getString("user-not-authenticated"));
		}
	}

	private static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle"));
		}

		return bundle;
	}

	private static class EmptyUser implements Principal, Serializable {

		private static final long serialVersionUID = 1L;

		@Override
		public String getName() {
			return "demoiselle";
		}
	}
}
