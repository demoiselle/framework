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
//package br.gov.frameworkdemoiselle.internal.implementation;
//
//import java.util.Locale;
//
//import junit.framework.Assert;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.api.easymock.PowerMock;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;
//import org.slf4j.Logger;
//
//import br.gov.frameworkdemoiselle.message.Message;
//import br.gov.frameworkdemoiselle.message.MessageContext;
//import br.gov.frameworkdemoiselle.message.SeverityType;
//import br.gov.frameworkdemoiselle.util.ResourceBundle;
//
//@RunWith(PowerMockRunner.class)
//public class MessageContextImplTest {
//
//	MessageContext messageContext;
//
//	Message m1;
//
//	@SuppressWarnings("unused")
//	@Before
//	public void before() {
//		messageContext = new MessageContextImpl();
//
//		Logger logger = PowerMock.createMock(Logger.class);
//		ResourceBundle bundle = new ResourceBundle(ResourceBundle.getBundle("demoiselle-core-bundle"));
//
//		Whitebox.setInternalState(messageContext, "logger", logger);
//		Whitebox.setInternalState(messageContext, "bundle", bundle);
//
//		m1 = new Message() {
//
//			private String key = "m1.message";
//
//			private String resourceName = "messages";
//
//			private Locale locale = Locale.getDefault();
//
//			private SeverityType severityType = SeverityType.INFO;
//
//			private Object[] parameters = {};
//
//			public String getText() {
//				return key;
//			}
//
//			public void setKey(String key) {
//				this.key = key;
//			}
//
//			public String getResourceName() {
//				return resourceName;
//			}
//
//			public void setResourceName(String resourceName) {
//				this.resourceName = resourceName;
//			}
//
//			public Locale getLocale() {
//				return locale;
//			}
//
//			public void setLocale(Locale locale) {
//				this.locale = locale;
//			}
//
//			public SeverityType getSeverity() {
//				return severityType;
//			}
//
//			public void setSeverityType(SeverityType severityType) {
//				this.severityType = severityType;
//			}
//
//			public Object[] getParameters() {
//				return parameters;
//			}
//
//			public Message setParameters(Object[] parameters) {
//				this.parameters = parameters;
//				return this;
//			}
//
//			public String getStringMessage() {
//				return "stringMessage";
//			}
//		};
//	}
//
//	@Test
//	public void testAddMessage() {
//		messageContext.add(m1);
//		Assert.assertTrue(messageContext.getMessages().size() == 1);
//		Assert.assertTrue(messageContext.getMessages().contains(m1));
//
//	}
//
//	@Test
//	public void testAddMessageObjectArray() {
//		Object[] param = { "1", "2" };
//		messageContext.add(m1, param);
//
//		Assert.assertTrue(messageContext.getMessages().size() == 1);
//		Assert.assertTrue(messageContext.getMessages().contains(m1));
//		Assert.assertNotNull(messageContext.getMessages().get(0).getParameters());
//		Assert.assertTrue(messageContext.getMessages().get(0).getParameters()[0] == param[0]);
//		Assert.assertTrue(messageContext.getMessages().get(0).getParameters()[1] == param[1]);
//
//	}
//
//	@Test
//	public void testAddStringObjectArray() {
//		String key = "my.key";
//		Object[] param = { "1", "2" };
//		messageContext.add(key, param);
//
//		Assert.assertTrue(messageContext.getMessages().size() == 1);
//		Assert.assertTrue(messageContext.getMessages().get(0).getText().equals(key));
//		Assert.assertTrue(messageContext.getMessages().get(0).getParameters()[0] == param[0]);
//		Assert.assertTrue(messageContext.getMessages().get(0).getParameters()[1] == param[1]);
//	}
//
//	@Test
//	public void testAddStringLocaleObjectArray() {
//		String key = "my.key";
//		Object[] param = { "1", "2" };
//		Locale locale = Locale.CANADA_FRENCH;
//		messageContext.add(key, locale, param);
//
//		Assert.assertTrue(messageContext.getMessages().size() == 1);
//		Assert.assertTrue(messageContext.getMessages().get(0).getText().equals(key));
//		Assert.assertTrue(messageContext.getMessages().get(0).getLocale().equals(locale));
//		Assert.assertTrue(messageContext.getMessages().get(0).getParameters()[0] == param[0]);
//		Assert.assertTrue(messageContext.getMessages().get(0).getParameters()[1] == param[1]);
//	}
//
//	@Test
//	public void testAddStringLocaleSeverityTypeObjectArray() {
//		String key = "my.key";
//		Object[] param = { "1", "2" };
//		Locale locale = Locale.CANADA_FRENCH;
//		SeverityType severity = SeverityType.ERROR;
//		messageContext.add(key, locale, severity, param);
//
//		Assert.assertTrue(messageContext.getMessages().size() == 1);
//		Assert.assertTrue(messageContext.getMessages().get(0).getText().equals(key));
//		Assert.assertTrue(messageContext.getMessages().get(0).getLocale().equals(locale));
//		Assert.assertTrue(messageContext.getMessages().get(0).getSeverity().equals(severity));
//		Assert.assertTrue(messageContext.getMessages().get(0).getParameters()[0] == param[0]);
//		Assert.assertTrue(messageContext.getMessages().get(0).getParameters()[1] == param[1]);
//	}
//
//	@Test
//	public void testAddStringLocaleSeverityTypeStringObjectArray() {
//		String key = "my.key";
//		Object[] param = { "1", "2" };
//		Locale locale = Locale.CANADA_FRENCH;
//		SeverityType severity = SeverityType.ERROR;
//		String resource = "myresourcename";
//		messageContext.add(key, locale, severity, resource, param);
//
//		Assert.assertTrue(messageContext.getMessages().size() == 1);
//		Assert.assertTrue(messageContext.getMessages().get(0).getText().equals(key));
//		Assert.assertTrue(messageContext.getMessages().get(0).getLocale().equals(locale));
//		Assert.assertTrue(messageContext.getMessages().get(0).getSeverity().equals(severity));
//		Assert.assertTrue(messageContext.getMessages().get(0).getResourceName().equals(resource));
//		Assert.assertTrue(messageContext.getMessages().get(0).getParameters()[0] == param[0]);
//		Assert.assertTrue(messageContext.getMessages().get(0).getParameters()[1] == param[1]);
//	}
//
//	@Test
//	public void testGetMessages() {
//		Assert.assertNotNull(messageContext.getMessages());
//		Assert.assertTrue(messageContext.getMessages().isEmpty());
//
//		messageContext.add("key1");
//		Assert.assertTrue(messageContext.getMessages().size() == 1);
//
//		messageContext.add("key2");
//		Assert.assertTrue(messageContext.getMessages().size() == 2);
//
//		Assert.assertTrue(messageContext.getMessages().get(0).getText().equals("key1"));
//		Assert.assertTrue(messageContext.getMessages().get(1).getText().equals("key2"));
//	}
//
//}
