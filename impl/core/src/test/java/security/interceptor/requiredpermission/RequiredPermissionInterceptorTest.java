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
package security.interceptor.requiredpermission;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import security.interceptor.loggedin.CustomAuthenticator;
import test.Tests;
import br.gov.frameworkdemoiselle.context.SessionContext;
import br.gov.frameworkdemoiselle.security.AuthorizationException;
import br.gov.frameworkdemoiselle.security.SecurityContext;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class RequiredPermissionInterceptorTest {

	@Inject
	private DummyProtectedClassAuthorized protectedClassAuthorized;
	
	@Inject
	private DummyProtectedClassUnauthorized protectedClassUnAuthorized;

	@Inject
	private DummyProtectedMethods protectedMethods;
	
	@Inject
	private DummyProtectedClassAndMethod protectedClassAndMethod;

	@Inject
	private SecurityContext securityContext;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment();
		deployment.addClass(DummyProtectedClassAuthorized.class);
		deployment.addClass(DummyProtectedClassUnauthorized.class);
		deployment.addClass(DummyProtectedMethods.class);
		deployment.addClass(DummyProtectedClassAndMethod.class);
		deployment.addClass(CustomAuthenticator.class);
		deployment.addClass(CustomAuthorizer.class);
		return deployment;
	}

	@Before
	public void activeContext() {
		SessionContext sessionContext = Beans.getReference(SessionContext.class);
		sessionContext.activate();
		
		securityContext.login();
	}

	@Test(expected=AuthorizationException.class)
	public void callProtectedClassAttribNotAuthorized() {
		protectedClassUnAuthorized.getDummyAttrib();
	}

	@Test
	public void callProtectedClassAttribAuthorized() {
		protectedClassAuthorized.setDummyAttrib("Test");
		assertEquals("Test", protectedClassAuthorized.getDummyAttrib());
	}

	@Test(expected=AuthorizationException.class)
	public void callProtectedMethodNotAuthorized(){
		protectedMethods.setDummyAttribUnauthorized("Not Authorized");
	}
	
	@Test
	public void callProtectedMethodAuthorized(){
		protectedMethods.setDummyAttribAuthorized("Authorized");
		assertEquals("Authorized", protectedMethods.getDummyAttrib());
	}
	
	/**
	 * This test aim to verify the priority of method authorization over class authorization
	 */
	@Test
	public void callNotAnnotatedMethod(){
		try{
			protectedClassAndMethod.setDummyAttribWithClassAuthorization("Class not authorized");
			fail();
		}catch(AuthorizationException cause){
		}
		
		protectedClassAndMethod.setDummyAttribWithAuthorization("Method authorized");
	}
	
	
	
	@After
	public void deactiveContext() {
		securityContext.logout();
		
		SessionContext ctx = Beans.getReference(SessionContext.class);
		ctx.deactivate();
	}
}
