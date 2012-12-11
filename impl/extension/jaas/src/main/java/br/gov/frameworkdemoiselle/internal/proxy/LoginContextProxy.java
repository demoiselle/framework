///*
// * Demoiselle Framework
// * Copyright (C) 2010 SERPRO
// * ----------------------------------------------------------------------------
// * This file is part of Demoiselle Framework.
// * 
// * Demoiselle Framework is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public License version 3
// * as published by the Free Software Foundation.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public License version 3
// * along with this program; if not,  see <http://www.gnu.org/licenses/>
// * or write to the Free Software Foundation, Inc., 51 Franklin Street,
// * Fifth Floor, Boston, MA  02110-1301, USA.
// * ----------------------------------------------------------------------------
// * Este arquivo é parte do Framework Demoiselle.
// * 
// * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
// * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
// * do Software Livre (FSF).
// * 
// * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
// * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
// * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
// * para maiores detalhes.
// * 
// * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
// * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
// * ou escreva para a Fundação do Software Livre (FSF) Inc.,
// * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
// */
//package br.gov.frameworkdemoiselle.internal.proxy;
//
//import java.io.Serializable;
//
//import javax.enterprise.context.Dependent;
//import javax.security.auth.Subject;
//import javax.security.auth.callback.CallbackHandler;
//import javax.security.auth.login.LoginContext;
//import javax.security.auth.login.LoginException;
//
//import br.gov.frameworkdemoiselle.internal.configuration.JAASConfig;
//import br.gov.frameworkdemoiselle.security.SecurityException;
//import br.gov.frameworkdemoiselle.util.Beans;
//
////@Alternative
////@SessionScoped
//@Dependent
//public class LoginContextProxy extends LoginContext implements Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	private transient LoginContext delegate;
//
//	private transient CallbackHandler callbackHandler;
//
//	private String name;
//
//	// public LoginContextProxy() {
//	// super(name)
//	// }
//
//	public LoginContextProxy()  {
//		super("");
//	}
//
//	private String getName() {
//		if (this.name == null) {
//			this.name = Beans.getReference(JAASConfig.class).getLoginModuleName();
//		}
//
//		return this.name;
//	}
//
//	private LoginContext getDelegate() {
//		if (this.delegate == null) {
//			try {
//				this.delegate = new LoginContext(getName(), getCallbackHandler());
//
//			} catch (LoginException cause) {
//				throw new SecurityException(cause);
//			}
//		}
//
//		return this.delegate;
//	}
//
//	private CallbackHandler getCallbackHandler() {
//		if (this.callbackHandler == null) {
//			this.callbackHandler = Beans.getReference(CallbackHandler.class);
//		}
//
//		return this.callbackHandler;
//	}
//
//	public boolean equals(Object object) {
//		return getDelegate().equals(object);
//	}
//
//	public Subject getSubject() {
//		return getDelegate().getSubject();
//	}
//
//	public int hashCode() {
//		return getDelegate().hashCode();
//	}
//
//	public void login() throws LoginException {
//		getDelegate().login();
//	}
//
//	public void logout() throws LoginException {
//		getDelegate().logout();
//	}
//}
