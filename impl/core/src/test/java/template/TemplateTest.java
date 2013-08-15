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
package template;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.internal.context.ContextManager;
import br.gov.frameworkdemoiselle.internal.context.ManagedContext;

@RunWith(Arquillian.class)
public class TemplateTest {

	private static final String DUMMY_NAME = "Dummy De Meu Deus";
	private static final Long INSERT_ID = 15L;
	private static final Long VALID_ID = 1L;

	@Inject
	private TemplateDelegateCrud templateBC;

	@Inject
	private CrudImpl crudImpl;

	@Deployment
	public static JavaArchive createDeployment() {

		JavaArchive deployment = Tests.createDeployment(TemplateTest.class);

		return deployment;

	}

	@Before
	public void initialize() {

		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		
		this.crudImpl.resetEntities();

	}

	@After
	public void finalize() {

		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
		
	}

	@Test
	public void testInsert() {

		assertNull(this.crudImpl.load(INSERT_ID));
		
		this.templateBC.insert(new DummyEntity(INSERT_ID, DUMMY_NAME));
		
		assertNotNull(this.crudImpl.load(INSERT_ID));
		
	}

	@Test
	public void testRemove() {

		assertNotNull(this.crudImpl.load(VALID_ID));
		
		this.templateBC.delete(VALID_ID);
		
		assertNull(this.crudImpl.load(VALID_ID));
		
	}

	@Test
	public void testFindAll() {

		List<DummyEntity> listImpl = this.crudImpl.findAll();
		List<DummyEntity> listDelegate = this.templateBC.findAll();
		
		assertEquals(listImpl, listDelegate);
		
	}

	@Test
	public void testLoad() {

		DummyEntity dummyEntityImpl = this.crudImpl.load(VALID_ID);
		DummyEntity dummyEntityDelegate = this.templateBC.load(VALID_ID);
		
		assertEquals(dummyEntityImpl, dummyEntityDelegate);
		
	}

	@Test
	public void testUpdate() {

		DummyEntity dummyEntity = this.crudImpl.load(VALID_ID);
		
		assertFalse(DUMMY_NAME.equals(dummyEntity.getName()));
		
		dummyEntity.setName(DUMMY_NAME);
		
		this.templateBC.update(dummyEntity);
		
		assertEquals(this.crudImpl.load(VALID_ID).getName(), DUMMY_NAME);
		
	}
	
}
