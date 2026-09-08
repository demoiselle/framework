/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.openapi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.junit.jupiter.api.Test;

class OpenAPIProductionCoverageTest {

    @Test
    void configurationDefaultsToEnabledAndCanBeDisabled() {
        OpenAPIConfig config = new OpenAPIConfig();
        assertTrue(config.isEnabled());

        config.setEnabled(false);
        assertFalse(config.isEnabled());
    }

    @Test
    void mergePathsIgnoresNullAndPreservesFirstContributor() throws Exception {
        DemoiselleOASModelReader reader = new DemoiselleOASModelReader();
        Method merge = DemoiselleOASModelReader.class
                .getDeclaredMethod("mergePaths", OpenAPI.class, OpenAPI.class);
        merge.setAccessible(true);

        OpenAPI target = OASFactory.createOpenAPI()
                .paths(OASFactory.createPaths());
        merge.invoke(reader, target, null);

        PathItem first = OASFactory.createPathItem();
        OpenAPI source = OASFactory.createOpenAPI().paths(
                OASFactory.createPaths().addPathItem("/orders", first));
        merge.invoke(reader, target, source);
        assertSame(first, target.getPaths().getPathItem("/orders"));

        PathItem duplicate = OASFactory.createPathItem();
        OpenAPI second = OASFactory.createOpenAPI().paths(
                OASFactory.createPaths().addPathItem("/orders", duplicate));
        merge.invoke(reader, target, second);
        assertSame(first, target.getPaths().getPathItem("/orders"),
                "the first contributor must win");
    }
}
