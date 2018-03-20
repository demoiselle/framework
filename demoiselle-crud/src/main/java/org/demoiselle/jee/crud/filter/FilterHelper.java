/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.filter;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.demoiselle.jee.crud.CrudMessage;
import org.demoiselle.jee.crud.CrudUtilHelper;
import org.demoiselle.jee.crud.DemoiselleRequestContext;
import org.demoiselle.jee.crud.ReservedKeyWords;
import org.demoiselle.jee.crud.TreeNodeField;
import org.demoiselle.jee.crud.configuration.DemoiselleCrudConfig;

import static org.demoiselle.jee.crud.CrudUtilHelper.extractSearchFieldsFromAnnotation;

/**
 * Class responsible for managing the 'filter' parameter comes from Url Query String.
 * 
 * Ex:
 * 
 * Given a request
 * <pre>
 * GET {@literal http://localhost:8080/api/users?field1=value1&field2=value2&field3(subField1)=subFieldValue1}
 * </pre>
 * 
 * This class will processing the request above and parse the values informed on parameters to 
 * a {@link org.demoiselle.jee.crud.TreeNodeField} object.
 * 
 * @author SERPRO
 */
@RequestScoped
public class FilterHelper {
    
    private UriInfo uriInfo;
    
    private ResourceInfo resourceInfo;
    
    @Inject
    private DemoiselleRequestContext drc;
    
    @Inject
    private CrudMessage crudMessage;

    @Inject
    private DemoiselleCrudConfig crudConfig;

    public FilterHelper(){}
    
    public FilterHelper(ResourceInfo resourceInfo, DemoiselleCrudConfig crudConfig, UriInfo uriInfo, DemoiselleRequestContext drc, CrudMessage crudMessage){
        this.crudConfig = crudConfig;
        this.uriInfo = uriInfo;
        this.resourceInfo = resourceInfo;
        this.drc = drc;
        this.crudMessage = crudMessage;
    }
    
    /**
     * Open the request query string to extract values used to filter the resource and 
     * fill the {@link TreeNodeField} object and set the fields parameter on {@link DemoiselleRequestContext#getFieldsContext()} object.
     * 
     * @param resourceInfo ResourceInfo
     * @param uriInfo UriInfo
     */
    public void execute(ResourceInfo resourceInfo, UriInfo uriInfo) {
        this.resourceInfo = resourceInfo == null ? this.resourceInfo : resourceInfo;
        this.uriInfo = uriInfo == null ? this.uriInfo : uriInfo;
        drc.getFilterContext().setFilterEnabled(isSearchEnabled());
        if (drc.getFilterContext().isFilterEnabled()) {
            drc.getFilterContext().setFilters(
                    extractFiltersFromParameterMap(
                            CrudUtilHelper.getEntityClass(this.resourceInfo),
                            uriInfo.getQueryParameters()
                    ));

            drc.getFilterContext().setDefaultFilters(
                    CrudUtilHelper.extractSearchFieldsFromAnnotation(drc.getDemoiselleResultAnnotation(),
                            drc.getEntityClass()));
        }
    }

    public boolean isSearchEnabled() {
        boolean isGlobalSearchEnabled = crudConfig.isSearchEnabled();
        boolean isAbstractRestRequest = CrudUtilHelper.getAbstractRestTargetClass(resourceInfo) != null;
        boolean isSearchEnabledInAnnotation = drc.getDemoiselleResultAnnotation() != null &&
                drc.getDemoiselleResultAnnotation().enableSearch();
        return isGlobalSearchEnabled && (isAbstractRestRequest || isSearchEnabledInAnnotation);
    }

    public static TreeNodeField<String,Set<String>> extractFiltersFromParameterMap(Class<?> targetClass,
                                                                                   MultivaluedMap<String, String> parameterMap) {
        Map<String, Set<String>> filters = new ConcurrentHashMap<>(5);

        parameterMap.forEach((key, values) ->{
            if(!isReservedKey(key)){
                Set<String> paramValues = new LinkedHashSet<>();

                values.stream().forEach(value -> {
                    paramValues.addAll(CrudUtilHelper.extractFields(value));
                });

                filters.putIfAbsent(key, paramValues);
            }
        });

        TreeNodeField<String, Set<String>> tnf = new TreeNodeField<>(targetClass.getName(), ConcurrentHashMap.newKeySet(1));

        if(!filters.isEmpty()){
            filters.forEach( (key, value) ->
                    CrudUtilHelper.fillLeafTreeNodeField(tnf, key, value)
            );

            return tnf;
        }
        return null;
    }

    private static Boolean isReservedKey(String key) {
        return key.equalsIgnoreCase(ReservedKeyWords.DEFAULT_RANGE_KEY.getKey()) 
                || key.equalsIgnoreCase(ReservedKeyWords.DEFAULT_SORT_DESC_KEY.getKey()) 
                || key.equalsIgnoreCase(ReservedKeyWords.DEFAULT_SORT_KEY.getKey())
                || key.equalsIgnoreCase(ReservedKeyWords.DEFAULT_FIELD_KEY.getKey());
    }
}
