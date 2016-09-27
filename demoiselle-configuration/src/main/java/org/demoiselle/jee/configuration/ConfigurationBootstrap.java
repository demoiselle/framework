package org.demoiselle.jee.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;

public class ConfigurationBootstrap implements Extension {
	
	private Collection<Class<? extends ConfigurationValueExtractor>> cache;

	public Collection<Class<? extends ConfigurationValueExtractor>> getCache() {
		if (this.cache == null) {
			this.cache = Collections.synchronizedSet(new HashSet<Class<? extends ConfigurationValueExtractor>>());
		}

		return this.cache;
	}
	
	public void processAnnotatedType(@Observes final ProcessAnnotatedType<? extends ConfigurationValueExtractor> pat) {
		
		Class<? extends ConfigurationValueExtractor> pcsClass = pat.getAnnotatedType().getJavaClass();
		
		if (pcsClass.isAnnotation() || pcsClass.isInterface()  
				|| pcsClass.isSynthetic() || pcsClass.isArray()
				|| pcsClass.isEnum()){
	            return;
	    }
		
		this.getCache().add(pat.getAnnotatedType().getJavaClass());
	}

}
