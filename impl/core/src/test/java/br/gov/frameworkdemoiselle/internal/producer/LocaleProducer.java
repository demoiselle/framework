package br.gov.frameworkdemoiselle.internal.producer;

import java.util.Locale;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

public class LocaleProducer {

	@Default
	@Produces
	public Locale create() {
		return Locale.getDefault();
	}
}
