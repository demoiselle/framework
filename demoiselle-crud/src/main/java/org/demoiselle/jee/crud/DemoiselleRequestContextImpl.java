/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or
 * <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.RequestScoped;

import org.demoiselle.jee.crud.sort.CrudSort;

/**
 * @author SERPRO
 *
 */
/**
 * @author SERPRO
 *
 */
@RequestScoped
public class DemoiselleRequestContextImpl implements DemoiselleRequestContext {

    private Integer offset = null;
    private Integer limit = null;
    private Long count = null;
    private Class<?> entityClass = null;
    private Map<String, Set<String>> filters = new HashMap<>();
    private Map<CrudSort, Set<String>> sorts = new HashMap<>();

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
    public Map<String, Set<String>> getFilters() {
        return filters;
    }

    @Override
    public void setFilters(Map<String, Set<String>> fieldsFilter) {
        this.filters = fieldsFilter;
    }
    
    @Override
    public Map<CrudSort, Set<String>> getSorts() {
        return sorts;
    }

    @Override
    public void setSorts(Map<CrudSort, Set<String>> sorts) {
        this.sorts = sorts;
    }

    @Override
    public String toString() {
        return "DemoiselleRequestContextImpl [offset=" + offset + ", limit=" + limit + ", count=" + count + "]";
    }

}
