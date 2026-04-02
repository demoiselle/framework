/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.BaseConfiguration;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationMapValueExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests verifying that the Pattern cache in {@link ConfigurationMapValueExtractor}
 * correctly reuses compiled Patterns for identical parameters and creates new entries
 * for different parameters.
 *
 * <p><b>Validates: Requirements 3.2, 3.3</b></p>
 */
class PatternCacheHitTest {

    /** Dummy class that holds a Map field for the extractor. */
    static class MapHolder {
        Map<String, String> data;
    }

    private static final Field MAP_FIELD;

    static {
        try {
            MAP_FIELD = MapHolder.class.getDeclaredField("data");
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ConfigurationMapValueExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new ConfigurationMapValueExtractor();
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, Pattern> getPatternCache() throws Exception {
        Field cacheField = ConfigurationMapValueExtractor.class.getDeclaredField("patternCache");
        cacheField.setAccessible(true);
        return (ConcurrentHashMap<String, Pattern>) cacheField.get(extractor);
    }

    @Test
    void singleCallShouldCacheExactlyOnePattern() throws Exception {
        BaseConfiguration config = new BaseConfiguration();
        config.setProperty("abc.myKey.sub1", "value1");

        extractor.getValue("abc\\.", "myKey", MAP_FIELD, config);

        ConcurrentHashMap<String, Pattern> cache = getPatternCache();
        assertEquals(1, cache.size(), "Cache should contain exactly 1 entry after first call");
    }

    @Test
    void repeatedCallWithSameParamsShouldReuseCache() throws Exception {
        BaseConfiguration config = new BaseConfiguration();
        config.setProperty("abc.myKey.sub1", "value1");

        extractor.getValue("abc\\.", "myKey", MAP_FIELD, config);
        ConcurrentHashMap<String, Pattern> cache = getPatternCache();
        assertEquals(1, cache.size(), "Cache should contain 1 entry after first call");

        // Capture the Pattern instance from the first call
        Pattern firstPattern = cache.values().iterator().next();

        extractor.getValue("abc\\.", "myKey", MAP_FIELD, config);
        assertEquals(1, cache.size(), "Cache should still contain exactly 1 entry after second call (cache hit)");

        // Verify it's the exact same Pattern object (identity check)
        Pattern secondPattern = cache.values().iterator().next();
        assertSame(firstPattern, secondPattern, "Same Pattern instance should be reused");
    }

    @Test
    void differentParamsShouldCreateNewCacheEntry() throws Exception {
        BaseConfiguration config = new BaseConfiguration();
        config.setProperty("abc.myKey.sub1", "value1");
        config.setProperty("xyz.otherKey.sub2", "value2");

        extractor.getValue("abc\\.", "myKey", MAP_FIELD, config);
        ConcurrentHashMap<String, Pattern> cache = getPatternCache();
        assertEquals(1, cache.size(), "Cache should contain 1 entry after first call");

        extractor.getValue("xyz\\.", "otherKey", MAP_FIELD, config);
        assertEquals(2, cache.size(), "Cache should contain 2 entries after call with different params");
    }
}
