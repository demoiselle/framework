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

import java.io.Serializable;


/**
 * Structure used to handle both authentication and authorizations mechanisms.
 * 
 * @author SERPRO
 */
public interface SecurityContext extends Serializable {

	/**
	 * Executes the login of a user to the application.
	 */
	void login();

	/**
	 * Executes the logout of a user.
	 * 
	 * @throws NotLoggedInException
	 *             if there is no user logged in a specific session
	 */
	void logout() throws NotLoggedInException;

	/**
	 * Checks if a specific user is logged in.
	 * 
	 * @return {@code true} if the user is logged in
	 */
	boolean isLoggedIn();

	/**
	 * Checks if the logged user has permission to execute an specific operation on a specific resource.
	 * 
	 * @param resource
	 *            resource to be checked
	 * @param operation
	 *            operation to be checked
	 * @return {@code true} if the user has the permission
	 * @throws NotLoggedInException
	 *             if there is no user logged in a specific session.
	 */
	boolean hasPermission(String resource, String operation) throws NotLoggedInException;

	/**
	 * Checks if the logged user has an specific role
	 * 
	 * @param role
	 *            role to be checked
	 * @return {@code true} if the user has the role
	 * @throws NotLoggedInException
	 *             if there is no user logged in a specific session.
	 */
	boolean hasRole(String role) throws NotLoggedInException;

	/**
	 * Return the user logged in the session.
	 * 
	 * @return the user logged in a specific session. If there is no active session returns {@code null}
	 */
	User getUser();
}
