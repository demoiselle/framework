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
package br.gov.frameworkdemoiselle.internal.bootstrap;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.inject.Scope;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.context.Contexts;
import br.gov.frameworkdemoiselle.internal.context.ThreadLocalContext;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LoggerProducer.class, Contexts.class })
public class AbstractBootstrapTest {

	private AbstractBootstrap bootstrap;

	@Before
	public void setUp() {
		bootstrap = new AbstractBootstrap();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAddContext() {
		mockStatic(LoggerProducer.class);
		mockStatic(Contexts.class);

		Logger logger = PowerMock.createMock(Logger.class);
		logger.trace(EasyMock.anyObject(String.class));
		
		expect(LoggerProducer.create(EasyMock.anyObject(Class.class))).andReturn(logger);

		ResourceBundle bundle = PowerMock.createMock(ResourceBundle.class);
		expect(bundle.getString(EasyMock.anyObject(String.class), EasyMock.anyObject(String.class))).andReturn(null);

		ResourceBundleProducer bundleFactory = PowerMock.createMock(ResourceBundleProducer.class);
		expect(bundleFactory.create(EasyMock.anyObject(String.class))).andReturn(bundle);

		Whitebox.setInternalState(AbstractBootstrap.class, "bundleFactory", bundleFactory);

		ThreadLocalContext context = new ThreadLocalContext(Scope.class);
		Contexts.add(EasyMock.anyObject(ThreadLocalContext.class), EasyMock.anyObject(AfterBeanDiscovery.class));
		replayAll(bundle, bundleFactory, logger, LoggerProducer.class, Contexts.class);

		bootstrap.addContext(context,null);

		verifyAll();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDisableContext() {
		AbstractBootstrap bootstrap = new AbstractBootstrap();

		mockStatic(LoggerProducer.class);
		mockStatic(Contexts.class);

		Logger logger = PowerMock.createMock(Logger.class);
		logger.trace(EasyMock.anyObject(String.class));
		expect(LoggerProducer.create(EasyMock.anyObject(Class.class))).andReturn(logger);

		ResourceBundle bundle = PowerMock.createMock(ResourceBundle.class);
		expect(bundle.getString(EasyMock.anyObject(String.class), EasyMock.anyObject(String.class))).andReturn(null);

		ResourceBundleProducer bundleFactory = PowerMock.createMock(ResourceBundleProducer.class);
		expect(bundleFactory.create(EasyMock.anyObject(String.class))).andReturn(bundle);

		Whitebox.setInternalState(AbstractBootstrap.class, "bundleFactory", bundleFactory);

		ThreadLocalContext context = new ThreadLocalContext(Scope.class);

		Contexts.remove(context);
		replayAll(bundle, bundleFactory, logger, LoggerProducer.class, Contexts.class);

		bootstrap.disableContext(context);

		verifyAll();
	}

}
