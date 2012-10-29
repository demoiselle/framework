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
// * along with this program; if not,  see <http:www.gnu.org/licenses/>
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
// * "LICENCA.txt", junto com esse programa. Se não, acesse <http:www.gnu.org/licenses/>
// * ou escreva para a Fundação do Software Livre (FSF) Inc.,
// * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
// */
//package br.gov.frameworkdemoiselle.internal.bootstrap;
//
//import static junit.framework.Assert.assertEquals;
//import static org.easymock.EasyMock.expect;
//import static org.easymock.EasyMock.replay;
//import static org.easymock.EasyMock.verify;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//import java.lang.reflect.Field;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Locale;
//import java.util.Set;
//
//import javax.enterprise.context.ConversationScoped;
//import javax.enterprise.context.RequestScoped;
//import javax.enterprise.context.SessionScoped;
//import javax.enterprise.inject.spi.AfterBeanDiscovery;
//import javax.enterprise.inject.spi.AnnotatedMethod;
//import javax.enterprise.inject.spi.AnnotatedType;
//import javax.enterprise.inject.spi.BeanManager;
//import javax.enterprise.inject.spi.ProcessAnnotatedType;
//
//import junit.framework.Assert;
//
//import org.easymock.EasyMock;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.api.easymock.PowerMock;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;
//import org.slf4j.Logger;
//
//import br.gov.frameworkdemoiselle.annotation.Startup;
//import br.gov.frameworkdemoiselle.annotation.ViewScoped;
//import br.gov.frameworkdemoiselle.internal.context.Contexts;
//import br.gov.frameworkdemoiselle.internal.context.ThreadLocalContext;
//import br.gov.frameworkdemoiselle.internal.processor.StartupProcessor;
//import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
//import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
//import br.gov.frameworkdemoiselle.util.ResourceBundle;
//
//@Ignore
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ Contexts.class, LoggerProducer.class, ResourceBundle.class, ResourceBundleProducer.class })
//@SuppressWarnings({ "rawtypes", "unchecked" })
//public class StartupBootstrapTest {
//
//	private ProcessAnnotatedType event;
//
//	private BeanManager beanManager;
//
//	private AnnotatedType annotatedType;
//
//	@Before
//	public void before() {
//		event = EasyMock.createMock(ProcessAnnotatedType.class);
//		annotatedType = EasyMock.createMock(AnnotatedType.class);
//		beanManager = null;
//	}
//
//	private List<StartupProcessor> getActions(StartupBootstrap bootstrap) throws IllegalArgumentException,
//			IllegalAccessException {
//		Set<Field> fields = Whitebox.getAllStaticFields(StartupBootstrap.class);
//		List<StartupProcessor> list = null;
//		for (Field field : fields) {
//			if (field.getName().equals("processors")) {
//				list = (List<StartupProcessor>) field.get(bootstrap);
//			}
//		}
//		return list;
//	}
//
//	@Test
//	public void processAnnotatedType() throws IllegalArgumentException, IllegalAccessException {
//		StartupBootstrap bootstrap = new StartupBootstrap();
//		List<StartupProcessor> list = getActions(bootstrap);
//
//		assertNotNull(list);
//		assertTrue(list.isEmpty());
//
//		AnnotatedMethod am1 = PowerMock.createMock(AnnotatedMethod.class);
//		AnnotatedMethod am2 = PowerMock.createMock(AnnotatedMethod.class);
//		AnnotatedMethod am3 = PowerMock.createMock(AnnotatedMethod.class);
//
//		Set<AnnotatedMethod> set = new HashSet<AnnotatedMethod>();
//		set.add(am1);
//		set.add(am2);
//		set.add(am3);
//
//		expect(am1.isAnnotationPresent(Startup.class)).andReturn(true);
//		expect(am2.isAnnotationPresent(Startup.class)).andReturn(true);
//		expect(am3.isAnnotationPresent(Startup.class)).andReturn(false);
//		expect(event.getAnnotatedType()).andReturn(annotatedType);
//		expect(annotatedType.getMethods()).andReturn(set);
//
//		replay(event, annotatedType, am1, am2, am3);
//		bootstrap.processAnnotatedType(event, beanManager);
//		verify(event, annotatedType);
//
//		list = getActions(bootstrap);
//		assertNotNull(list);
//		assertFalse(list.isEmpty());
//		assertTrue(list.size() == 2);
//	}
//
//	@SuppressWarnings("static-access")
//	@Test
//	public void testLoadTempContexts() {
//		StartupBootstrap bootstrap = new StartupBootstrap();
//
//		Logger logger = PowerMock.createMock(Logger.class);
//		ResourceBundleProducer bundleFactory = PowerMock.createMock(ResourceBundleProducer.class);
//		ResourceBundle bundle = PowerMock.createMock(ResourceBundle.class);
//
//		PowerMock.mockStatic(Contexts.class);
//		PowerMock.mockStatic(LoggerProducer.class);
//
//		List<ThreadLocalContext> tempContexts = Whitebox.getInternalState(bootstrap, "tempContexts");
//
//		assertNotNull(tempContexts);
//		assertTrue(tempContexts.isEmpty());
//
//		expect(LoggerProducer.create(EasyMock.anyObject(Class.class))).andReturn(logger).anyTimes();
//		expect(bundleFactory.create(EasyMock.anyObject(String.class), EasyMock.anyObject(Locale.class))).andReturn(
//				bundle).anyTimes();
//		expect(bundle.getString(EasyMock.anyObject(String.class), EasyMock.anyObject(String.class))).andReturn("")
//				.anyTimes();
//
//		logger.trace(EasyMock.anyObject(String.class));
//		EasyMock.expectLastCall().anyTimes();
//
//		Contexts.add(EasyMock.anyObject(ThreadLocalContext.class), EasyMock.anyObject(AfterBeanDiscovery.class));
//		EasyMock.expectLastCall().anyTimes();
//
//		Whitebox.setInternalState(AbstractBootstrap.class, ResourceBundleProducer.class, bundleFactory);
//
//		PowerMock.replayAll();
//		bootstrap.loadTempContexts(null);
//		PowerMock.verifyAll();
//
//		assertNotNull(tempContexts);
//		assertEquals(4, tempContexts.size());
//
//		for (ThreadLocalContext tlc : tempContexts) {
//			if (!tlc.getScope().equals(SessionScoped.class) && !tlc.getScope().equals(ConversationScoped.class)
//					&& !tlc.getScope().equals(RequestScoped.class) && !tlc.getScope().equals(ViewScoped.class)) {
//				fail();
//			}
//		}
//	}
//
//	@SuppressWarnings("static-access")
//	@Test
//	public void testStartup() throws Throwable {
//		StartupBootstrap bootstrap = new StartupBootstrap();
//
//		PowerMock.mockStatic(Contexts.class);
//		PowerMock.mockStatic(LoggerProducer.class);
//
//		Logger logger = PowerMock.createMock(Logger.class);
//		ResourceBundleProducer bundleFactory = PowerMock.createMock(ResourceBundleProducer.class);
//		ResourceBundle bundle = PowerMock.createMock(ResourceBundle.class);
//
//		expect(LoggerProducer.create(EasyMock.anyObject(Class.class))).andReturn(logger).anyTimes();
//		expect(bundleFactory.create(EasyMock.anyObject(String.class), EasyMock.anyObject(Locale.class))).andReturn(
//				bundle).anyTimes();
//		expect(bundle.getString(EasyMock.anyObject(String.class), EasyMock.anyObject(String.class))).andReturn("")
//				.anyTimes();
//
//		logger.debug(EasyMock.anyObject(String.class));
//		EasyMock.expectLastCall().anyTimes();
//
//		Whitebox.setInternalState(AbstractBootstrap.class, ResourceBundleProducer.class, bundleFactory);
//
//		List<StartupProcessor> list = getActions(bootstrap);
//		list.clear();
//
//		MyProcessor<?> processor = PowerMock.createMock(MyProcessor.class);
//		list.add(processor);
//		expect(processor.process()).andReturn(true).times(1);
//
//		PowerMock.replayAll();
//		bootstrap.startup();
//
//		assertTrue(list.isEmpty());
//		PowerMock.verifyAll();
//	}
//
//	@SuppressWarnings("static-access")
//	@Test
//	public void testLoadTempContextsAndStartup() {
//
//		StartupBootstrap bootstrap = new StartupBootstrap();
//
//		bootstrap.loadTempContexts(null);
//		Assert.assertFalse(Contexts.getActiveContexts().isEmpty());
//
//		try {
//			bootstrap.startup();
//			Assert.assertTrue(Contexts.getActiveContexts().isEmpty());
//		} catch (Throwable e) {
//			fail();
//		}
//	}
//}
//
//@SuppressWarnings("rawtypes")
//class MyProcessor<T> extends StartupProcessor<T> {
//
//	@SuppressWarnings("unchecked")
//	public MyProcessor(AnnotatedMethod annotatedMethod, BeanManager beanManager) {
//		super(annotatedMethod, beanManager);
//	}
//
//	@Override
//	public int compareTo(final StartupProcessor<T> other) {
//		return 1;
//	}
// }
