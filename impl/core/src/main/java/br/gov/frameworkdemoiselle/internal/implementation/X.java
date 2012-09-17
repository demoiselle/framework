package br.gov.frameworkdemoiselle.internal.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;

public class X {

	private final Map<ClassLoader, List<Class<?>>> cache = Collections
			.synchronizedMap(new HashMap<ClassLoader, List<Class<?>>>());

	private void loadProxyConfigurarion() {
		Class<?> clazz = this.getClass();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		boolean loaded = true;

		if (cache.containsKey(classLoader)) {
			if (!cache.get(classLoader).contains(clazz)) {
				loaded = false;
			}

		} else {
			List<Class<?>> classes = Collections.synchronizedList(new ArrayList<Class<?>>());
			cache.put(classLoader, classes);
			loaded = false;
		}

		if (!loaded) {
			new ConfigurationLoader().load(this);
			cache.get(classLoader).add(clazz);
		}

		new ConfigurationLoader().load(this);
	}
}
