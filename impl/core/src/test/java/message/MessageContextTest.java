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

import resourcebundle.parameter.ResourceBundleWithParameter;
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
		messageContext.add(message);
		assertEquals(messageContext.getMessages().size(), 1);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}

	@Test
	public void testAddMessageWithoutParamsIfSeverityIsInfo() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Menssage without param");
		messageContext.add(message);
		assertEquals(messageContext.getMessages().get(0).getSeverity(), SeverityType.INFO);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}

	@Test
	public void testAddMessageWitSeverityInfo() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Menssage without param", SeverityType.INFO);
		messageContext.add(message);
		assertEquals(messageContext.getMessages().get(0).getSeverity(), SeverityType.INFO);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}

	@Test
	public void testAddMessageWitSeverityWarn() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Menssage without param", SeverityType.WARN);
		messageContext.add(message);
		assertEquals(messageContext.getMessages().get(0).getSeverity(), SeverityType.WARN);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}
	
	@Test
	public void testAddMessageWitSeverityErro() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Menssage without param", SeverityType.ERROR);
		messageContext.add(message);
		assertEquals(messageContext.getMessages().get(0).getSeverity(), SeverityType.ERROR);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}
	
	@Test
	public void testCleanMessageContext() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Menssage without param");
		messageContext.add(message);
		assertEquals(messageContext.getMessages().size(), 1);
		messageContext.clear();
		assertEquals(messageContext.getMessages().size(), 0);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}

	@Test
	public void testRecoverMessageWithParams() {
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message message = new DefaultMessage("Message with {0} param");
		messageContext.add(message, 1);
		assertTrue(messageContext.getMessages().get(0).getText().equals("Message with 1 param"));
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
	public void testMessageParsedText(){
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message MESSAGE_PARSED = new DefaultMessage("{MESSAGE_PARSED}");
		String expected = "Message parsed";
		String value = MESSAGE_PARSED.getText();
		Assert.assertEquals(expected, value);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}
	
	@Test
	public void testMessageIsNull(){
		ContextManager.activate(ManagedContext.class, RequestScoped.class);
		Message NULL_MESSAGE = new DefaultMessage(null);
		String expected = null;
		String value = NULL_MESSAGE.getText();
		Assert.assertEquals(expected, value);
		ContextManager.deactivate(ManagedContext.class, RequestScoped.class);
	}
	
}
