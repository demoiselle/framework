package br.gov.frameworkdemoiselle.internal.producer;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import javax.naming.Context;
import javax.naming.NamingException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.DemoiselleException;

import com.sun.naming.internal.ResourceManager;

@SuppressWarnings("restriction")
@RunWith(PowerMockRunner.class)
@PrepareForTest(ResourceManager.class)
public class ContextProducerTest {

	@Test
	public void testCreate() {

		Context context = ContextProducer.create();

		Assert.assertNotNull(context);

	}

	@Test
	public void testCreateThrowingException() {

		Context context = null;

		try {

			mockStatic(ResourceManager.class);
			expect(ResourceManager.getInitialEnvironment(null)).andThrow(new NamingException());
			PowerMock.replay(ResourceManager.class);

			context = ContextProducer.create();
			fail();

		} catch (Exception e) {
			assertNull(context);
			assertTrue(e instanceof DemoiselleException);
		}

	}

	// This test exists only to get 100% on conbertura.
	@Test
	public void testInstantiateContextProducer() {
		@SuppressWarnings("unused")
		ContextProducer contextProducer = new ContextProducer();
	}

}
