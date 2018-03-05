package org.demoiselle.jee.crud.pagination;

public class PaginationParameters {
    private boolean enabled;
    private Integer offset;
    private Integer limit;

    public PaginationParameters(boolean enabled, Integer offset, Integer limit) {
        this.enabled = enabled;
        this.offset = offset;
        this.limit = limit;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getLimit() {
        return limit;
    }
}
