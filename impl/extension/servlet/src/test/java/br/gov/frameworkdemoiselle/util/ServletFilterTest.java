package br.gov.frameworkdemoiselle.util;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.internal.producer.HttpServletRequestProducer;
import br.gov.frameworkdemoiselle.internal.producer.HttpServletResponseProducer;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Beans.class)
public class ServletFilterTest {

	private ServletFilter filter;

	@Test
	public void testDoFilter() throws IOException, ServletException {
		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);
		FilterChain chain = createMock(FilterChain.class);
		HttpServletRequestProducer requestProducer = createMock(HttpServletRequestProducer.class);
		HttpServletResponseProducer responseProducer = createMock(HttpServletResponseProducer.class);

		mockStatic(Beans.class);
		expect(Beans.getReference(HttpServletRequestProducer.class)).andReturn(requestProducer);
		expect(Beans.getReference(HttpServletResponseProducer.class)).andReturn(responseProducer);
		requestProducer.setDelegate(request);
		PowerMock.expectLastCall().times(1);
		responseProducer.setDelegate(response);
		PowerMock.expectLastCall().times(1);
		chain.doFilter(request, response);
		PowerMock.expectLastCall().times(1);

		replayAll();

		filter = new ServletFilter();

		filter.doFilter(request, response, chain);

		verifyAll();
	}

}
