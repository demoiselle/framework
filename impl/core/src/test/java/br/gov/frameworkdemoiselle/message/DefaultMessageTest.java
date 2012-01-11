/*
 * Demoiselle Framework Copyright (C) 2010 SERPRO
 * ---------------------------------------------------------------------------- This file is part of Demoiselle
 * Framework. Demoiselle Framework is free software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License version 3 as published by the Free Software Foundation. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU Lesser General Public License version 3 along with this program; if not, see
 * <http://www.gnu.org/licenses/> or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301, USA. ---------------------------------------------------------------------------- Este arquivo
 * é parte do Framework Demoiselle. O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação do Software Livre (FSF). Este
 * programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA GARANTIA; sem uma garantia implícita de
 * ADEQUAÇÃO a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português para
 * maiores detalhes. Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título "LICENCA.txt", junto com esse
 * programa. Se não, acesse <http://www.gnu.org/licenses/> ou escreva para a Fundação do Software Livre (FSF) Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.message;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.powermock.api.easymock.PowerMock.expectPrivate;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.Arrays;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;
import br.gov.frameworkdemoiselle.util.Strings;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class, ResourceBundle.class })
public class DefaultMessageTest {

	private Message message;

	private String MOCK_RESOURCE_BUNDLE_KEY = "{key}";

	private String MOCK_RESOURCE_BUNDLE_VALUE = "param {0} and param {1}";

	private ResourceBundle bundle;

	@Before
	public void setUp() throws Exception {
		bundle = PowerMock.createMock(ResourceBundle.class);
		expect(bundle.getString(MOCK_RESOURCE_BUNDLE_KEY)).andReturn(MOCK_RESOURCE_BUNDLE_VALUE).anyTimes();
		expectPrivate(bundle, "handleGetObject", EasyMock.anyObject(String.class))
				.andReturn(MOCK_RESOURCE_BUNDLE_VALUE).anyTimes();

		mockStatic(Beans.class);
		expect(Beans.getReference(ResourceBundle.class)).andReturn(bundle).anyTimes();
		replayAll(bundle, Beans.class);
	}

	@After
	public void tearDown() throws Exception {
		verifyAll();
	}

	@Test
	public void testConstructorWithTextOnly() {
		message = new DefaultMessage("");
		assertEquals("", message.getText());

		message = new DefaultMessage("a");
		assertEquals("a", message.getText());

		message = new DefaultMessage(null);
		assertEquals(null, message.getText());

		message = new DefaultMessage(MOCK_RESOURCE_BUNDLE_KEY);
		assertEquals(MOCK_RESOURCE_BUNDLE_VALUE, message.getText());
	}

	@Test
	public void testCachedText() {
		message = new DefaultMessage(MOCK_RESOURCE_BUNDLE_KEY);
		assertSame(message.getText(), message.getText());
	}

	@Test
	public void testDefaultSeverity() {
		message = new DefaultMessage(null);
		assertEquals(SeverityType.INFO, message.getSeverity());

		assertEquals(DefaultMessage.DEFAULT_SEVERITY, message.getSeverity());

		message = new DefaultMessage(null, null, new Object[] {});
		assertEquals(DefaultMessage.DEFAULT_SEVERITY, message.getSeverity());
	}

	@Test
	public void testConstructorWithParametrizedText() {
		String text;
		Object[] params;

		text = "";
		params = new Object[] { "1", "2" };
		message = new DefaultMessage(text, params);
		assertEquals(Strings.getString(text, params), message.getText());

		text = "params: {0}, {1}";
		params = new Object[] { "1", "2" };
		message = new DefaultMessage(text, params);
		assertEquals(Strings.getString(text, params), message.getText());

		text = null;
		params = new Object[] { "1" };
		message = new DefaultMessage(text, params);
		assertEquals(Strings.getString(text, params), message.getText());

		text = MOCK_RESOURCE_BUNDLE_KEY;
		params = new Object[] { "1", "2" };
		message = new DefaultMessage(text, params);
		assertEquals(Strings.getString(bundle.getString(text), params), message.getText());
	}

	@Test
	public void testToString() throws SecurityException, NoSuchFieldException {
		String text;
		Object[] params;

		text = "text";
		message = new DefaultMessage(text);
		assertEquals("DefaultMessage [originalText=" + text + ", parsedText=" + text
				+ ", severity=INFO, params=[], bundle=" + bundle.toString() + "]", message.toString());

		text = MOCK_RESOURCE_BUNDLE_KEY;
		params = new Object[] { "1", "2" };
		message = new DefaultMessage(text, SeverityType.FATAL, params);
		assertEquals(
				"DefaultMessage [originalText=" + text + ", parsedText="
						+ Strings.getString(bundle.getString(text), params) + ", severity=FATAL, params="
						+ Arrays.toString(params) + ", bundle=" + bundle.toString() + "]", message.toString());
	}

	@Test
	public void testConstructorWithParametrizedTextAndSeverityType() {
		message = new DefaultMessage("", SeverityType.FATAL, "");
		assertEquals("", message.getText());
		assertEquals(SeverityType.FATAL, message.getSeverity());

		message = new DefaultMessage("text", SeverityType.WARN, "param");
		assertEquals("text", message.getText());
		assertEquals(SeverityType.WARN, message.getSeverity());
	}

	enum MessagesEnum implements Message {

		FIRST_KEY("first-key"), SECOND_KEY("second-key", SeverityType.WARN), THIRD_KEY, FOURTH_KEY(SeverityType.FATAL), LITERAL_TEXT(
				"This is a literal text");

		private final DefaultMessage msg;

		MessagesEnum() {
			msg = new DefaultMessage(this.name());
		}

		MessagesEnum(String name) {
			msg = new DefaultMessage(name);
		}

		MessagesEnum(SeverityType severity) {
			msg = new DefaultMessage(this.name(), severity);
		}

		MessagesEnum(String name, SeverityType severity) {
			msg = new DefaultMessage(name, severity);
		}

		@Override
		public String getText() {
			return msg.getText();
		}

		@Override
		public SeverityType getSeverity() {
			return msg.getSeverity();
		}

	}

	// @Test
	public void testMessagesEnum() {

		// bundle = PowerMock.createMock(ResourceBundle.class);
		// expect(bundle.getString("first-key")).andReturn("First message text");
		// expect(bundle.getString("second-key")).andReturn("Second message text");
		// expect(bundle.getString("THIRD_KEY")).andReturn("Third message text");
		// expect(bundle.getString("FOURTH_KEY")).andReturn("Fourth message text");
		// replayAll(bundle);

		message = MessagesEnum.FIRST_KEY;
		assertEquals(SeverityType.INFO, message.getSeverity());
		assertEquals("First message text", message.getText());

		message = MessagesEnum.SECOND_KEY;
		assertEquals(SeverityType.WARN, message.getSeverity());
		assertEquals("Second message text", message.getText());

		message = MessagesEnum.THIRD_KEY;
		assertEquals(SeverityType.INFO, message.getSeverity());
		assertEquals("Third message text", message.getText());

		message = MessagesEnum.FOURTH_KEY;
		assertEquals(SeverityType.FATAL, message.getSeverity());
		assertEquals("Fourth message text", message.getText());

		message = MessagesEnum.LITERAL_TEXT;
		assertEquals(SeverityType.INFO, message.getSeverity());
		assertEquals("This is a literal text", message.getText());
	}

	enum ErrorMessages implements Message {

		FIRST_ERROR_KEY, SECOND_ERROR_KEY("second-error-key"), LITERAL_ERROR_TEXT("This is a literal error text");

		private final DefaultMessage msg;

		ErrorMessages() {
			msg = new DefaultMessage(this.name(), SeverityType.ERROR);
		}

		ErrorMessages(String name) {
			msg = new DefaultMessage(name, SeverityType.ERROR);
		}

		@Override
		public String getText() {
			return msg.getText();
		}

		@Override
		public SeverityType getSeverity() {
			return msg.getSeverity();
		}

	}

	// @Test
	public void testErrorMessagesEnum() {
		message = ErrorMessages.FIRST_ERROR_KEY;
		assertEquals(SeverityType.ERROR, message.getSeverity());
		assertEquals("First error message text", message.getText());

		message = ErrorMessages.SECOND_ERROR_KEY;
		assertEquals(SeverityType.ERROR, message.getSeverity());
		assertEquals("Second error message text", message.getText());

		message = ErrorMessages.LITERAL_ERROR_TEXT;
		assertEquals(SeverityType.ERROR, message.getSeverity());
		assertEquals("This is a literal error text", message.getText());
	}

	interface MessagesInterface {

		final Message FIRST_KEY = new DefaultMessage("first-key");

		final Message SECOND_KEY = new DefaultMessage("second-key", SeverityType.WARN);

		final Message THIRD_KEY = new DefaultMessage("THIRD_KEY");

		final Message FOURTH_KEY = new DefaultMessage("FOURTH_KEY", SeverityType.FATAL);

		final Message LITERAL_TEXT = new DefaultMessage("This is a literal text");

	}

	// @Test
	public void testMessagesInterface() {
		message = MessagesInterface.FIRST_KEY;
		assertEquals(SeverityType.INFO, message.getSeverity());
		assertEquals("First message text", message.getText());

		message = MessagesInterface.SECOND_KEY;
		assertEquals(SeverityType.WARN, message.getSeverity());
		assertEquals("Second message text", message.getText());

		message = MessagesInterface.THIRD_KEY;
		assertEquals(SeverityType.INFO, message.getSeverity());
		assertEquals("Third message text", message.getText());

		message = MessagesInterface.FOURTH_KEY;
		assertEquals(SeverityType.FATAL, message.getSeverity());
		assertEquals("Fourth message text", message.getText());

		message = MessagesInterface.LITERAL_TEXT;
		assertEquals(SeverityType.INFO, message.getSeverity());
		assertEquals("This is a literal text", message.getText());
	}

}
