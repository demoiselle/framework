/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.bootstrap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.persistence.Entity;

/**
 *
 * Adding the @Vetoed annotation to all persistent entities is considered a best
 * practice in most cases. The purpose of this annotation is to prevent the
 * BeanManager from managing an entity as a CDI Bean.
 *
 * http://www.cdi-spec.org/faq/ Why is @Vetoed a best practice for persistent
 * (JPA) entities?
 * 
 * @author SERPRO
 *
 */
public class PersistenceBootstrap implements javax.enterprise.inject.spi.Extension {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void processAnnotatedType(@Observes final ProcessAnnotatedType pat) {
		final AnnotatedType annotatedType = pat.getAnnotatedType();
		if (annotatedType.getJavaClass().isAnnotationPresent(Entity.class)) {
			pat.veto();
		}
	}

}
