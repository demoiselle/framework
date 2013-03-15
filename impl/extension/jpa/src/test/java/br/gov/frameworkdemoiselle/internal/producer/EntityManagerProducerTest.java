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

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Persistence.class)
public class EntityManagerProducerTest {

	private EntityManagerProducer producer;

	private Logger logger;

	private ResourceBundle bundle;

	private InjectionPoint ip;

	private EntityManagerConfig config;

	private Annotated annotated;

	private Name name;

	private EntityManagerFactory emf;

	private Map<String, EntityManager> cache;

	private EntityManager em;

	@Before
	public void setUp() {
		emf = createMock(EntityManagerFactory.class);
		em = createMock(EntityManager.class);

		mockStatic(Persistence.class);
		expect(Persistence.createEntityManagerFactory("pu1")).andReturn(emf);
		expect(emf.createEntityManager()).andReturn(em);

		replay(emf, Persistence.class);

		producer = new EntityManagerProducer();
		bundle = ResourceBundleProducer.create("demoiselle-jpa-bundle", Locale.getDefault());
		logger = createMock(Logger.class);

		setInternalState(producer, ResourceBundle.class, bundle);
		setInternalState(producer, Logger.class, logger);

		ip = createMock(InjectionPoint.class);
		config = createMock(EntityManagerConfig.class);
		annotated = createMock(Annotated.class);
	}

	@Test
	public void testClose() {
		em.close();
		replay(em);
		cache = Collections.synchronizedMap(new HashMap<String, EntityManager>());
		cache.put("pu1", em);
		setInternalState(producer, Map.class, cache);
		producer.close();
		verify(em);
	}

	@Test
	public void testGetCache() {
		cache = Collections.synchronizedMap(new HashMap<String, EntityManager>());
		setInternalState(producer, Map.class, cache);
		Assert.assertEquals(cache, producer.getCache());
	}

	@After
	public void tearDown() {
		producer = null;
	}

	// @Test
	// public void testCreateWithEntityManagerAnnotatedWithName() {
	// name = createMock(Name.class);
	// expect(name.value()).andReturn("pu1");
	// expect(annotated.isAnnotationPresent(Name.class)).andReturn(true);
	// expect(annotated.getAnnotation(Name.class)).andReturn(name);
	// expect(ip.getAnnotated()).andReturn(annotated).anyTimes();
	// replay(name, annotated, ip);
	//
	// EntityManagerProxy entityManagerProxy = (EntityManagerProxy) producer.create(ip, config);
	// assertNotNull(entityManagerProxy);
	// }

	// @Test
	// public void testCreateWithPersistenceUnitNameFromDemoiselleProperties() {
	// expect(annotated.isAnnotationPresent(Name.class)).andReturn(false);
	// expect(ip.getAnnotated()).andReturn(annotated).anyTimes();
	// expect(config.getDefaultPersistenceUnitName()).andReturn("pu1");
	//
	// replay(annotated, ip, config);
	//
	// EntityManagerProxy entityManagerProxy = (EntityManagerProxy) producer.create(ip, config);
	// assertNotNull(entityManagerProxy);
	// }

	// @Test
	// public void testCreateWithPersistenceUnitNameFromPersistenceXML() {
	//
	// Map<String, EntityManagerFactory> cache = Collections
	// .synchronizedMap(new HashMap<String, EntityManagerFactory>());
	//
	// cache.put("pu1", emf);
	//
	// EntityManagerFactoryProducer entityManagerFactoryProducer = createMock(EntityManagerFactoryProducer.class);
	//
	// expect(entityManagerFactoryProducer.getCache()).andReturn(cache);
	//
	// expect(annotated.isAnnotationPresent(Name.class)).andReturn(false);
	// expect(ip.getAnnotated()).andReturn(annotated).anyTimes();
	// expect(config.getDefaultPersistenceUnitName()).andReturn(null);
	//
	// replay(annotated, ip, config, entityManagerFactoryProducer);
	//
	// setInternalState(producer, EntityManagerFactoryProducer.class, entityManagerFactoryProducer);
	//
	// EntityManagerProxy entityManagerProxy = (EntityManagerProxy) producer.create(ip, config);
	// assertNotNull(entityManagerProxy);
	// }
}
