package org.demoiselle.jee.openapi;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;

/**
 * {@link OASModelReader} that discovers {@link OpenAPIContributor} instances
 * via CDI and aggregates their contributions into the final OpenAPI document.
 *
 * <p>Path merging follows a "first wins" strategy: when two contributors
 * define the same path, the definition from the first contributor processed
 * is preserved and subsequent duplicates are silently ignored.</p>
 *
 * <p>If an individual contributor throws an exception, the error is logged
 * as a warning and processing continues with the remaining contributors.</p>
 */
public class DemoiselleOASModelReader implements OASModelReader {

    private static final Logger LOG = Logger.getLogger(DemoiselleOASModelReader.class.getName());

    @Override
    public OpenAPI buildModel() {
        OpenAPI merged = OASFactory.createOpenAPI();
        merged.paths(OASFactory.createPaths());

        if (!isEnabled()) {
            return merged;
        }

        CDI.current().select(OpenAPIContributor.class).stream()
                .forEach(contributor -> {
                    try {
                        OpenAPI partial = contributor.contribute();
                        mergePaths(merged, partial);
                    } catch (Exception e) {
                        LOG.log(Level.WARNING,
                                "Error processing OpenAPIContributor {0}: {1}",
                                new Object[]{contributor.getClass().getName(), e.getMessage()});
                    }
                });

        return merged;
    }

    /**
     * Checks whether OpenAPI documentation generation is enabled via
     * {@link OpenAPIConfig}. Returns {@code true} when the config bean
     * is not available (e.g. no CDI container) to preserve default behaviour.
     */
    private boolean isEnabled() {
        try {
            OpenAPIConfig config = CDI.current().select(OpenAPIConfig.class).get();
            return config.isEnabled();
        } catch (Exception e) {
            LOG.log(Level.FINE, "Could not resolve OpenAPIConfig, defaulting to enabled", e);
            return true;
        }
    }

    private void mergePaths(OpenAPI target, OpenAPI source) {
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
}
