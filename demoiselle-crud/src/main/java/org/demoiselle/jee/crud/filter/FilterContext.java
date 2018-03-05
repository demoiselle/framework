package org.demoiselle.jee.crud.filter;

import java.util.Set;

import org.demoiselle.jee.crud.TreeNodeField;
import org.demoiselle.jee.crud.pagination.PaginationContext;

public class FilterContext {
    public static final FilterContext DISABLED_FILTER = new FilterContext(false, null);

    private boolean filterEnabled;
    TreeNodeField<String, Set<String>> filters;

    public FilterContext(boolean filterEnabled, TreeNodeField<String, Set<String>> filters) {
        this.filterEnabled = filterEnabled;
        this.filters = filters;
    }

    public static FilterContext disabledFilter() {
        return DISABLED_FILTER;
    }

    public boolean isFilterEnabled() {
        return filterEnabled;
    }

    public void setFilterEnabled(boolean filterEnabled) {
        this.filterEnabled = filterEnabled;
    }

    public TreeNodeField<String, Set<String>> getFilters() {
        return filters;
    }

    public void setFilters(TreeNodeField<String, Set<String>> filters) {
        this.filters = filters;
    }

    public FilterContext copy() {
        return new FilterContext(filterEnabled, filters);
    }
}
