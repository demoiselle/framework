/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.openapi;

// Feature: cross-cutting-improvements, Property 5: Tolerância a falhas na agregação OpenAPI

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jqwik.api.*;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for fault-tolerant OpenAPI contributor aggregation.
 *
 * <p><b>Validates: Requirements 5.4</b></p>
 *
 * <p>Property 5: For any set of contributors where a subset throws exceptions,
 * paths from healthy contributors are present in the result and no exception
 * propagates to the caller.</p>
 *
 * <p>Tests the fault-tolerant merge logic (same algorithm as
 * {@link DemoiselleOASModelReader#buildModel()}) without requiring CDI.</p>
 */
class FaultTolerantAggregationPropertyTest {

    private static final Logger LOG = Logger.getLogger(
            FaultTolerantAggregationPropertyTest.class.getName());

    /**
     * Replicates the fault-tolerant aggregation logic from
     * {@link DemoiselleOASModelReader#buildModel()} — each contributor is
     * invoked inside a try/catch so that a failing contributor does not
     * prevent healthy ones from being merged.
     */
    static OpenAPI aggregateWithFaultTolerance(List<OpenAPIContributor> contributors) {
        OpenAPI merged = OASFactory.createOpenAPI();
        merged.paths(OASFactory.createPaths());

        for (OpenAPIContributor contributor : contributors) {
            try {
                OpenAPI partial = contributor.contribute();
                mergePaths(merged, partial);
            } catch (Exception e) {
                LOG.log(Level.WARNING,
                        "Error processing OpenAPIContributor: {0}",
                        e.getMessage());
            }
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

    // ---- Property: healthy paths present, no exception propagates ----

    @Property(tries = 100)
    @Tag("cross-cutting-improvements")
    @Tag("property-5-fault-tolerant-aggregation")
    void healthyContributorPathsPresentDespiteFaultyOnes(
            @ForAll("mixedContributors") MixedScenario scenario) {

        // The aggregation must never throw, even when faulty contributors exist
        OpenAPI merged = assertDoesNotThrow(
                () -> aggregateWithFaultTolerance(scenario.contributors),
                "Aggregation must not propagate exceptions from faulty contributors");

        // All paths from healthy contributors must be present
        for (String healthyPath : scenario.healthyPaths) {
            assertTrue(merged.getPaths().hasPathItem(healthyPath),
                    "Merged document must contain healthy path: " + healthyPath);
        }
    }

    // ---- Generators ----

    @Provide
    Arbitrary<MixedScenario> mixedContributors() {
        Arbitrary<Integer> healthyCount = Arbitraries.integers().between(0, 6);
        Arbitrary<Integer> faultyCount = Arbitraries.integers().between(1, 5);

        return Combinators.combine(healthyCount, faultyCount).flatAs((hc, fc) ->
                pathNames().list().ofSize(hc).uniqueElements().map(healthyPaths -> {
                    List<OpenAPIContributor> contributors = new ArrayList<>();
                    List<String> allHealthyPaths = new ArrayList<>(healthyPaths);

                    // Add healthy contributors
                    for (String path : healthyPaths) {
                        contributors.add(() -> {
                            OpenAPI partial = OASFactory.createOpenAPI();
                            Paths p = OASFactory.createPaths();
                            p.addPathItem(path, OASFactory.createPathItem());
                            partial.paths(p);
                            return partial;
                        });
                    }

                    // Add faulty contributors that throw RuntimeException
                    for (int i = 0; i < fc; i++) {
                        final int idx = i;
                        contributors.add(() -> {
                            throw new RuntimeException(
                                    "Simulated failure from contributor #" + idx);
                        });
                    }

                    // Interleave: put a faulty contributor at the beginning
                    // to ensure fault tolerance works regardless of order
                    if (!contributors.isEmpty() && fc > 0) {
                        OpenAPIContributor firstFaulty = contributors.remove(
                                contributors.size() - 1);
                        contributors.add(0, firstFaulty);
                    }

                    return new MixedScenario(contributors, allHealthyPaths);
                })
        );
    }

    Arbitrary<String> pathNames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20)
                .map(s -> "/" + s.toLowerCase());
    }

    // ---- Helper class for mixed scenarios ----

    static class MixedScenario {
        final List<OpenAPIContributor> contributors;
        final List<String> healthyPaths;

        MixedScenario(List<OpenAPIContributor> contributors,
                       List<String> healthyPaths) {
            this.contributors = contributors;
            this.healthyPaths = healthyPaths;
        }

        @Override
        public String toString() {
            return "MixedScenario{healthy=" + healthyPaths.size()
                    + ", total=" + contributors.size() + "}";
        }
    }
}
