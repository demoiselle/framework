package br.gov.frameworkdemoiselle.util;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContextEvent;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.internal.bootstrap.ShutdownBootstrap;
import br.gov.frameworkdemoiselle.lifecycle.AfterStartupProccess;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Beans.class)
public class ServletListenerTest {

	private ServletListener listener;
	
	@Test
	public void testContextInitialized() {
		ServletContextEvent event = createMock(ServletContextEvent.class);
		BeanManager beanManager = PowerMock.createMock(BeanManager.class);
		
		mockStatic(Beans.class);
		expect(Beans.getBeanManager()).andReturn(beanManager);
		beanManager.fireEvent(EasyMock.anyObject(AfterStartupProccess.class));
		PowerMock.expectLastCall().times(1);
		
		replayAll();
		
		listener = new ServletListener();
		listener.contextInitialized(event);
		
		verifyAll();
	}
	
	@Test
	public void testContextDestroyed() {
		ServletContextEvent event = createMock(ServletContextEvent.class);
		BeanManager beanManager = PowerMock.createMock(BeanManager.class);
		
		mockStatic(Beans.class);
		expect(Beans.getBeanManager()).andReturn(beanManager);
		beanManager.fireEvent(EasyMock.anyObject(ShutdownBootstrap.class));
		PowerMock.expectLastCall().times(1);
		
		replayAll();
		
		listener = new ServletListener();
		listener.contextDestroyed(event);
		
		verifyAll();
	}
	
}
