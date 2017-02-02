/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.fields;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.ReservedKeyWords;

/**
 * @author SERPRO
 *
 */
@RequestScoped
public class FieldHelper {

    private UriInfo uriInfo;

    private ResourceInfo resourceInfo;

    @Inject
    private DemoiselleRequestContext drc;

    public FieldHelper() {
    }

    public FieldHelper(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleRequestContext drc) {
        this.uriInfo = uriInfo;
        this.resourceInfo = resourceInfo;
        this.drc = drc;
    }

    public void execute(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;

        uriInfo.getQueryParameters().forEach((key, values) -> {
            if (ReservedKeyWords.DEFAULT_FIELD_KEY.getKey().equalsIgnoreCase(key)) {
                Set<String> paramValues = new HashSet<>();

                values.stream().forEach(value -> {
                    String[] paramValueSplit = value.split("\\,");
                    paramValues.addAll(Arrays.asList(paramValueSplit));
                });

                drc.getFields().addAll(paramValues);
            }
        });
    }

}
