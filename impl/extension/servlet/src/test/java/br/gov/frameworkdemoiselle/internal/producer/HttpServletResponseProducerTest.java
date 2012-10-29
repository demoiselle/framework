package br.gov.frameworkdemoiselle.internal.producer;

import static org.easymock.EasyMock.replay;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.Test;
import org.powermock.reflect.Whitebox;


public class HttpServletResponseProducerTest {

	private HttpServletResponseProducer httpServletResponseProducer;
	
	private HttpServletResponse response;
	
	@Test
	public void testCreate() {
		response = createMock(HttpServletResponse.class);
		replay(response);

		httpServletResponseProducer = new HttpServletResponseProducer();
		Whitebox.setInternalState(httpServletResponseProducer, "response", response);

		Assert.assertEquals(httpServletResponseProducer.create(), response);
		
		verifyAll();
	}
	
	@Test
	public void testSetDelegate() {
		response = createMock(HttpServletResponse.class);
		replay(response);

		httpServletResponseProducer = new HttpServletResponseProducer();

		httpServletResponseProducer.setDelegate(response);
		Assert.assertEquals(Whitebox.getInternalState(httpServletResponseProducer, "response"), response);
		
		verifyAll();
	}	
	
}
