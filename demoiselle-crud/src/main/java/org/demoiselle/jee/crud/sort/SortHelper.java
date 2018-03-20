/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.sort;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.AbstractDAO;
import org.demoiselle.jee.crud.CrudMessage;
import org.demoiselle.jee.crud.CrudUtilHelper;
import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.ReservedKeyWords;
import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig;

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

    @Inject
    private DemoiselleCrudConfig crudConfig;

    public SortHelper() {
    }

    public SortHelper(ResourceInfo resourceInfo, UriInfo uriInfo, DemoiselleCrudConfig crudConfig, DemoiselleRequestContext drc, SortHelperMessage sortHelperMessage, CrudMessage crudMessage) {
        this.resourceInfo = resourceInfo;
        this.uriInfo = uriInfo;
        this.crudConfig = crudConfig;
        this.drc = drc;
        this.sortHelperMessage = sortHelperMessage;
        this.crudMessage = crudMessage;
    }

    /**
     * Open the request query string to extract values from 'sort' and 'desc'
     * parameters and fill the {@link DemoiselleRequestContext#getSortContext()}
     *
     * @param resourceInfo ResourceInfo
     * @param uriInfo UriInfo
     */
    public void execute(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;

        List<SortModel> sorts = extractSortsFromParameterMap(uriInfo.getQueryParameters(), crudMessage, sortHelperMessage);
        drc.getSortContext().setSortEnabled(isSortEnabled());
        drc.getSortContext().setSorts(sorts);
    }


    public static List<SortModel> extractSortsFromParameterMap(MultivaluedMap<String, String> map, CrudMessage crudMessage, SortHelperMessage sortHelperMessage) {
        List<String> descValues = getValuesFromParameterMap(map, ReservedKeyWords.DEFAULT_SORT_DESC_KEY.getKey());
        List<String> sortValues = getValuesFromParameterMap(map, ReservedKeyWords.DEFAULT_SORT_KEY.getKey());
        List<SortModel> sorts = getSortsFromParameters(sortValues, descValues, crudMessage, sortHelperMessage);
        return sorts;
    }

    private static List<SortModel> getSortsFromParameters(List<String> sortValues, List<String> descValues, CrudMessage crudMessage, SortHelperMessage sortHelperMessage) {
        Set<String> descList = new LinkedHashSet<>();
        Boolean descAll = Boolean.FALSE;

        // 'desc' parameter was filled and 'sort' parameter not
        if (descValues != null && sortValues == null) {
            throw new IllegalArgumentException(sortHelperMessage.descParameterWithoutSortParameter());
        }

        List<SortModel> sorts  = new ArrayList<>();

        if (descValues != null) {

            //&desc without parameters
            if (descValues.isEmpty()) {
                descAll = Boolean.TRUE;
            } else {
                for (String value : descValues) {
                    //&desc=a,b,c
                    if (!value.isEmpty()) {
                        if (!sortValues.contains(value)) {
                            throw new BadRequestException(crudMessage.fieldRequestDoesNotExistsOnDemoiselleResultField(value));
                        }
                        descList.add(value);
                    }
                }
            }
        }

        if (sortValues != null) {
            for (String field : sortValues) {
                if (descAll == Boolean.TRUE) {
                    sorts.add(new SortModel(CrudSort.DESC, field));
                } else {
                    // Field was set to desc
                    if (descList.contains(field)) {
                        sorts.add(new SortModel(CrudSort.DESC, field));
                    } else {
                        sorts.add(new SortModel(CrudSort.ASC, field));
                    }
                }
            }
        }
        return sorts;
    }

    public static void validateSorts(List<SortModel> sorts, List<String> searchFields, Class<?> entityClass) {
        CrudMessage crudMessage = CDI.current().select(CrudMessage.class).select().get();
        if (searchFields != null && searchFields.get(0) != null && !searchFields.get(0).equals("*")) {
            sorts.stream().filter((sortModel) -> (!searchFields.contains(sortModel.getField()))).forEachOrdered((sortModel) -> {
                throw new BadRequestException(crudMessage.fieldRequestDoesNotExistsOnDemoiselleResultField(sortModel.getField()));
            });
        }
        // Validate if the fields are valid
        sorts.stream().forEach(sortModel -> {
            CrudUtilHelper.checkIfExistField(entityClass, sortModel.getField(), crudMessage);
        });
    }

    private boolean isSortEnabled() {
        boolean isGlobalSortEnabled = crudConfig.isSortEnabled();
        boolean isWithSortParameters = hasSortParametersInRequest();
        boolean isAbstractRestClass = CrudUtilHelper.getAbstractRestTargetClass(resourceInfo) != null;
        boolean isSortEnabledInAnnotation = CrudUtilHelper.getDemoiselleResultAnnotation(resourceInfo) != null
                && CrudUtilHelper.getDemoiselleResultAnnotation(resourceInfo).enableSort();
        return isGlobalSortEnabled && isWithSortParameters && (isAbstractRestClass || isSortEnabledInAnnotation);

    }

    private boolean hasSortParametersInRequest() {
        if (uriInfo.getQueryParameters() == null) {
            return false;
        }
        for (String queryStringKey : uriInfo.getQueryParameters().keySet()) {
            if (ReservedKeyWords.DEFAULT_SORT_KEY.getKey().equalsIgnoreCase(queryStringKey)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> getValuesFromParameterMap(MultivaluedMap<String, String> map, String key) {
        if (map == null) {
            return null;
        }
        for (String queryStringKey : map.keySet()) {
            if (key.equalsIgnoreCase(queryStringKey)) {
                List<String> result = new LinkedList<>();
                for (String value : map.get(queryStringKey)) {
                    result.addAll(CrudUtilHelper.extractFields(value));
                }
                return result;
            }
        }
        return null;
    }
}
