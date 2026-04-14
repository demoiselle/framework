/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.message;

import java.util.Locale;

public interface MessageBundleResolver {

    <T> T resolve(Class<T> bundleType);

    <T> T resolve(Class<T> bundleType, Locale locale);
}
