package br.gov.frameworkdemoiselle.internal.implementation;

import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.util.Beans;

public class ConfigurationImpl {

	private boolean loaded = false;

	@SuppressWarnings("unused")
	private synchronized void load(Object instance) {
		if (!loaded) {
			Beans.getReference(ConfigurationLoader.class).load(instance);
			loaded = true;
		}
	}
}
