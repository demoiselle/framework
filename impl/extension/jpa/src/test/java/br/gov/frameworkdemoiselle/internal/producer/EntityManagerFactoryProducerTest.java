package br.gov.frameworkdemoiselle.internal.producer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Persistence.class)
public class EntityManagerFactoryProducerTest {

	private EntityManagerFactory emFactory;
	private EntityManagerFactoryProducer producer;
	private Map<String, EntityManagerFactory> cache;
	private Logger logger;
	private ResourceBundle bundle;
	
	@Before
	public void setUp() {
		logger = createMock(Logger.class);
		bundle = new ResourceBundleProducer().create("demoiselle-jpa-bundle", Locale.getDefault());
		producer = new EntityManagerFactoryProducer();
		cache = Collections.synchronizedMap(new HashMap<String, EntityManagerFactory>());
		setInternalState(producer, Map.class, cache);
		setInternalState(producer, Logger.class, logger);
		setInternalState(producer, ResourceBundle.class, bundle);
		emFactory = createMock(EntityManagerFactory.class);
	}
	
	@Test
	public void testCreateWithUnitPersistenceExisting() {
		cache.put("pu1", emFactory);
		Assert.assertEquals(emFactory, producer.create("pu1"));
	}
	
	@Test
	public void testCreateWithUnitPersistenceNotExisting() {
		
		mockStatic(Persistence.class);
		expect(Persistence.createEntityManagerFactory("pu1")).andReturn(emFactory);
		
		replay(Persistence.class);
		
		Assert.assertEquals(emFactory, producer.create("pu1"));
	}
	
	@Test
	public void testInitWithoutError() {
		mockStatic(Persistence.class);
		expect(Persistence.createEntityManagerFactory("pu1")).andReturn(emFactory);
		replay(Persistence.class);
		
		producer.init();
		Assert.assertEquals(emFactory, cache.get("pu1"));
	}
	
	@Test
	public void testInitWithError() {
		try {
			producer.init();
			Assert.fail();
		}catch(DemoiselleException cause) {
			Assert.assertTrue(true);
		}
	}
	
	@Test
	public void testGetCache() {
		Assert.assertEquals(cache, producer.getCache());
	}
	
	@Test
	public void testClose() {
		cache.put("pu1", emFactory);
		emFactory.close();
		replay(emFactory);
		producer.close();
		verify(emFactory);
	}
}
