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
import br.gov.frameworkdemoiselle.internal.implementation.DefaultAuthenticator;
import br.gov.frameworkdemoiselle.internal.implementation.DefaultAuthorizer;
import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Authorizer;

/**
 * A <code>SecurityConfig</code> object is responsible for specifying which security configurations should be used for a
 * particular application.
 * 
 * @author SERPRO
 */
@Configuration(prefix = "frameworkdemoiselle.security")
public class SecurityConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;

	private Class<? extends Authenticator> authenticatorClass = DefaultAuthenticator.class;

	private Class<? extends Authorizer> authorizerClass = DefaultAuthorizer.class;

	/**
	 * Tells whether or not the security is enabled for the current application. This value could be defined in the
	 * <b>demoiselle.properties</b> file, using the key <i>frameworkdemoiselle.security.enabled</i>.
	 * 
	 * @return the value defined for the key <i>frameworkdemoiselle.security.enabled</i> in the
	 *         <b>demoiselle.properties</b> file. If there is no value defined, returns the default value <tt>true</tt>
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public Class<? extends Authenticator> getAuthenticatorClass() {
		return authenticatorClass;
	}

	public Class<? extends Authorizer> getAuthorizerClass() {
		return authorizerClass;
	}
}
