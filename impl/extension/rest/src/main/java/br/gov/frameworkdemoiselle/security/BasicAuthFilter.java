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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import br.gov.frameworkdemoiselle.util.Beans;

public class BasicAuthFilter extends AbstractHTTPAuthorizationFilter {

	@Override
	protected String getType() {
		return "Basic";
	}

	@Override
	protected void performLogin(HttpServletRequest request, HttpServletResponse response) {
		String[] decoded = decodeCredentials(request);

		Credentials credentials = Beans.getReference(Credentials.class);
		credentials.setUsername(decoded[0]);
		credentials.setPassword(decoded[1]);

		super.performLogin(request, response);
	}

	private String[] decodeCredentials(HttpServletRequest request) throws InvalidCredentialsException {
		String[] result = null;

		String authData = getAuthData(request);
		byte[] decoded = Base64.decodeBase64(authData);
		result = new String(decoded).split(":");

		if (result == null || result.length != 2) {
			throw new InvalidCredentialsException("formato inválido do cabeçalho");
		}

		return result;
	}

	@Override
	protected boolean isActive() {
		return Beans.getReference(RESTSecurityConfig.class).isBasicFilterActive();
	}
}
