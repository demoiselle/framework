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
package br.gov.frameworkdemoiselle.internal.configuration;

import java.io.Serializable;

import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Authorizer;
import br.gov.frameworkdemoiselle.security.SecurityConfig;

@Configuration(prefix = "frameworkdemoiselle.security")
public class SecurityConfigImpl implements Serializable, SecurityConfig {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;

	private Class<? extends Authenticator> authenticatorClass;

	private Class<? extends Authorizer> authorizerClass;

	/*
	 * (non-Javadoc)
	 * @see br.gov.frameworkdemoiselle.security.SecurityConfig#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	/*
	 * (non-Javadoc)
	 * @see br.gov.frameworkdemoiselle.security.SecurityConfig#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * @see br.gov.frameworkdemoiselle.security.SecurityConfig#getAuthenticatorClass()
	 */
	@Override
	public Class<? extends Authenticator> getAuthenticatorClass() {
		return this.authenticatorClass;
	}

	/*
	 * (non-Javadoc)
	 * @see br.gov.frameworkdemoiselle.security.SecurityConfig#setAuthenticatorClass(java.lang.Class)
	 */
	@Override
	public void setAuthenticatorClass(Class<? extends Authenticator> authenticatorClass) {
		this.authenticatorClass = authenticatorClass;
	}

	/*
	 * (non-Javadoc)
	 * @see br.gov.frameworkdemoiselle.security.SecurityConfig#getAuthorizerClass()
	 */
	@Override
	public Class<? extends Authorizer> getAuthorizerClass() {
		return this.authorizerClass;
	}

	/*
	 * (non-Javadoc)
	 * @see br.gov.frameworkdemoiselle.security.SecurityConfig#setAuthorizerClass(java.lang.Class)
	 */
	@Override
	public void setAuthorizerClass(Class<? extends Authorizer> authorizerClass) {
		this.authorizerClass = authorizerClass;
	}
}
