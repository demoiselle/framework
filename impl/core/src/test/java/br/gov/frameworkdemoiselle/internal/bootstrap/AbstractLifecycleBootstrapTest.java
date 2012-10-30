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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.gov.frameworkdemoiselle.annotation.ViewScoped;
import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.internal.context.Contexts;
import br.gov.frameworkdemoiselle.internal.context.ThreadLocalContext;
import br.gov.frameworkdemoiselle.internal.implementation.AnnotatedMethodProcessor;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.lifecycle.Startup;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Contexts.class, LoggerProducer.class, ResourceBundle.class, ResourceBundleProducer.class,
		Beans.class, ConfigurationLoader.class })
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class AbstractLifecycleBootstrapTest {

	private ProcessAnnotatedType event;

	private BeanManager beanManager;

	private AnnotatedType annotatedType;

	private StartupBootstrap startupBootstrap;

	private ShutdownBootstrap shutdownBootstrap;

	private Class annotationClass;

	@Before
	public void before() {
		event = createMock(ProcessAnnotatedType.class);
		annotatedType = createMock(AnnotatedType.class);
		beanManager = null;
		annotationClass = null;
	}

	private List<AnnotatedMethodProcessor> getActions(AbstractLifecycleBootstrap bootstrap)
			throws IllegalArgumentException, IllegalAccessException {

		Field fields = Whitebox.getField(AbstractLifecycleBootstrap.class, "processors");
		List<AnnotatedMethodProcessor> list = (List<AnnotatedMethodProcessor>) fields.get(bootstrap);
		return list;
	}

	@Test
	public void processAnnotatedTypeStartup() throws IllegalArgumentException, IllegalAccessException {
		startupBootstrap = new StartupBootstrap();
		List<AnnotatedMethodProcessor> list = getActions(startupBootstrap);

		assertNotNull(list);
		assertTrue(list.isEmpty());

		AnnotatedMethod am1 = createMock(AnnotatedMethod.class);
		AnnotatedMethod am2 = createMock(AnnotatedMethod.class);
		AnnotatedMethod am3 = createMock(AnnotatedMethod.class);

		Set<AnnotatedMethod> set = new HashSet<AnnotatedMethod>();
		set.add(am1);
		set.add(am2);
		set.add(am3);

		expect(am1.isAnnotationPresent(Startup.class)).andReturn(true);
		expect(am2.isAnnotationPresent(Startup.class)).andReturn(true);
		expect(am3.isAnnotationPresent(Startup.class)).andReturn(false);
		expect(event.getAnnotatedType()).andReturn(annotatedType);
		expect(annotatedType.getMethods()).andReturn(set);

		EasyMock.replay(event, annotatedType, am1, am2, am3);
		startupBootstrap.processAnnotatedType(event);
		EasyMock.verify(event, annotatedType);

		list = getActions(startupBootstrap);
		assertNotNull(list);
		assertFalse(list.isEmpty());
		assertTrue(list.size() == 2);
	}

	@Test
	public void testLoadTempContexts() {
		startupBootstrap = new StartupBootstrap();

		List<ThreadLocalContext> tempContexts = Whitebox.getInternalState(startupBootstrap, "tempContexts");

		assertNotNull(tempContexts);
		assertTrue(tempContexts.isEmpty());

		replayAll();
		startupBootstrap.loadTempContexts(null);
		verifyAll();

		assertNotNull(tempContexts);
		Assert.assertEquals(4, tempContexts.size());
		for (ThreadLocalContext tlc : tempContexts) {
			if (!tlc.getScope().equals(SessionScoped.class) && !tlc.getScope().equals(ConversationScoped.class)
					&& !tlc.getScope().equals(RequestScoped.class) && !tlc.getScope().equals(ViewScoped.class)) {
				Assert.fail();
			}
		}
	}
}
