package br.gov.frameworkdemoiselle.internal.producer;

import static org.easymock.EasyMock.replay;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.Test;
import org.powermock.reflect.Whitebox;


public class HttpServletRequestProducerTest {

	private HttpServletRequestProducer httpServletRequestProducer;
	
	private HttpServletRequest request;
	
	@Test
	public void testCreate() {
		request = createMock(HttpServletRequest.class);
		replay(request);

		httpServletRequestProducer = new HttpServletRequestProducer();
		Whitebox.setInternalState(httpServletRequestProducer, "request", request);

		Assert.assertEquals(httpServletRequestProducer.create(), request);
		
		verifyAll();
	}
	
	@Test
	public void testSetDelegate() {
		request = createMock(HttpServletRequest.class);
		replay(request);

		httpServletRequestProducer = new HttpServletRequestProducer();

		httpServletRequestProducer.setDelegate(request);
		Assert.assertEquals(Whitebox.getInternalState(httpServletRequestProducer, "request"),request);
		
		verifyAll();
	}	
	
}
