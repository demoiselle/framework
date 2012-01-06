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
package br.gov.frameworkdemoiselle.template;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.powermock.reflect.Whitebox.setInternalState;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Faces;
import br.gov.frameworkdemoiselle.util.Parameter;
import br.gov.frameworkdemoiselle.util.Reflections;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

import com.sun.faces.util.Util;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Parameter.class, Beans.class, Reflections.class, Converter.class, FacesContext.class, Util.class, Faces.class })
public class AbstractEditPageBeanTest {

	private AbstractEditPageBean<Contact, Object> pageBean;

	private ResourceBundle bundle;

	@Before
	public void before() {
		bundle = new ResourceBundleProducer().create("demoiselle-jsf-bundle");

		pageBean = new AbstractEditPageBean<Contact, Object>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String update() {
				return null;
			}

			@Override
			public String insert() {
				return null;
			}

			@Override
			public String delete() {
				return null;
			}

			@Override
			protected void handleLoad() {
			}
		};
	}

	@Test
	public void testClear() {
		Parameter<?> param = PowerMock.createMock(Parameter.class);

		assertNull(Whitebox.getInternalState(pageBean, "bean"));
		assertNull(Whitebox.getInternalState(pageBean, "id"));

		setInternalState(pageBean, "bean", new Contact());
		setInternalState(pageBean, "id", param);

		pageBean.clear();

		assertNull(Whitebox.getInternalState(pageBean, "bean"));
		assertNull(Whitebox.getInternalState(pageBean, "id"));
	}

	@Test
	public void testCreateBean() {
		mockStatic(Beans.class);
		Contact c = new Contact();
		expect(Beans.getReference(Contact.class)).andReturn(c);

		replayAll();
		assertEquals(c, pageBean.createBean());
		verifyAll();
	}

	@Test
	public void testGetBean() {

		pageBean = new AbstractEditPageBean<Contact, Object>() {

			private static final long serialVersionUID = 1L;

			private boolean updateMode = false;

			@Override
			public String update() {
				return null;
			}

			@Override
			public String insert() {
				return null;
			}

			@Override
			public String delete() {
				return null;
			}

			@Override
			protected void handleLoad() {
				this.setBean(new Contact(200L));
			}

			public boolean isUpdateMode() {
				return updateMode;
			}

		};

		Contact c = new Contact();
		assertNull(Whitebox.getInternalState(pageBean, "bean"));
		setInternalState(pageBean, "bean", c);
		assertEquals(c, pageBean.getBean());

		mockStatic(Beans.class);
		expect(Beans.getReference(Contact.class)).andReturn(c);

		pageBean.clear();

		replayAll();
		assertEquals(c, pageBean.getBean());
		verifyAll();

		pageBean.clear();

		setInternalState(pageBean, "updateMode", true);
		assertEquals(Long.valueOf(200), pageBean.getBean().getId());
	}

	@Test
	public void testGetBeanClass() {
		mockStatic(Reflections.class);
		expect(Reflections.getGenericTypeArgument(pageBean.getClass(), 0)).andReturn(Object.class);

		assertNull(Whitebox.getInternalState(pageBean, "beanClass"));

		replayAll();
		assertEquals(Object.class, pageBean.getBeanClass());
		verifyAll();

		setInternalState(pageBean, "beanClass", Contact.class, AbstractEditPageBean.class);
		assertEquals(Contact.class, pageBean.getBeanClass());
	}

	@Test
	public void testGetIdClass() {
		mockStatic(Reflections.class);
		expect(Reflections.getGenericTypeArgument(pageBean.getClass(), 1)).andReturn(Object.class);

		assertNull(Whitebox.getInternalState(pageBean, "idClass"));

		replayAll();
		assertEquals(Object.class, pageBean.getIdClass());
		verifyAll();

		Whitebox.setInternalState(pageBean, "idClass", Long.class, AbstractEditPageBean.class);
		assertEquals(Long.class, pageBean.getIdClass());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetStringId() {
		mockStatic(Util.class);

		FacesContext facesContext = createMock(FacesContext.class);
		Parameter<String> parameter = createMock(Parameter.class);

		setInternalState(pageBean, "facesContext", facesContext);
		setInternalState(pageBean, "id", parameter);
		setInternalState(pageBean, "idClass", String.class, AbstractEditPageBean.class);

		String value = "1";
		expect(parameter.getValue()).andReturn(value);

		replayAll();
		assertEquals(value, pageBean.getId());
		verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetLongId() {
		mockStatic(Faces.class);
		
		FacesContext facesContext = createMock(FacesContext.class);
		Converter converter = createMock(Converter.class);
		UIViewRoot viewRoot = createMock(UIViewRoot.class);
		Parameter<String> parameter = createMock(Parameter.class);
		
		setInternalState(pageBean, "facesContext", facesContext);
		setInternalState(pageBean, "id", parameter);
		setInternalState(pageBean, "idClass", Long.class, AbstractEditPageBean.class);
		
		String value = "1";
		
		expect(parameter.getValue()).andReturn(value);
		expect(facesContext.getViewRoot()).andReturn(viewRoot);
		expect(Faces.getConverter(Long.class)).andReturn(converter);
		expect(converter.getAsObject(facesContext, viewRoot, value)).andReturn(Long.valueOf(value));
		
		replayAll();
		assertEquals(Long.valueOf(value), pageBean.getId());
		verifyAll();
	}

	@Test
	public void testGetNotStringIdWithNullConverter() {
		FacesContext facesContext = createMock(FacesContext.class);

		setInternalState(pageBean, "facesContext", facesContext);
		setInternalState(pageBean, "idClass", Contact.class, AbstractEditPageBean.class);
		setInternalState(pageBean, "bundle", bundle);

		replayAll();
		try {
			pageBean.getId();
		} catch (DemoiselleException cause) {
			assertEquals(bundle.getString("id-converter-not-found", Contact.class.getCanonicalName()), cause.getMessage());
		}

		verifyAll();
	}

	@SuppressWarnings("serial")
	@Test
	public void testUpdateMode() {

		pageBean = new AbstractEditPageBean<Contact, Object>() {

			private Long id = null;

			@Override
			public String update() {
				return null;
			}

			@Override
			public String insert() {
				return null;
			}

			@Override
			public String delete() {
				return null;
			}

			@Override
			protected void handleLoad() {
				this.setBean(new Contact(200L));
			}

			public Long getId() {
				return id;
			}

		};

		assertFalse(pageBean.isUpdateMode());
		setInternalState(pageBean, "id", 1L);
		assertTrue(pageBean.isUpdateMode());
	}
}
