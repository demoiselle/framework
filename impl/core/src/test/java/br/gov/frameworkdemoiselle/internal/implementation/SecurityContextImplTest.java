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
package br.gov.frameworkdemoiselle.internal.implementation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.enterprise.inject.spi.BeanManager;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.internal.bootstrap.AuthenticatorBootstrap;
import br.gov.frameworkdemoiselle.internal.configuration.SecurityConfig;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Authorizer;
import br.gov.frameworkdemoiselle.security.NotLoggedInException;
import br.gov.frameworkdemoiselle.security.User;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class, ResourceBundle.class })
public class SecurityContextImplTest {

	private SecurityContextImpl context;

	private SecurityConfig config;

	private ResourceBundle bundle;

	@Before
	public void setUpConfig() {
		context = new SecurityContextImpl();
		config = createMock(SecurityConfig.class);

		mockStatic(Beans.class);
		expect(Beans.getReference(SecurityConfig.class)).andReturn(config).anyTimes();
	}

	@Test
	public void testHasPermissionWithSecurityDisabled() {
		expect(config.isEnabled()).andReturn(false);
		replayAll(Beans.class, config);

		try {
			assertTrue(context.hasPermission(null, null));
		} catch (NotLoggedInException e) {
			fail();
		}
	}

	public void mockGetAuthenticator() {
		Class<? extends Authenticator> cache = AuthenticatorImpl.class;
		List<Class<? extends Authenticator>> cacheList = new ArrayList<Class<? extends Authenticator>>();
		cacheList.add(cache);

		AuthenticatorBootstrap bootstrap = PowerMock.createMock(AuthenticatorBootstrap.class);

		expect(Beans.getReference(AuthenticatorBootstrap.class)).andReturn(bootstrap).anyTimes();
		expect(config.getAuthenticatorClass()).andReturn(null).anyTimes();
		expect(bootstrap.getCache()).andReturn(cacheList);
		expect(Beans.getReference(AuthenticatorImpl.class)).andReturn(new AuthenticatorImpl());
		expect(Beans.getReference(Locale.class)).andReturn(Locale.getDefault()).anyTimes();
	}

	@Test
	public void testHasPermissionWithSecurityEnabledAndNotLoggedIn() {
		mockGetAuthenticator();

		expect(config.isEnabled()).andReturn(true).anyTimes();
		replayAll(Beans.class, config);

		bundle = ResourceBundleProducer.create("demoiselle-core-bundle");

		try {
			context.hasPermission(null, null);
			fail();
		} catch (NotLoggedInException e) {
			assertTrue(e.getMessage().equals(bundle.getString("user-not-authenticated")));
		}
	}

	@Test
	public void testHasPermissionWithSecurityEnabledAndLoggedIn() {
		expect(config.isEnabled()).andReturn(true).anyTimes();
		replay(config);

		loginSuccessfully();

		Authorizer authorizer = createMock(Authorizer.class);
		expect(authorizer.hasPermission(null, null)).andReturn(true);

		setInternalState(context, "authorizer", authorizer);

		replay(authorizer);

		try {
			assertTrue(context.hasPermission(null, null));
		} catch (NotLoggedInException e) {
			fail();
		}
	}

	private void loginSuccessfully() {
		Authenticator authenticator = createMock(Authenticator.class);
		// expect(authenticator.authenticate()).andReturn(true);

		BeanManager manager = createMock(BeanManager.class);
		expect(Beans.getBeanManager()).andReturn(manager);
		manager.fireEvent(EasyMock.anyObject(Class.class));
		PowerMock.expectLastCall();

		User user = createMock(User.class);
		expect(authenticator.getUser()).andReturn(user).anyTimes();

		setInternalState(context, "authenticator", authenticator);

		replayAll(authenticator, user, Beans.class, manager);

		context.login();
		assertTrue(context.isLoggedIn());
	}

	@Test
	public void testHasRoleWithSecurityDisabled() {
		expect(config.isEnabled()).andReturn(false);
		replayAll(Beans.class, config);

		try {
			assertTrue(context.hasRole(null));
		} catch (NotLoggedInException e) {
			fail();
		}
	}

	@Test
	public void testHasRoleWithSecurityEnabledAndNotLoggedIn() {
		mockGetAuthenticator();

		expect(config.isEnabled()).andReturn(true).anyTimes();
		replayAll(Beans.class, config);

		bundle = ResourceBundleProducer.create("demoiselle-core-bundle");

		try {
			context.hasRole(null);
			fail();
		} catch (NotLoggedInException e) {
			assertTrue(e.getMessage().equals(bundle.getString("user-not-authenticated")));
		}
	}

	@Test
	public void testHasRoleWithSecurityEnabledAndLoggedIn() {
		expect(config.isEnabled()).andReturn(true).anyTimes();
		replay(config);

		loginSuccessfully();

		Authorizer authorizer = createMock(Authorizer.class);
		expect(authorizer.hasRole(null)).andReturn(true);

		setInternalState(context, "authorizer", authorizer);

		replay(authorizer);

		try {
			assertTrue(context.hasRole(null));
		} catch (NotLoggedInException e) {
			fail();
		}
	}

