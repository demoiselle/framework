package br.gov.frameworkdemoiselle.internal.producer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Beans.class)
public class ServletLocaleProducerTest {

	private ServletLocaleProducer servletLocaleProducer;

	private HttpServletRequest request;

	@Test
	public void testCreate() {
		request = createMock(HttpServletRequest.class);

		mockStatic(Beans.class);
		expect(Beans.getReference(HttpServletRequest.class)).andReturn(request);
		replay(Beans.class);

		servletLocaleProducer = new ServletLocaleProducer();
		servletLocaleProducer.create();

		verifyAll();
	}
	
	@Test
	public void testCreate2() {
		servletLocaleProducer = new ServletLocaleProducer();
		servletLocaleProducer.create();

		verifyAll();
	}

}
