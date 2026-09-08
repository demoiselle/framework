/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import java.util.Arrays;
import java.util.Objects;

/**
 * Structured cache key that avoids relying solely on a numeric hash to identify
 * cached entries. A key is composed of three parts:
 *
 * <ul>
 *   <li>{@code namespace} — the owning entity's fully qualified class name, used
 *       for bulk invalidation ({@link CacheBackend#invalidateNamespace(String)});</li>
 *   <li>{@code discriminator} — the method signature (or another logical operation
 *       identifier);</li>
 *   <li>{@code paramsSignature} — a stable textual signature of the parameters,
 *       combined with their {@code deepHashCode} so that distinct parameter sets
 *       that happen to share a hash still yield distinct keys (collision-safe).</li>
 * </ul>
 *
 * <p>The canonical string form is
 * {@code namespace|discriminator|paramsSignature} where {@code paramsSignature}
 * embeds both the deep hash and a rendered form of the arguments.</p>
 *
 * @param namespace       the entity namespace (never {@code null})
 * @param discriminator   the operation discriminator (never {@code null})
 * @param paramsSignature the structured parameter signature (never {@code null})
 *
 * @author SERPRO
 */
public record CacheKey(String namespace, String discriminator, String paramsSignature) {

    private static final char SEP = '|';

    public CacheKey {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(discriminator, "discriminator");
        Objects.requireNonNull(paramsSignature, "paramsSignature");
    }

    /**
     * Builds a key from an entity namespace, a discriminator and the raw
     * parameter array. The parameter signature embeds the deep hash and a
     * bounded textual rendering to minimize collisions.
     *
     * @param namespace     the entity namespace
     * @param discriminator the operation discriminator
     * @param params        the method parameters (may be {@code null})
     * @return a structured cache key
     */
    public static CacheKey of(String namespace, String discriminator, Object[] params) {
        return new CacheKey(namespace, discriminator, signatureOf(params));
    }

    private static String signatureOf(Object[] params) {
        if (params == null || params.length == 0) {
            return "0:[]";
        }
        int deepHash = Arrays.deepHashCode(params);
        StringBuilder rendered = new StringBuilder();
        rendered.append(params.length).append('#');
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                rendered.append(',');
            }
            rendered.append(render(params[i]));
        }
        return deepHash + ":[" + rendered + "]";
    }

    private static String render(Object value) {
        if (value == null) {
            return "null";
        }
        Class<?> type = value.getClass();
        String text;
        if (type.isArray()) {
            text = arrayToString(value);
        } else {
            text = String.valueOf(value);
        }
        // Prefix with the type to further reduce cross-type collisions
        // (e.g. the string "1" vs the integer 1).
        return type.getName() + "=" + text;
    }

    private static String arrayToString(Object array) {
        if (array instanceof Object[] oa) {
            return Arrays.deepToString(oa);
        }
        if (array instanceof int[] a) { return Arrays.toString(a); }
        if (array instanceof long[] a) { return Arrays.toString(a); }
        if (array instanceof short[] a) { return Arrays.toString(a); }
        if (array instanceof byte[] a) { return Arrays.toString(a); }
        if (array instanceof char[] a) { return Arrays.toString(a); }
        if (array instanceof boolean[] a) { return Arrays.toString(a); }
        if (array instanceof float[] a) { return Arrays.toString(a); }
        if (array instanceof double[] a) { return Arrays.toString(a); }
        return String.valueOf(array);
    }

    /**
     * @return the canonical, backend-agnostic string form of this key
     */
    public String asString() {
        return namespace + SEP + discriminator + SEP + paramsSignature;
    }
}
