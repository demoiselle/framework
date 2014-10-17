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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.gov.frameworkdemoiselle.util.Beans;

public class TokenAuthFilter extends AbstractHTTPAuthorizationFilter {

	@Override
	protected String getType() {
		return "Token";
	}

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String authData = getAuthData(request);

		super.doFilter(request, response, chain);

		String value = Beans.getReference(Token.class).getValue();
		if (value != null && !value.equals(authData)) {
			response.setHeader("Set-Token", value);
		}
	}

	@Override
	protected boolean isActive() {
		return Beans.getReference(RESTSecurityConfig.class).isTokenFilterActive();
	}

	@Override
	protected void performLogin(HttpServletRequest request, HttpServletResponse response) {
		Token token = Beans.getReference(Token.class);
		String authData = getAuthData(request);
		token.setValue(authData);

		super.performLogin(request, response);
	}

	// public void setHeader(@Observes AfterLoginSuccessful event) {
	// Token token = Beans.getReference(Token.class);
	//
	// if (!token.isEmpty()) {
	// HttpServletResponse response = Beans.getReference(HttpServletResponse.class);
	// response.setHeader("Set-Token", token.getValue());
	// }
	// }
}
