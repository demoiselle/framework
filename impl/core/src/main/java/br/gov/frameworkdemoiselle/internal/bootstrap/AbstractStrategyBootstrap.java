package br.gov.frameworkdemoiselle.internal.bootstrap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.util.Reflections;

public abstract class AbstractStrategyBootstrap<I> implements Extension {

	private Class<? extends I> strategyClass;

	private Collection<Class<? extends I>> cache;

	protected abstract Logger getLogger();

	protected Class<? extends I> getStrategyClass() {
		if (this.strategyClass == null) {
			this.strategyClass = Reflections.getGenericTypeArgument(this.getClass(), 0);
		}

		return this.strategyClass;
	}

	public Collection<Class<? extends I>> getCache() {
		if (this.cache == null) {
			this.cache = Collections.synchronizedSet(new HashSet<Class<? extends I>>());
		}

		return this.cache;
	}

	@SuppressWarnings("unchecked")
	public <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> event) {
		final AnnotatedType<T> annotatedType = event.getAnnotatedType();

		if (Reflections.isOfType(annotatedType.getJavaClass(), this.getStrategyClass())) {
			this.getCache().add((Class<I>) annotatedType.getJavaClass());
		}
	}
}
