/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Represents a permission as a pair of resource and operation.
 * Used exclusively as a member of {@link RequiredAllPermissions}.
 * </p>
 *
 * @see RequiredAllPermissions
 * @author SERPRO
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Permission {

    String resource();

    String operation();
}
