package br.gov.frameworkdemoiselle.internal.producer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

public class HttpSessionProducerTest {

	private HttpSessionProducer httpSessionProducer;

	private HttpServletRequest request;

	@Test
	public void testCreateWithRequestNull() {
		httpSessionProducer = new HttpSessionProducer();
		Assert.assertNull(httpSessionProducer.create(null));

		verifyAll();
	}

	@Test
	public void testCreateWithRequest() {
		request = createMock(HttpServletRequest.class);
		HttpSession session = createMock(HttpSession.class);
		EasyMock.expect(request.getSession()).andReturn(session);
		replay(request, session);

		httpSessionProducer = new HttpSessionProducer();
		Assert.assertNotNull(httpSessionProducer.create(request));

		verifyAll();

	}

}
