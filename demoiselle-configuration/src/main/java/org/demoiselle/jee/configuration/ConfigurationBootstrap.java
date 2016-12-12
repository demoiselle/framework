/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;

/**
 * 
 * Class responsible for loading all extractors classes that implement the {@link ConfigurationValueExtractor} interface.
 * 
 *
 */
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
