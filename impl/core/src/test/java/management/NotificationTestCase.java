package management;

import java.io.File;

import javax.inject.Inject;

import junit.framework.Assert;
import management.testclasses.DummyManagedClass;
import management.testclasses.DummyNotificationListener;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.LocaleProducer;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.management.internal.ManagedType;
import br.gov.frameworkdemoiselle.management.internal.MonitoringManager;
import br.gov.frameworkdemoiselle.management.notification.AttributeChangeNotification;
import br.gov.frameworkdemoiselle.management.notification.Notification;
import br.gov.frameworkdemoiselle.management.notification.NotificationManager;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Test the {@link NotificationManager} with a dummy extension
 * to check if notifications are correctly propagated
 * 
 * @author serpro
 *
 */
@RunWith(Arquillian.class)
public class NotificationTestCase {
	
	@Inject
	private NotificationManager manager;
	
	@Inject
	@Name("demoiselle-core-bundle")
	private ResourceBundle bundle;
	
	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap
				.create(JavaArchive.class)
				.addClass(LocaleProducer.class)
				.addPackages(true, "br")
				.addAsResource(new FileAsset(new File("src/test/resources/test/beans.xml")), "beans.xml")
				.addAsManifestResource(
						new File("src/main/resources/META-INF/services/javax.enterprise.inject.spi.Extension"),
						"services/javax.enterprise.inject.spi.Extension")
				.addPackages(false, ManagementBootstrapTestCase.class.getPackage())
				.addClasses(DummyNotificationListener.class,DummyManagedClass.class);
	}
	
	/**
	 * Test sending a normal notification
	 */
	@Test
	public void testSendGenericNotification(){
		manager.sendNotification(new Notification("Test Message"));
		DummyNotificationListener listener = Beans.getReference(DummyNotificationListener.class);
		Assert.assertEquals("Test Message", listener.getMessage());
	}
	
	/**
	 * Test sending a notification of change in attribute
	 */
	@Test
	public void testSendAttributeChangeNotification(){
		manager.sendNotification(new AttributeChangeNotification("Test Message", "attribute", String.class, "old", "new"));
		DummyNotificationListener listener = Beans.getReference(DummyNotificationListener.class);
		Assert.assertEquals("Test Message - attribute", listener.getMessage());
	}
	
	/**
	 * Test if notifications are automatically sent when an attribute from a managed
	 * class change values 
	 */
	@Test
	public void testNotifyChangeManagedClass(){
		MonitoringManager manager = Beans.getReference(MonitoringManager.class);
		
		for (ManagedType type : manager.getManagedTypes()){
			if (type.getType().equals(DummyManagedClass.class)){
				manager.setProperty(type, "id", new Integer(10));
				break;
			}
		}
		
		DummyNotificationListener listener = Beans.getReference(DummyNotificationListener.class);
		Assert.assertEquals( bundle.getString("management-notification-attribute-changed","id",DummyManagedClass.class.getCanonicalName()) + " - id"
				, listener.getMessage());
	}

}
