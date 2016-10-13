/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.jpa.bootstrap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.persistence.Entity;

/**
 * 
 * Este processo evita que o CDI faça a gestão da entidade no lugar do EntityManager. Garantindo que o EntityManager faça a gestão.
 * 
 * http://www.cdi-spec.org/faq/
 * Why is @Vetoed a best practice for persistent (JPA) entities?
 * 
 * @author SERPRO
 *
 */
public class PersistenceBootstrap implements javax.enterprise.inject.spi.Extension {

	/**
	 * Adiciona Vetoed nas entidades
	 * 
	 * @param pat
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void processAnnotatedType(@Observes final ProcessAnnotatedType pat) {
		final AnnotatedType annotatedType = pat.getAnnotatedType();
		if (annotatedType.getJavaClass().isAnnotationPresent(Entity.class)) {
			pat.veto();
		}
	}

}
