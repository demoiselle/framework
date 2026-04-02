/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.jqwik.api.*;

import org.demoiselle.jee.crud.CrudFilter;
import org.demoiselle.jee.crud.ReflectionCache;
import org.demoiselle.jee.crud.TreeNodeField;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jee-migration-v4, Property 11: Projeção de campos para profundidade ≥ 3 níveis
 *
 * Validates: Requirements 12.1, 12.2
 *
 * For objects with N levels (N ≥ 3), field projection should resolve correctly.
 * Given a tree specification like fields=level1.level2.level3, the recursive
 * resolveFields method should return the correct nested value.
 */
class FieldProjectionPropertyTest {

    // ---- 4-level deep model ----
    public static class Level4 {
        private String deepValue;

        public Level4(String deepValue) { this.deepValue = deepValue; }
        public Level4() {}
    }

    public static class Level3 {
        private String l3Name;
        private Level4 level4;

        public Level3(String l3Name, Level4 level4) {
            this.l3Name = l3Name;
            this.level4 = level4;
        }
        public Level3() {}
    }

    public static class Level2 {
        private String l2Name;
        private Level3 level3;

        public Level2(String l2Name, Level3 level3) {
            this.l2Name = l2Name;
            this.level3 = level3;
        }
        public Level2() {}
    }

    public static class Level1 {
        private String l1Name;
        private Level2 level2;

        public Level1(String l1Name, Level2 level2) {
            this.l1Name = l1Name;
            this.level2 = level2;
        }
        public Level1() {}
    }

    @Provide
    Arbitrary<String> leafValues() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }

    @Provide
    Arbitrary<String> l3Names() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> l2Names() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> l1Names() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }

    /**
     * P11: For a 4-level deep object, projecting fields=level2.level3.level4.deepValue
     * should resolve the leaf value correctly (depth ≥ 3).
     */
    @SuppressWarnings("unchecked")
    @Property(tries = 100)
    @Tag("Feature_jee-migration-v4_Property-11_field-projection-depth-3-plus")
    void projectionShouldResolveFieldsAtDepth4(
            @ForAll("l1Names") String l1Name,
            @ForAll("l2Names") String l2Name,
            @ForAll("l3Names") String l3Name,
            @ForAll("leafValues") String deepValue) {

        // Build a 4-level deep object
        Level4 l4 = new Level4(deepValue);
        Level3 l3 = new Level3(l3Name, l4);
        Level2 l2 = new Level2(l2Name, l3);
        Level1 root = new Level1(l1Name, l2);

        // Build tree: level2 -> level3 -> level4 -> deepValue
        TreeNodeField<String, Set<String>> rootNode = new TreeNodeField<>("root", new HashSet<>());
        TreeNodeField<String, Set<String>> level2Node = rootNode.addChild("level2", new HashSet<>());
        TreeNodeField<String, Set<String>> level3Node = level2Node.addChild("level3", new HashSet<>());
        TreeNodeField<String, Set<String>> level4Node = level3Node.addChild("level4", new HashSet<>());
        level4Node.addChild("deepValue", new HashSet<>());

        ReflectionCache reflectionCache = new ReflectionCache();
        CrudFilter filter = new CrudFilter(null, null, null, null, null, null, null, reflectionCache, null);

        Map<String, Object> result = filter.resolveFields(root, rootNode, Level1.class, 10);

        // Verify 3+ levels of nesting resolved correctly
        assertNotNull(result.get("level2"), "level2 should be present in projection");
        Map<String, Object> l2Map = (Map<String, Object>) result.get("level2");

        assertNotNull(l2Map.get("level3"), "level3 should be present in projection");
        Map<String, Object> l3Map = (Map<String, Object>) l2Map.get("level3");

        assertNotNull(l3Map.get("level4"), "level4 should be present in projection");
        Map<String, Object> l4Map = (Map<String, Object>) l3Map.get("level4");

        assertEquals(deepValue, l4Map.get("deepValue"),
                "deepValue at depth 4 should match the original value");
    }

    /**
     * P11 (supplementary): Projecting exactly 3 levels deep should also work.
     */
    @SuppressWarnings("unchecked")
    @Property(tries = 100)
    @Tag("Feature_jee-migration-v4_Property-11_field-projection-depth-3-plus")
    void projectionShouldResolveFieldsAtExactlyDepth3(
            @ForAll("l2Names") String l2Name,
            @ForAll("l3Names") String l3Name,
            @ForAll("leafValues") String deepValue) {

        Level4 l4 = new Level4(deepValue);
        Level3 l3 = new Level3(l3Name, l4);
        Level2 l2 = new Level2(l2Name, l3);
        Level1 root = new Level1("root", l2);

        // Tree: level2 -> level3 -> l3Name (leaf at depth 3)
        TreeNodeField<String, Set<String>> rootNode = new TreeNodeField<>("root", new HashSet<>());
        TreeNodeField<String, Set<String>> level2Node = rootNode.addChild("level2", new HashSet<>());
        TreeNodeField<String, Set<String>> level3Node = level2Node.addChild("level3", new HashSet<>());
        level3Node.addChild("l3Name", new HashSet<>());

        ReflectionCache reflectionCache = new ReflectionCache();
        CrudFilter filter = new CrudFilter(null, null, null, null, null, null, null, reflectionCache, null);

        Map<String, Object> result = filter.resolveFields(root, rootNode, Level1.class, 10);

        Map<String, Object> l2Map = (Map<String, Object>) result.get("level2");
        assertNotNull(l2Map, "level2 should be present");

        Map<String, Object> l3Map = (Map<String, Object>) l2Map.get("level3");
        assertNotNull(l3Map, "level3 should be present");

        assertEquals(l3Name, l3Map.get("l3Name"),
                "l3Name at depth 3 should match the original value");
    }
}
