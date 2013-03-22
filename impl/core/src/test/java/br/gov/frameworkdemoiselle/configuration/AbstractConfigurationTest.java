package br.gov.frameworkdemoiselle.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import br.gov.frameworkdemoiselle.annotation.Ignore;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.internal.bootstrap.ConfigurationBootstrap;
import br.gov.frameworkdemoiselle.internal.bootstrap.CoreBootstrap;
import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.internal.producer.LocaleProducer;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.Beans;

public abstract class AbstractConfigurationTest {

	protected static Class<?>[] getConfigurationClasses() {
		List<Class<?>> result = new ArrayList<Class<?>>();

		result.add(Ignore.class);
		result.add(Name.class);
		result.add(Configuration.class);
		result.add(CoreBootstrap.class);
		result.add(ConfigurationBootstrap.class);
		result.add(ConfigurationLoader.class);
		result.add(Beans.class);
		result.add(ResourceBundleProducer.class);
		result.add(LoggerProducer.class);
		result.add(LocaleProducer.class);

		return result.toArray(new Class<?>[0]);
	}

	public static JavaArchive createConfigurationDeployment() {
		return ShrinkWrap
				.create(JavaArchive.class)
				.addClasses(getConfigurationClasses())
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsResource(
						new FileAsset(new File("src/test/resources/configuration/fields/basic/demoiselle.properties")),
						"demoiselle.properties").
				addAsResource(
						new FileAsset(new File("src/test/resources/configuration/fields/basic/demoiselle.xml")),
						"demoiselle.xml")
				.addAsManifestResource(
						new File("src/main/resources/META-INF/services/javax.enterprise.inject.spi.Extension"),
						"services/javax.enterprise.inject.spi.Extension");
	}
}