	@Test
	public void testIsLoggedInWithSecurityEnabled() {
		Authenticator authenticator = createMock(Authenticator.class);
		expect(authenticator.getUser()).andReturn(null).anyTimes();
		expect(config.isEnabled()).andReturn(true).anyTimes();

		setInternalState(context, "authenticator", authenticator);

		replayAll(config, authenticator);

		assertFalse(context.isLoggedIn());
	}

	@Test
	public void testIsLoggedInWithSecurityDisabled() {
		expect(config.isEnabled()).andReturn(false);
		replayAll(config, Beans.class);

		assertTrue(context.isLoggedIn());
	}

	@Test
	public void testLoginWithSecurityDisabled() {
		expect(config.isEnabled()).andReturn(false).times(2);
		replayAll(config, Beans.class);
		context.login();

		assertTrue(context.isLoggedIn());
	}

	@Test
	public void testLoginWithAuthenticationFail() {
		Authenticator authenticator = createMock(Authenticator.class);

		expect(config.isEnabled()).andReturn(true).anyTimes();
		// expect(authenticator.authenticate()).andReturn(false);
		expect(authenticator.getUser()).andReturn(null).anyTimes();

		setInternalState(context, "authenticator", authenticator);

		replayAll(authenticator, config);

		context.login();
		assertFalse(context.isLoggedIn());
	}

	@Test
	public void testLogOutWithSecurityDisabled() {
		expect(config.isEnabled()).andReturn(false).times(2);

		replayAll(config, Beans.class);

		try {
			context.logout();
			assertTrue(context.isLoggedIn());
		} catch (NotLoggedInException e) {
			fail();
		}
	}

	@Test
	public void testLogOutWithoutPreviousLogin() {
		Authenticator authenticator = createMock(Authenticator.class);

		expect(authenticator.getUser()).andReturn(null).anyTimes();
		expect(Beans.getReference(Locale.class)).andReturn(Locale.getDefault()).anyTimes();
		expect(config.isEnabled()).andReturn(true).anyTimes();

		setInternalState(context, "authenticator", authenticator);

		replayAll(config, authenticator, Beans.class);

		bundle = ResourceBundleProducer.create("demoiselle-core-bundle");

		try {
			context.logout();
			fail();
		} catch (NotLoggedInException e) {
			assertTrue(e.getMessage().equals(bundle.getString("user-not-authenticated")));
		}
	}

	@Test
	public void testLogOutAfterSuccessfulLogin() {
		expect(config.isEnabled()).andReturn(true).anyTimes();

		Authenticator authenticator = createMock(Authenticator.class);
		// expect(authenticator.authenticate()).andReturn(true);
		authenticator.unAuthenticate();
		PowerMock.expectLastCall();

		User user = createMock(User.class);
		expect(authenticator.getUser()).andReturn(user);
		expect(authenticator.getUser()).andReturn(null);

		BeanManager manager = createMock(BeanManager.class);
		expect(Beans.getBeanManager()).andReturn(manager).times(2);
		manager.fireEvent(EasyMock.anyObject(Class.class));
		PowerMock.expectLastCall().times(2);

		setInternalState(context, "authenticator", authenticator);

		replayAll(Beans.class, authenticator, user, manager, config);

		context.login();
		context.logout();

		assertFalse(context.isLoggedIn());
	}

	@Test
	public void testGetUserWhenSecurityIsDisabled() {
		Authenticator authenticator = createMock(Authenticator.class);
		expect(authenticator.getUser()).andReturn(null).anyTimes();
		expect(config.isEnabled()).andReturn(false).anyTimes();
		replay(config, authenticator, Beans.class);

		setInternalState(context, "authenticator", authenticator);

		assertNotNull(context.getUser());
		assertNotNull(context.getUser().getId());
		assertNull(context.getUser().getAttribute(null));
		context.getUser().setAttribute(null, null);
	}

	@Test
	public void testGetUserWhenSecurityIsEnabled() {
		Authenticator authenticator = createMock(Authenticator.class);
		expect(authenticator.getUser()).andReturn(null).anyTimes();
		expect(config.isEnabled()).andReturn(true);
		replay(config, authenticator, Beans.class);

		setInternalState(context, "authenticator", authenticator);

		assertNull(context.getUser());
	}

	@Test
	public void testGetUserWhenSecurityIsEnabledAndUserIsNotNull() {
		User user = createMock(User.class);

		Authenticator authenticator = createMock(Authenticator.class);
		expect(authenticator.getUser()).andReturn(user).anyTimes();
		expect(config.isEnabled()).andReturn(true).anyTimes();
		replay(config, user, authenticator, Beans.class);

		setInternalState(context, "authenticator", authenticator);

		assertEquals(context.getUser(), user);
	}

	class AuthenticatorImpl implements Authenticator {

		private static final long serialVersionUID = 1L;

		@Override
		public void authenticate() {
		}

		@Override
		public void unAuthenticate() {
		}

		@Override
		public User getUser() {
			return null;
		}
	}
}
