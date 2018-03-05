package org.demoiselle.jee.crud.sort;

import java.util.Collections;
import java.util.List;

import org.demoiselle.jee.crud.pagination.PaginationContext;

public class SortContext {
    private final static SortContext SORT_DISABLED = new SortContext(false, Collections.emptyList());

    private boolean sortEnabled;
    private List<SortModel> sorts;

    public SortContext(boolean sortEnabled, List<SortModel> sorts) {
        this.sortEnabled = sortEnabled;
        this.sorts = sorts;
    }

    public static SortContext disabledSort() {
        return SORT_DISABLED;
    }

    public boolean isSortEnabled() {
        return sortEnabled;
    }

    public void setSortEnabled(boolean sortEnabled) {
        this.sortEnabled = sortEnabled;
    }

    public List<SortModel> getSorts() {
        return sorts;
    }

    public void setSorts(List<SortModel> sorts) {
        this.sorts = sorts;
    }

    public SortContext copy() {
        return new SortContext(this.sortEnabled, this.sorts);
    }
}
