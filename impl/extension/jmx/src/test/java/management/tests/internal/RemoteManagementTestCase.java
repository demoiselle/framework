package management.tests.internal;

import java.io.File;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import management.LocaleProducer;
import management.domain.ManagedTestClass;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * Tests remote management using JMX.
 * 
 * <p>
 * TODO Currently you need to configure Eclipse Run configuration to pass
 * system properties enabling remote management to the Java VM. Arquillian's
 * current version doesn't support this.
 * </p>
 * 
 * <p>
 * On a SUN virtual machine these properties need to be set to enable remote management:
 * <ul>
 * <li>com.sun.management.jmxremote.port=9999</li>
 * <li>com.sun.management.jmxremote.authenticate=false</li>
 * <li>com.sun.management.jmxremote.ssl=false</li>
 * </ul>  
 * </p>
 * 
 * @author serpro
 *
 */
@RunWith(Arquillian.class)
@Ignore
public class RemoteManagementTestCase {

	@Deployment(testable=false)
	public static JavaArchive createDeployment() {
		JavaArchive mainDeployment = ShrinkWrap.create(JavaArchive.class);
		mainDeployment
				.addPackages(true, "br")
				.addAsResource(new FileAsset(new File("src/test/resources/test/beans.xml")), "beans.xml")
				.addAsResource(new FileAsset(new File("src/test/resources/configuration/demoiselle.properties")),
						"demoiselle.properties").addPackages(false, DynamicMBeanProxyTestCase.class.getPackage())
				.addClasses(LocaleProducer.class, ManagedTestClass.class);

		return mainDeployment;
	}

	@Test
	@RunAsClient
	public void testRemotePropertyReading() {
		try {
			JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi");
			JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
			MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

			ObjectName name = null;
			try {
				name = new ObjectName("br.gov.frameworkdemoiselle.jmx.domain:name=ManagedTest");
			} catch (MalformedObjectNameException e) {
				Assert.fail();
			}

			mbsc.setAttribute(name, new Attribute("attribute", "New Value"));
			Object info = mbsc.getAttribute(name, "attribute");

			Assert.assertEquals("New Value", info);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

}
