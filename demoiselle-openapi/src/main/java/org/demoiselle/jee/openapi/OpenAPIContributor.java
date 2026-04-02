package org.demoiselle.jee.openapi;

import org.eclipse.microprofile.openapi.models.OpenAPI;

/**
 * Interface that framework modules implement to contribute
 * partial OpenAPI definitions to the final document.
 *
 * <p>Each contributor returns a partial {@link OpenAPI} object containing
 * paths, schemas, or other definitions relevant to its module. The
 * {@link DemoiselleOASModelReader} discovers all contributors via CDI
 * and merges them into the final OpenAPI document.</p>
 *
 * <p>When multiple contributors define the same path, the first one
 * processed takes precedence (no overwriting).</p>
 */
public interface OpenAPIContributor {

    /**
     * Contributes partial OpenAPI definitions to the final document.
     *
     * @return a partial OpenAPI document with paths, schemas, etc.
     */
    OpenAPI contribute();
}
