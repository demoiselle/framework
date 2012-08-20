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
import org.junit.Ignore;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.bootstrap.CoreBootstrap;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * @author e-saito
 */
/**
 * @author 80342167553
 */
@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest(CoreBootstrap.class)
public class EntityManagerConfigTest {

	private EntityManagerConfig config = new EntityManagerConfig();

	@Before
	public void setUp() throws Exception {
		Logger logger = PowerMock.createMock(Logger.class);
		ResourceBundle bundle = new ResourceBundle("demoiselle-core-bundle", Locale.getDefault());

		ConfigurationLoader configurationLoader = new ConfigurationLoader();

		Whitebox.setInternalState(configurationLoader, "bundle", bundle);
		Whitebox.setInternalState(configurationLoader, "logger", logger);

		mockStatic(CoreBootstrap.class);
		expect(CoreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replay(CoreBootstrap.class);

		configurationLoader.load(config);
	}

	@After
	public void tearDown() throws Exception {
		config = null;
	}

	/**
	 * Test method for
	 * {@link br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig#getPersistenceUnitName()}.
	 */
	@Test
	public void testGetPersistenceUnitName() {
		assertEquals("PersistenceUnitName", config.getPersistenceUnitName());
	}
}
