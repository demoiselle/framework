/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.crud;

import java.util.Map;
import java.util.Set;

import org.demoiselle.jee.crud.sort.CrudSort;

/**
 * 
 * @author SERPRO
 *
 */
//TODO mover para demoisele-core
public interface DemoiselleRequestContext {

    Integer getLimit();
    void setLimit(Integer limit);

    Integer getOffset();
    void setOffset(Integer offset);

    Long getCount();
    void setCount(Long count);
    
    Class<?> getEntityClass();
    void setEntityClass(Class<?> entityClass);
    
    Map<String, Set<String>> getFilters();
    void setFilters(Map<String, Set<String>> filters);
    
    Map<CrudSort, Set<String>> getSorts();
    void setSorts(Map<CrudSort, Set<String>> sorts);
    
}
