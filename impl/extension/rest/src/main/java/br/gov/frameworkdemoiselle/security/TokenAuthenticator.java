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

import static br.gov.frameworkdemoiselle.annotation.Priority.L3_PRIORITY;

import java.security.Principal;

import javax.enterprise.context.RequestScoped;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.StrategyQualifier;

@RequestScoped
@Priority(L3_PRIORITY)
public class TokenAuthenticator implements Authenticator {

	private static final long serialVersionUID = 1L;

	private Principal user;

	@Override
	public void authenticate() throws Exception {
		Token token = Beans.getReference(Token.class);
		TokenManager tokenManager = Beans.getReference(TokenManager.class, new StrategyQualifier());

		if (token.isEmpty()) {
			this.user = customAuthentication();

			String newToken = tokenManager.persist(this.user);
			token.setValue(newToken);

		} else {
			this.user = tokenAuthentication(token, tokenManager);
		}
	}

	protected Principal customAuthentication() throws Exception {
		ServletAuthenticator authenticator = Beans.getReference(ServletAuthenticator.class);
		authenticator.authenticate();

		return authenticator.getUser();
	}

	private Principal tokenAuthentication(Token token, TokenManager tokenManager) throws Exception {
		Principal principal = tokenManager.load(token.getValue());

		if (principal == null) {
			throw new InvalidCredentialsException("token inválido");
		}

		return principal;
	}

	@Override
	// TODO Apagar o token
	public void unauthenticate() {
		this.user = null;
	}

	@Override
	public Principal getUser() {
		return this.user;
	}
}
