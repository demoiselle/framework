package org.demoiselle.jee.crud.pagination;

public class PaginationContext {
    private static final PaginationContext DISABLED_PAGINATION = new PaginationContext(null, null, false);
    private Integer limit;
    private Integer offset;
    private boolean paginationEnabled;

    public PaginationContext() {
    }

    public PaginationContext(Integer limit, Integer offset, Boolean paginationEnabled) {
        this.limit = limit;
        this.offset = offset;
        this.paginationEnabled = Boolean.TRUE.equals(paginationEnabled);
    }

    public PaginationContext copy() {
        return new PaginationContext(this.getLimit(), this.getOffset(), this.paginationEnabled);
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public boolean isPaginationEnabled() {
        return paginationEnabled;
    }

    public void setPaginationEnabled(boolean paginationEnabled) {
        this.paginationEnabled = paginationEnabled;
    }

    public static final PaginationContext disabledPagination() {
        return PaginationContext.DISABLED_PAGINATION;
    }
}
