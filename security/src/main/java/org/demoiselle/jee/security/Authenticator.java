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
package org.demoiselle.jee.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * <p>
 * Defines the methods that should be implemented by anyone who wants an authentication mechanism.
 * </p>
 *
 * @author SERPRO
 */
public interface Authenticator extends Serializable {

	/**
	 * <p>
	 * Executes the necessary steps to authenticate an user. After this call, {@link #getUser()} must return
	 * the currently authenticated user, or <code>null</code> if the authentication process fails.
	 * </p>
	 *
	 * @throws InvalidCredentialsException
	 * 				You should throw this exception when the informed credentials are invalid.
	 *
	 * @throws Exception
	 * 				If the underlying authentication mechanism throwns any other exception,
	 *             just throw it and leave the security context implementation to handle it.
	 */
	void authenticate() throws Exception;

	/**
	 * <p>
	 * Executes the necessary steps to unauthenticate an user. After this call, {@link #getUser()} must return <code>null</code>.
	 * </p>
	 *
	 * @throws Exception
	 *             If the underlying authentication mechanism throwns any other exception,
	 *             just throw it and leave the security context implementation to handle it.
	 */
	void unauthenticate() throws Exception;

	/**
	 * <p>
	 * Returns the currently authenticated user.
	 * </p>
	 *
	 * @return the user currently authenticated, or <code>null</code> if there is no
	 * authenticated user.
	 * 
	 * @see #authenticate()
	 * @see #unauthenticate()
	 */
	Principal getUser();
}
