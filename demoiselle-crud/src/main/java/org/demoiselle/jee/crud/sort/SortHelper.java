/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.sort;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.AbstractDAO;
import org.demoiselle.jee.crud.CrudMessage;
import org.demoiselle.jee.crud.CrudUtilHelper;
import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.ReservedKeyWords;
import org.demoiselle.jee.crud.Search;

/**
 * Class responsible for managing the 'sort' parameter comes from Url Query
 * String.
 *
 * Ex:
 *
 * Given a request
 * <pre>
 * GET {@literal http://localhost:8080/api/users?sort=field1,field2&desc=field2}
 * </pre>
 *
 * This class will processing the request above and parse the 'sort=...' and
 * 'desc=...' parameters to list of {@link SortModel} objects.
 *
 * This list will be use on {@link AbstractDAO} class to execute the sort on
 * database.
 *
 * @author SERPRO
 */
@RequestScoped
public class SortHelper {

    private ResourceInfo resourceInfo;

    private UriInfo uriInfo;

    @Inject
    private DemoiselleRequestContext drc;

    @Inject
    private SortHelperMessage sortHelperMessage;

    @Inject
    private CrudMessage crudMessage;

    public SortHelper() {
    }

    public SortHelper(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleRequestContext drc, SortHelperMessage sortHelperMessage, CrudMessage crudMessage) {
        this.resourceInfo = resourceInfo;
        this.uriInfo = uriInfo;
        this.drc = drc;
        this.sortHelperMessage = sortHelperMessage;
        this.crudMessage = crudMessage;
    }

    /**
     * Open the request query string to extract values from 'sort' and 'desc'
     * parameters and fill the {@link DemoiselleRequestContext#setSorts(List)}
     *
     * @param resourceInfo ResourceInfo
     * @param uriInfo UriInfo
     */
    public void execute(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;

        Set<String> descList = new LinkedHashSet<>();
        Boolean descAll = Boolean.FALSE;

        List<String> descValues = getValuesFromQueryString(ReservedKeyWords.DEFAULT_SORT_DESC_KEY.getKey());
        List<String> sortValues = getValuesFromQueryString(ReservedKeyWords.DEFAULT_SORT_KEY.getKey());

        // 'desc' parameter was filled and 'sort' parameter not
        if (descValues != null && sortValues == null) {
            throw new IllegalArgumentException(sortHelperMessage.descParameterWithoutSortParameter());
        }

        if (descValues != null) {

            //&desc without parameters
            if (descValues.isEmpty()) {
                descAll = Boolean.TRUE;
            } else {
                for (String value : descValues) {
                    //&desc=a,b,c
                    if (!value.isEmpty()) {
                        if (!sortValues.contains(value)) {
                            throw new BadRequestException(crudMessage.fieldRequestDoesNotExistsOnSearchField(value));
                        }
                        descList.add(value);
                    }
                }
            }
        }

        if (sortValues != null) {

            for (String field : sortValues) {
                if (descAll == Boolean.TRUE) {
                    drc.getSorts().add(new SortModel(CrudSort.DESC, field));
                } else {
                    // Field was set to desc
                    if (descList.contains(field)) {
                        drc.getSorts().add(new SortModel(CrudSort.DESC, field));
                    } else {
                        drc.getSorts().add(new SortModel(CrudSort.ASC, field));
                    }
                }
            }
        }

        //Valid if fields exists on fields attribute from @Search annotation
        if (this.resourceInfo.getResourceMethod().isAnnotationPresent(Search.class)) {
            Search search = this.resourceInfo.getResourceMethod().getAnnotation(Search.class);
            List<String> searchFields = Arrays.asList(search.fields());
            if (searchFields.get(0) != null && !searchFields.get(0).equals("*")) {
                drc.getSorts().stream().filter((sortModel) -> (!searchFields.contains(sortModel.getField()))).forEachOrdered((sortModel) -> {
                    throw new BadRequestException(crudMessage.fieldRequestDoesNotExistsOnSearchField(sortModel.getField()));
                });
            }
        }

        // Validate if the fields are valid
        drc.getSorts().stream().forEach(sortModel -> {
            CrudUtilHelper.checkIfExistField(CrudUtilHelper.getTargetClass(this.resourceInfo.getResourceClass()), sortModel.getField());
        });

    }

    private List<String> getValuesFromQueryString(String key) {

        for (String queryStringKey : uriInfo.getQueryParameters().keySet()) {
            if (key.equalsIgnoreCase(queryStringKey)) {
                List<String> result = new LinkedList<>();
                for (String value : uriInfo.getQueryParameters().get(queryStringKey)) {
                    result.addAll(CrudUtilHelper.extractFields(value));
                }
                return result;
            }
        }
        return null;
    }
}
