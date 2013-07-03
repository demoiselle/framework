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
package message;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.internal.context.ContextManager;
import br.gov.frameworkdemoiselle.internal.context.ManagedContext;
import br.gov.frameworkdemoiselle.message.DefaultMessage;
import br.gov.frameworkdemoiselle.message.Message;
import br.gov.frameworkdemoiselle.message.MessageContext;
import br.gov.frameworkdemoiselle.message.SeverityType;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class MessageContextTest {

	@Inject
	private MessageContext messageContext;

	@Inject
	private MessageWithResourceBundle bundleCustom;

	private static final String PATH = "src/test/resources/message/";

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(MessageContextTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "messages.properties"), "messages.properties");

		return deployment;
	}

	@Test
	public void testAddMessageWithoutParams() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Menssage without param");
		DummyMessageAppender appender = Beans.getReference(DummyMessageAppender.class);

		messageContext.add(message);
		assertEquals(appender.getMessages().size(), 1);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}

	@Test
	public void testAddMessageWithoutParamsIfSeverityIsInfo() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Menssage without param");
		DummyMessageAppender appender = Beans.getReference(DummyMessageAppender.class);

		messageContext.add(message);
		assertEquals(appender.getMessages().get(0).getSeverity(), SeverityType.INFO);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}

	@Test
	public void testAddMessageWitSeverityInfo() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Menssage without param", SeverityType.INFO);
		DummyMessageAppender appender = Beans.getReference(DummyMessageAppender.class);

		messageContext.add(message);
		assertEquals(appender.getMessages().get(0).getSeverity(), SeverityType.INFO);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}

	@Test
	public void testAddMessageWitSeverityWarn() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Menssage without param", SeverityType.WARN);
		DummyMessageAppender appender = Beans.getReference(DummyMessageAppender.class);

		messageContext.add(message);
		assertEquals(appender.getMessages().get(0).getSeverity(), SeverityType.WARN);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}

	@Test
	public void testAddMessageWitSeverityErro() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Menssage without param", SeverityType.ERROR);
		DummyMessageAppender appender = Beans.getReference(DummyMessageAppender.class);

		messageContext.add(message);
		assertEquals(appender.getMessages().get(0).getSeverity(), SeverityType.ERROR);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}

	@Test
	public void testRecoverMessageWithParams() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Message with {0} param");
		DummyMessageAppender appender = Beans.getReference(DummyMessageAppender.class);

		messageContext.add(message, 1);
		assertTrue(appender.getMessages().get(0).getText().equals("Message with 1 param"));
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}

	@Test
	public void testMessageWithResourceBundle() {
		bundleCustom = Beans.getReference(MessageWithResourceBundle.class);
		String expected = "Mensagem sem parâmetro";
		String value = bundleCustom.getBundle().getString("MESSAGE_WITHOUT_PARAMETER");
		Assert.assertEquals(expected, value);
	}

	@Test
	public void testMessageParsedText() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message MESSAGE_PARSED = new DefaultMessage("{MESSAGE_PARSED}");
		String expected = "Message parsed";
		String value = MESSAGE_PARSED.getText();
		Assert.assertEquals(expected, value);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}

	@Test
	public void testMessageIsNull() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message NULL_MESSAGE = new DefaultMessage(null);
		String expected = null;
		String value = NULL_MESSAGE.getText();
		Assert.assertEquals(expected, value);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}
}
