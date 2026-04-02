/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.openapi;

// Feature: cross-cutting-improvements, Property 4: Agregação de OpenAPIContributors preserva paths

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jqwik.api.*;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for OpenAPIContributor aggregation path preservation.
 *
 * <p><b>Validates: Requirements 5.2, 5.3</b></p>
 *
 * <p>Property 4: For any set of contributors with distinct paths, the final
 * document contains all paths. When paths overlap, the first contributor's
 * definition is preserved (first-wins strategy).</p>
 *
 * <p>Tests the merge logic directly (same algorithm as
 * {@link DemoiselleOASModelReader}) without requiring CDI.</p>
 */
class OpenAPIContributorAggregationPropertyTest {

    private static final Logger LOG = Logger.getLogger(
            OpenAPIContributorAggregationPropertyTest.class.getName());

    /**
     * Replicates the merge logic from {@link DemoiselleOASModelReader#mergePaths}
     * so we can test it without CDI.
     */
    static OpenAPI mergeContributions(List<OpenAPI> contributions) {
        OpenAPI merged = OASFactory.createOpenAPI();
        merged.paths(OASFactory.createPaths());

        for (OpenAPI partial : contributions) {
            mergePaths(merged, partial);
        }
        return merged;
    }

    private static void mergePaths(OpenAPI target, OpenAPI source) {
        if (source == null || source.getPaths() == null) {
            return;
        }

        Paths targetPaths = target.getPaths();
        Map<String, PathItem> sourceItems = source.getPaths().getPathItems();

        if (sourceItems == null) {
            return;
        }

        sourceItems.forEach((path, item) -> {
            if (!targetPaths.hasPathItem(path)) {
                targetPaths.addPathItem(path, item);
            }
        });
    }

    // ---- Property: distinct paths are all preserved ----

    @Property(tries = 100)
    @Tag("cross-cutting-improvements")
    @Tag("property-4-aggregation-preserves-paths")
    void allDistinctPathsArePreservedInMergedDocument(
            @ForAll("distinctContributors") List<OpenAPI> contributors) {

        OpenAPI merged = mergeContributions(contributors);

        // Collect all expected paths from all contributors
        for (OpenAPI contributor : contributors) {
            if (contributor.getPaths() != null
                    && contributor.getPaths().getPathItems() != null) {
                for (String path : contributor.getPaths().getPathItems().keySet()) {
                    assertTrue(merged.getPaths().hasPathItem(path),
                            "Merged document must contain path: " + path);
                }
            }
        }
    }

    // ---- Property: on overlap, first contributor wins ----

    @Property(tries = 100)
    @Tag("cross-cutting-improvements")
    @Tag("property-4-first-wins-on-overlap")
    void firstContributorWinsOnOverlappingPaths(
            @ForAll("overlappingContributors") OverlapScenario scenario) {

        OpenAPI merged = mergeContributions(scenario.contributors);

        // The merged path item for the shared path must be the one from the first contributor
        PathItem mergedItem = merged.getPaths().getPathItems().get(scenario.sharedPath);
        assertNotNull(mergedItem, "Shared path must exist in merged document");

        // Verify it's the first contributor's item (identified by its description)
        assertSame(scenario.firstPathItem, mergedItem,
                "On overlapping paths, the first contributor's definition must be preserved");
    }

    // ---- Generators ----

    @Provide
    Arbitrary<List<OpenAPI>> distinctContributors() {
        return Arbitraries.integers().between(1, 10).flatMap(count ->
                pathNames().list().ofSize(count).uniqueElements().map(paths -> {
                    List<OpenAPI> contributors = new ArrayList<>();
                    for (String path : paths) {
                        OpenAPI partial = OASFactory.createOpenAPI();
                        Paths p = OASFactory.createPaths();
                        p.addPathItem(path, OASFactory.createPathItem());
                        partial.paths(p);
                        contributors.add(partial);
                    }
                    return contributors;
                })
        );
    }

    @Provide
    Arbitrary<OverlapScenario> overlappingContributors() {
        return Combinators.combine(
                pathNames(),
                Arbitraries.integers().between(0, 5),
                Arbitraries.integers().between(0, 5)
        ).as((sharedPath, extraBefore, extraAfter) -> {
            // First contributor has the shared path + some unique paths
            PathItem firstItem = OASFactory.createPathItem()
                    .description("first-contributor");
            OpenAPI first = OASFactory.createOpenAPI();
            Paths firstPaths = OASFactory.createPaths();
            firstPaths.addPathItem(sharedPath, firstItem);
            for (int i = 0; i < extraBefore; i++) {
                firstPaths.addPathItem(sharedPath + "/extra-a-" + i,
                        OASFactory.createPathItem());
            }
            first.paths(firstPaths);

            // Second contributor also has the shared path (should be ignored)
            PathItem secondItem = OASFactory.createPathItem()
                    .description("second-contributor");
            OpenAPI second = OASFactory.createOpenAPI();
            Paths secondPaths = OASFactory.createPaths();
            secondPaths.addPathItem(sharedPath, secondItem);
            for (int i = 0; i < extraAfter; i++) {
                secondPaths.addPathItem(sharedPath + "/extra-b-" + i,
                        OASFactory.createPathItem());
            }
            second.paths(secondPaths);

            List<OpenAPI> contributors = new ArrayList<>();
            contributors.add(first);
            contributors.add(second);

            return new OverlapScenario(contributors, sharedPath, firstItem);
        });
    }

    Arbitrary<String> pathNames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20)
                .map(s -> "/" + s.toLowerCase());
    }

    // ---- Helper record for overlap scenarios ----

    static class OverlapScenario {
        final List<OpenAPI> contributors;
        final String sharedPath;
        final PathItem firstPathItem;

        OverlapScenario(List<OpenAPI> contributors, String sharedPath,
                        PathItem firstPathItem) {
            this.contributors = contributors;
            this.sharedPath = sharedPath;
            this.firstPathItem = firstPathItem;
        }

        @Override
        public String toString() {
            return "OverlapScenario{sharedPath='" + sharedPath + "'}";
        }
    }
}
