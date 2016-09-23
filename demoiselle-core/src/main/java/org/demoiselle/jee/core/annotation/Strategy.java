/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.annotation;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 *
 * <p>
 * This literal marks a bean to be selected at runtime based on a priority system.
 * The user qualifies the injection point with this literal and then at runtime
 * the CDI engine will circle through all candidate subtypes to be injected
 * that are annotated with {@link Priority}. If there is only one subtype with the
 * highest priority then this one will be selected to be injected.
 * </p>
 *
 * <p>
 * This allows users to plug in libraries with new candidates and have them be selected
 * if their priority values are higher than the default values already present. One example
 * is the {@link org.demoiselle.security.Authorizer} type, the framework has a {@link org.demoiselle.internal.implementation.DefaultAuthorizer}
 * with {@link Priority#L1_PRIORITY the lowest priority} but the user can add libraries with new
 * implementations of {@link org.demoiselle.security.Authorizer} annotated with higher priorities, the code will
 * then automatically select these new implementations with no extra configuration.
 * </p>
 *
 * <p>
 * This annotation must be used with supported types. Usually this involves creating {@link javax.enterprise.inject.Produces} CDI
 * producer methods that will select the correct strategy. To create your own producer
 * methods that support strategy selection, use the utility {@linkplain org.demoiselle.internal.producer.StrategySelector}.
 * </p>
 *
 * @author SERPRO
 */
@Qualifier
@Inherited
@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD, PARAMETER })
public @interface Strategy {

}
