package org.demoiselle.jee.crud.configuration;

import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.configuration.annotation.ConfigurationName;
import org.demoiselle.jee.crud.DemoiselleCrud;

/**
 * Configurations of the Demoiselle CRUD module
 *
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.crud")
public class DemoiselleCrudConfig {

    /**
     * Enable the search feature, adding query predicates based on request parameters.
     */
    @ConfigurationName("search.enabled")
    private boolean searchEnabled = true;

    /**
     * Enable the filter feature, filtering out some fields from an entity based on request parameters.
     */
    @ConfigurationName("filterFields.enabled")
    private boolean filterFields = true;

    /**
     * Enable pagination globally, if this is disabled pagination will only be possible from request parameters
     * if {@link org.demoiselle.jee.crud.helper.DemoiselleCrudHelper} instances are created manually.
     */
    @ConfigurationName("pagination.globalEnabled")
    private boolean paginationEnabled = true;

    /**
     * The default page size. This is actually used to restrict the maximum number of records that can be returned.
     */
    @ConfigurationName("pagination.defaultPagination")
    private Integer defaultPagination = 20;

    /**
     * Enable the sort feature, adding "ORDER BY" clauses based on request parameters.
     */
    @ConfigurationName("sort.enabled")
    private boolean sortEnabled = true;

    public DemoiselleCrudConfig() {
    }

    public DemoiselleCrudConfig(boolean searchEnabled, boolean filterFields, boolean sortEnabled, boolean paginationEnabled, int defaultPagination) {
        this.searchEnabled = searchEnabled;
        this.filterFields = filterFields;
        this.sortEnabled = sortEnabled;
        this.paginationEnabled = paginationEnabled;
        this.defaultPagination = defaultPagination;
    }


    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public boolean isFilterFields() {
        return filterFields;
    }

    public boolean isPaginationEnabled() {
        return paginationEnabled;
    }

    public boolean isSortEnabled() {
        return sortEnabled;
    }

    public Integer getDefaultPagination() {
        return defaultPagination;
    }
}