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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Strings;

public class BasicAuthFilter extends AbstractHTTPAuthorizationFilter {

	private String header;

	@Override
	protected boolean isSupported(String authHeader) {
		header = authHeader;
		return !Strings.isEmpty(header);
	}

	@Override
	protected void prepareForLogin() {
		if (header != null) {
			String[] basicCredentials = getCredentials(header);

			Credentials credentials = Beans.getReference(Credentials.class);
			credentials.setUsername(basicCredentials[0]);
			credentials.setPassword(basicCredentials[1]);
		}
	}

	@Override
	protected void prepareForLogout() {
	}

	private static String[] getCredentials(String header)
			throws InvalidCredentialsException {
		String[] result = null;

		String regexp = "^Basic[ \\n]+(.+)$";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(header);

		if (matcher.matches()) {
			byte[] decoded = Base64.decodeBase64(matcher.group(1));
			result = new String(decoded).split(":");
		}

		if (result == null || result.length != 2) {
			throw new InvalidCredentialsException(
					"Formato inválido do cabeçalho");
		}

		return result;
	}

}