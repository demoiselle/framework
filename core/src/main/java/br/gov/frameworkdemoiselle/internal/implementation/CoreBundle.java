package br.gov.frameworkdemoiselle.internal.implementation;

import javax.inject.Inject;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public class CoreBundle {

	@Inject
	@Name("demoiselle-core-bundle")
	private ResourceBundle bundle;

	private static CoreBundle instance;

	private static synchronized CoreBundle getInstance() {
		if (instance == null) {
			instance = Beans.getReference(CoreBundle.class);
		}

		return instance;
	}

	public static ResourceBundle get() {
		return getInstance().bundle;
	}
}
