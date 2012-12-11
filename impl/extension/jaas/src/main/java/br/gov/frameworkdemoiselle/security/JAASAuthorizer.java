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

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;

import javax.security.auth.login.LoginContext;

import br.gov.frameworkdemoiselle.internal.producer.LoginContextFactory;

public class JAASAuthorizer implements Authorizer {

	private static final long serialVersionUID = 1L;

	private transient LoginContext loginContext;

	@Override
	public boolean hasRole(String role) {
		boolean result = false;

		Group group;
		Principal member;
		Enumeration<? extends Principal> enumeration;

		for (Principal principal : getLoginContext().getSubject().getPrincipals()) {

			if (principal instanceof Group) {
				group = (Group) principal;
				enumeration = group.members();

				while (enumeration.hasMoreElements()) {
					member = (Principal) enumeration.nextElement();

					System.out.println("xxxxxx: " + member.getName());
					
					if (member.getName().equals(role)) {
						result = true;
						break;
					}
				}
			}
		}

		return result;
	}

	public LoginContext getLoginContext() {
		if (this.loginContext == null) {
			this.loginContext = LoginContextFactory.createLoginContext();
		}

		return this.loginContext;
	}

	@Override
	public boolean hasPermission(String resource, String operation) {
		return true;
	}
}
