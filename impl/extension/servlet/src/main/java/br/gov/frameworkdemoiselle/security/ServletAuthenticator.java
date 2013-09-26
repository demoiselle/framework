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

import static br.gov.frameworkdemoiselle.annotation.Priority.L2_PRIORITY;

import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Implements the {@link Authenticator} interface, offering a way to implement offering a manner to use the
 * authenticator's functionalities.
 * 
 * @author SERPRO
 */

@Priority(L2_PRIORITY)
public class ServletAuthenticator implements Authenticator {

	private static final long serialVersionUID = 1L;

	private static ResourceBundle bundle;

	@Override
	public void authenticate() throws AuthenticationException {
		try {
			getRequest().login(getCredentials().getUsername(), getCredentials().getPassword());

		} catch (ServletException cause) {
			if (cause.getMessage().contains("invalid")) {
				throw new InvalidCredentialsException(getBundle().getString("invalid-credentials"));
			} else {
				throw new AuthenticationException(getBundle().getString("authentication-failed"), cause);
			}
		}
	}

	@Override
	public void unauthenticate() {
		getCredentials().clear();
		try {
			getRequest().logout();
		} catch (ServletException e) {
			// Logout já havia sido efetuado
		}
		getRequest().getSession().invalidate();
	}

	// TODO Criar uma delegação especializada de User ao invés de retornar
	// uma inner class
	@Override
	public User getUser() {
		final Principal principal = getRequest().getUserPrincipal();

		User user = null;

		if (principal != null) {
			user = new User() {

				private static final long serialVersionUID = 1L;

				@Override
				public String getId() {
					return principal.getName();
				}

				@Override
				public void setAttribute(Object key, Object value) {
				}

				@Override
				public Object getAttribute(Object key) {
					return null;
				}
			};
		}

		return user;
	}

	protected Credentials getCredentials() {
		return Beans.getReference(Credentials.class);
	}

	private HttpServletRequest getRequest() {
		return Beans.getReference(HttpServletRequest.class);
	}

	private static ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-servlet-bundle"));
		}

		return bundle;
	}
}
