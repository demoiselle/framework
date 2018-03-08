/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.util.function.Function;

import org.demoiselle.jee.crud.fields.FieldsContext;
import org.demoiselle.jee.crud.filter.FilterContext;
import org.demoiselle.jee.crud.pagination.PaginationContext;
import org.demoiselle.jee.crud.sort.SortContext;

/**
 * Class used to make a Context of the Request and Response of CRUD feature.
 * 
 * @author SERPRO 
 */
public interface DemoiselleRequestContext {

    boolean isAbstractRestRequest();
    void setAbstractRestRequest(boolean abstractRestRequest);

    DemoiselleResult getDemoiselleResultAnnotation();
    void setDemoiselleResultAnnotation(DemoiselleResult demoiselleResultAnnotation);

    PaginationContext getPaginationContext();
    void setPaginationContext(PaginationContext paginationContext);

    FilterContext getFilterContext();
    void setFilterContext(FilterContext filterContext);

    FieldsContext getFieldsContext();
    void setFieldsContext(FieldsContext fieldsContext);

    SortContext getSortContext();
    void setSortContext(SortContext sortContext);

//    List<SortModel> getSorts();
//    void setSorts(List<SortModel> sorts);
//
//    TreeNodeField<String, Set<String>> getFields();
//    void setFields(TreeNodeField<String, Set<String>> fields);
//
    Class<?> getEntityClass();
    void setEntityClass(Class<?> entityClass);

    Class<?> getResultClass();
    void setResultClass(Class<?> resultClass);

    Function getResultTransformer();
    void setResultTransformer(Function resultTransformer);

}
