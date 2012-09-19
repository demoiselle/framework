package br.gov.frameworkdemoiselle.internal.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.util.Beans;

public class ConfigurationImpl {

	private static Map<ClassLoader, List<Class<?>>> cache;
	
	private boolean loaded = false;

	private synchronized static Map<ClassLoader, List<Class<?>>> getCache() {
		if (cache == null) {
			cache = Collections.synchronizedMap(new HashMap<ClassLoader, List<Class<?>>>());
		}

		return cache;
	}

	@SuppressWarnings("unused")
	private synchronized void loadProxyConfigurarion(Object instance) {
		if (!loaded) {
			Beans.getReference(ConfigurationLoader.class).load(instance);
			loaded = true;
		}
	}
	
	@SuppressWarnings("unused")
	private synchronized static void loadProxyConfigurarionX(Object instance) {
		Class<?> clazz = instance.getClass().getSuperclass();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		boolean loaded = true;

		if (getCache().containsKey(classLoader)) {
			if (!getCache().get(classLoader).contains(clazz)) {
				loaded = false;
			}

		} else {
			List<Class<?>> classes = Collections.synchronizedList(new ArrayList<Class<?>>());
			getCache().put(classLoader, classes);
			loaded = false;
		}

		if (!loaded) {
//			new br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader().load(instance);
			Beans.getReference(ConfigurationLoader.class).load(instance);
			getCache().get(classLoader).add(clazz);
		}
	}
}
