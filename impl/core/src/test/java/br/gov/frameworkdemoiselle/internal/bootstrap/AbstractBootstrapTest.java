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
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.context.Contexts;
import br.gov.frameworkdemoiselle.internal.context.ThreadLocalContext;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LoggerProducer.class, Contexts.class })
public class AbstractBootstrapTest {
	
	@After
	public void afterTests() {
		for(int x=0; x < Contexts.getActiveContexts().size(); x++) 
			Contexts.getActiveContexts().remove(x);
		
		for(int x=0; x < Contexts.getInactiveContexts().size(); x++) 
			Contexts.getInactiveContexts().remove(x);
	}
	
	@Test
	@SuppressWarnings({ "unchecked"})
	public void testAddContextThatIsNotActive() {
		mockStatic(LoggerProducer.class);
		Logger logger = PowerMock.createMock(Logger.class);
		logger.trace(EasyMock.anyObject(String.class));
		expect(LoggerProducer.create(EasyMock.anyObject(Class.class))).andReturn(logger);
		replayAll(logger, LoggerProducer.class);

		ThreadLocalContext context = new ThreadLocalContext(Scope.class);

		AbstractBootstrap.addContext(context, null);

		Assert.assertTrue(context.isActive());
		Assert.assertEquals(1, Contexts.getActiveContexts().size());
	}
	
	@Test
	@SuppressWarnings({ "unchecked"})
	public void testAddContextThatIsActive() {
		mockStatic(LoggerProducer.class);
		Logger logger = PowerMock.createMock(Logger.class);
		logger.trace(EasyMock.anyObject(String.class));
		expect(LoggerProducer.create(EasyMock.anyObject(Class.class))).andReturn(logger);
		replayAll(logger, LoggerProducer.class);

		ThreadLocalContext context = new ThreadLocalContext(Scope.class);
		Contexts.getActiveContexts().add(context);
		
		AbstractBootstrap.addContext(context, null);

		Assert.assertFalse(context.isActive());
		Assert.assertEquals(1, Contexts.getInactiveContexts().size());
	}

	@Test
	@SuppressWarnings({ "unchecked"})
	public void testAddContextThatEventIsNotNull() {
		mockStatic(LoggerProducer.class);
		Logger logger = PowerMock.createMock(Logger.class);
		logger.trace(EasyMock.anyObject(String.class));
		expect(LoggerProducer.create(EasyMock.anyObject(Class.class))).andReturn(logger);
		AfterBeanDiscovery event = PowerMock.createMock(AfterBeanDiscovery.class);

		ThreadLocalContext context = new ThreadLocalContext(Scope.class);
		event.addContext(context);
		
		replayAll(logger, LoggerProducer.class);


		AbstractBootstrap.addContext(context, event);

		Assert.assertTrue(context.isActive());
		Assert.assertEquals(1, Contexts.getActiveContexts().size());
		
		verifyAll();
	}
	
	@Test
	@SuppressWarnings({ "unchecked"})
	public void testDisableContextIsActive() {
		mockStatic(LoggerProducer.class);
		Logger logger = PowerMock.createMock(Logger.class);
		logger.trace(EasyMock.anyObject(String.class));
		logger.trace(EasyMock.anyObject(String.class));
		expect(LoggerProducer.create(EasyMock.anyObject(Class.class))).andReturn(logger).anyTimes();

		ThreadLocalContext context = new ThreadLocalContext(Scope.class);
		
		replayAll(logger, LoggerProducer.class);


		AbstractBootstrap.addContext(context, null);

		Assert.assertTrue(context.isActive());
		Assert.assertEquals(1, Contexts.getActiveContexts().size());
		
		AbstractBootstrap.disableContext(context);
		
		Assert.assertFalse(context.isActive());
		Assert.assertEquals(0, Contexts.getActiveContexts().size());
		
	}
	
	@Test
	@SuppressWarnings({ "unchecked"})
	public void testDisableContextIsNotActive() {
		mockStatic(LoggerProducer.class);
		Logger logger = PowerMock.createMock(Logger.class);
		logger.trace(EasyMock.anyObject(String.class));
		logger.trace(EasyMock.anyObject(String.class));
		expect(LoggerProducer.create(EasyMock.anyObject(Class.class))).andReturn(logger).anyTimes();

		ThreadLocalContext context = new ThreadLocalContext(Scope.class);
		
		replayAll(logger, LoggerProducer.class);

		Contexts.getInactiveContexts().add(context);
		Assert.assertEquals(1, Contexts.getInactiveContexts().size());

		AbstractBootstrap.disableContext(context);
		
		Assert.assertEquals(0, Contexts.getInactiveContexts().size());
		
	}
	
	@Test
	@SuppressWarnings({ "unchecked"})
	public void testDisableContextIsActiveAndExistTheSameScopeInTheInactives() {
		mockStatic(LoggerProducer.class);
		Logger logger = PowerMock.createMock(Logger.class);
		logger.trace(EasyMock.anyObject(String.class));
		logger.trace(EasyMock.anyObject(String.class));
		expect(LoggerProducer.create(EasyMock.anyObject(Class.class))).andReturn(logger).anyTimes();

		ThreadLocalContext context = new ThreadLocalContext(Scope.class);
		
		replayAll(logger, LoggerProducer.class);

		AbstractBootstrap.addContext(context, null);

		ThreadLocalContext context2 = new ThreadLocalContext(Scope.class);
		context2.setActive(false);
		Contexts.getInactiveContexts().add(context2);
		
		Assert.assertTrue(context.isActive());
		Assert.assertEquals(1, Contexts.getActiveContexts().size());
		
		Assert.assertFalse(context2.isActive());
		Assert.assertEquals(1, Contexts.getInactiveContexts().size());
		
		AbstractBootstrap.disableContext(context);
		
		Assert.assertFalse(context.isActive());
		Assert.assertTrue(context2.isActive());
		Assert.assertEquals(1, Contexts.getActiveContexts().size());
		Assert.assertEquals(0, Contexts.getInactiveContexts().size());
		
	}
}
