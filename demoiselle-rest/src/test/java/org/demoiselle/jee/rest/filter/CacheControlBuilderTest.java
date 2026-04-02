/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.lang.annotation.Annotation;

import org.demoiselle.jee.rest.annotation.CacheControl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CacheControlBuilder} edge cases and specific
 * combinations.
 */
class CacheControlBuilderTest {

    // ---------------------------------------------------------------
    // Stub helper
    // ---------------------------------------------------------------

    private static CacheControl stub(String value, int maxAge, int sMaxAge,
                                     boolean noCache, boolean noStore,
                                     boolean mustRevalidate, boolean isPrivate) {
        return new CacheControl() {
            @Override public Class<? extends Annotation> annotationType() { return CacheControl.class; }
            @Override public String value()          { return value; }
            @Override public int maxAge()             { return maxAge; }
            @Override public int sMaxAge()            { return sMaxAge; }
            @Override public boolean noCache()        { return noCache; }
            @Override public boolean noStore()        { return noStore; }
            @Override public boolean mustRevalidate() { return mustRevalidate; }
            @Override public boolean isPrivate()      { return isPrivate; }
        };
    }

    /** Default annotation with no attributes produces "max-age=0". */
    @Test
    void noAttributesProducesDefaultMaxAgeZero() {
        CacheControl cc = stub("", -1, -1, false, false, false, false);
        assertEquals("max-age=0", CacheControlBuilder.build(cc));
    }

    /** maxAge=300 + noCache=true produces correct combined string. */
    @Test
    void maxAgeWithNoCacheProducesCorrectString() {
        CacheControl cc = stub("", 300, -1, true, false, false, false);
        String result = CacheControlBuilder.build(cc);
        assertEquals("max-age=300, no-cache, public", result);
    }

    /** All typed attributes set. */
    @Test
    void allTypedAttributesSet() {
        CacheControl cc = stub("", 3600, 1800, true, true, true, true);
        String result = CacheControlBuilder.build(cc);
        assertEquals("max-age=3600, s-maxage=1800, no-cache, no-store, must-revalidate, private", result);
    }

    /** All typed attributes set with isPrivate=false adds "public". */
    @Test
    void allTypedAttributesWithPublic() {
        CacheControl cc = stub("", 3600, 1800, true, true, true, false);
        String result = CacheControlBuilder.build(cc);
        assertEquals("max-age=3600, s-maxage=1800, no-cache, no-store, must-revalidate, public", result);
    }

    /** Legacy value() takes precedence over typed attributes. */
    @Test
    void legacyValueTakesPrecedence() {
        CacheControl cc = stub("no-store", 300, -1, true, false, false, false);
        assertEquals("no-store", CacheControlBuilder.build(cc));
    }

    /** Only isPrivate=true produces "private" (no "public"). */
    @Test
    void onlyPrivateProducesPrivateDirective() {
        CacheControl cc = stub("", -1, -1, false, false, false, true);
        assertEquals("private", CacheControlBuilder.build(cc));
    }

    /** maxAge=0 is a valid directive (zero is >= 0). */
    @Test
    void maxAgeZeroIsValid() {
        CacheControl cc = stub("", 0, -1, false, false, false, false);
        assertEquals("max-age=0, public", CacheControlBuilder.build(cc));
    }

    /** sMaxAge alone produces "s-maxage=<val>, public". */
    @Test
    void sMaxAgeAloneProducesPublic() {
        CacheControl cc = stub("", -1, 600, false, false, false, false);
        assertEquals("s-maxage=600, public", CacheControlBuilder.build(cc));
    }

    /** buildFromTypedAttributes is directly testable (package-private). */
    @Test
    void buildFromTypedAttributesDirectly() {
        CacheControl cc = stub("ignored-value", 120, -1, false, true, false, false);
        // buildFromTypedAttributes ignores value()
        assertEquals("max-age=120, no-store, public",
                CacheControlBuilder.buildFromTypedAttributes(cc));
    }

    /** noStore alone produces "no-store, public". */
    @Test
    void noStoreAloneProducesPublic() {
        CacheControl cc = stub("", -1, -1, false, true, false, false);
        assertEquals("no-store, public", CacheControlBuilder.build(cc));
    }

    /** mustRevalidate alone produces "must-revalidate, public". */
    @Test
    void mustRevalidateAloneProducesPublic() {
        CacheControl cc = stub("", -1, -1, false, false, true, false);
        assertEquals("must-revalidate, public", CacheControlBuilder.build(cc));
    }
}
