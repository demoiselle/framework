package br.gov.frameworkdemoiselle.internal.implementation;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;
import br.gov.frameworkdemoiselle.util.Strings;

public class StrategySelector {

	public static <T> T getReference(String configKey, Class<T> type, Class<? extends T> defaultType) {
		Class<T> selectedType = loadSelected(configKey, type, defaultType);
		return Beans.getReference(selectedType);
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> loadSelected(String configKey, Class<T> type, Class<? extends T> defaultType) {
		ResourceBundle bundle = ResourceBundleProducer.create("demoiselle-core-bundle",
				Beans.getReference(Locale.class));

		Class<T> result = null;
		String canonicalName = null;
		String typeName = type.getSimpleName().toLowerCase();
		String key = null;

		try {
			URL url = ConfigurationLoader.getResourceAsURL("demoiselle.properties");
			Configuration config = new PropertiesConfiguration(url);
			canonicalName = config.getString(configKey, defaultType.getCanonicalName());

			ClassLoader classLoader = ConfigurationLoader.getClassLoaderForClass(canonicalName);
			if (classLoader == null) {
				classLoader = Thread.currentThread().getContextClassLoader();
			}

			result = (Class<T>) Class.forName(canonicalName, false, classLoader);
			result.asSubclass(type);

		} catch (org.apache.commons.configuration.ConfigurationException cause) {
			throw new ConfigurationException(bundle.getString("file-not-found", "demoiselle.properties"));

		} catch (ClassNotFoundException cause) {
			key = Strings.getString("{0}-class-not-found", typeName);
			throw new ConfigurationException(bundle.getString(key, canonicalName));

		} catch (FileNotFoundException e) {
			throw new ConfigurationException(bundle.getString("file-not-found", "demoiselle.properties"));

		} catch (ClassCastException cause) {
			key = Strings.getString("{0}-class-must-be-of-type", typeName);
			throw new ConfigurationException(bundle.getString(key, canonicalName, type));
		}

		return result;
	}
}
