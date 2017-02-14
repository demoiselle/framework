/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.util.List;
import java.util.Set;

import org.demoiselle.jee.crud.sort.SortModel;

/**
 * Class used to make a Context of the Request and Response of CRUD feature.
 * 
 * @author SERPRO 
 */
public interface DemoiselleRequestContext {

    Integer getLimit();
    void setLimit(Integer limit);

    Integer getOffset();
    void setOffset(Integer offset);

    Long getCount();
    void setCount(Long count);
    
    Class<?> getEntityClass();
    void setEntityClass(Class<?> entityClass);
    
    TreeNodeField<String, Set<String>> getFilters();
    void setFilters(TreeNodeField<String, Set<String>> filters);
    
    List<SortModel> getSorts();
    void setSorts(List<SortModel> sorts);
    
    TreeNodeField<String, Set<String>> getFields();
    void setFields(TreeNodeField<String, Set<String>> fields);
    
    Boolean isPaginationEnabled();
    void setPaginationEnabled(Boolean isPaginationEnabled);
    
}
