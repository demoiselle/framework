/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.util.ArrayList;
import java.util.List;

import org.demoiselle.jee.rest.annotation.CacheControl;

/**
 * Utility class that builds the {@code Cache-Control} header string from a
 * {@link CacheControl} annotation. When the annotation's {@code value()} is
 * non-empty the literal value is returned (legacy mode). Otherwise the typed
 * attributes are assembled into a standards-compliant directive string.
 *
 * @author SERPRO
 */
public final class CacheControlBuilder {

    private CacheControlBuilder() {
    }

    /**
     * Builds the Cache-Control header value from the given annotation.
     * If {@code value()} is non-empty it takes absolute precedence over
     * typed attributes.
     *
     * @param annotation the CacheControl annotation
     * @return the Cache-Control header string
     */
    public static String build(CacheControl annotation) {
        if (annotation.value() != null && !annotation.value().isEmpty()) {
            return annotation.value();
        }
        return buildFromTypedAttributes(annotation);
    }

    /**
     * Builds the Cache-Control header value exclusively from typed attributes,
     * ignoring {@code value()}.
     *
     * @param annotation the CacheControl annotation
     * @return the Cache-Control header string
     */
    static String buildFromTypedAttributes(CacheControl annotation) {
        List<String> directives = new ArrayList<>();
        if (annotation.maxAge() >= 0) {
            directives.add("max-age=" + annotation.maxAge());
        }
        if (annotation.sMaxAge() >= 0) {
            directives.add("s-maxage=" + annotation.sMaxAge());
        }
        if (annotation.noCache()) {
            directives.add("no-cache");
        }
        if (annotation.noStore()) {
            directives.add("no-store");
        }
        if (annotation.mustRevalidate()) {
            directives.add("must-revalidate");
        }
        if (annotation.isPrivate()) {
            directives.add("private");
        } else if (!directives.isEmpty()) {
            directives.add("public");
        }
        return directives.isEmpty() ? "max-age=0" : String.join(", ", directives);
    }
}
