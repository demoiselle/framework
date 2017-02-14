/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;

import org.demoiselle.jee.crud.sort.SortModel;

/**
 * Implementation from {@link DemoiselleRequestContext}
 * 
 * @author SERPRO
 */
@RequestScoped
public class DemoiselleRequestContextImpl implements DemoiselleRequestContext {

    private Integer offset = null;
    private Integer limit = null;
    private Long count = null;
    private Class<?> entityClass = null;
    private TreeNodeField<String, Set<String>> filters = null; 
    private List<SortModel> sorts = new LinkedList<>();
    private TreeNodeField<String, Set<String>> fields = null;
    private Boolean isPaginationEnabled = Boolean.TRUE;

    @Override
    public Integer getLimit() {
        return limit;
    }

    @Override
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public Integer getOffset() {
        return offset;
    }

    @Override
    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    @Override
    public Long getCount() {
        return count;
    }

    @Override
    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

    @Override
    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public TreeNodeField<String, Set<String>> getFilters() {
        return filters;
    }

    @Override
    public void setFilters(TreeNodeField<String, Set<String>> filters) {
        this.filters = filters;
    }
    
    @Override
    public List<SortModel> getSorts() {
        return this.sorts;
    }

    @Override
    public void setSorts(List<SortModel> sorts) {
        this.sorts = sorts;
    }

    @Override
    public TreeNodeField<String, Set<String>> getFields() {
        return this.fields;
    }

    @Override
    public void setFields(TreeNodeField<String, Set<String>> fields) {
        this.fields = fields;
    }

    @Override
    public Boolean isPaginationEnabled() {
        return this.isPaginationEnabled;
    }

    @Override
    public void setPaginationEnabled(Boolean isPaginationEnabled) {
        this.isPaginationEnabled = isPaginationEnabled;
    }

    @Override
    public String toString() {
        return "DemoiselleRequestContextImpl [offset=" + offset + ", limit=" + limit + ", count=" + count + ", paginationEnabled=" + isPaginationEnabled + "]";
    }

}
