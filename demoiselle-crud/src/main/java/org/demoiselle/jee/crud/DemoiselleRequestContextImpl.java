/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import javax.enterprise.context.RequestScoped;

import java.util.function.Function;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.demoiselle.jee.crud.fields.FieldsContext;
import org.demoiselle.jee.crud.filter.FilterContext;
import org.demoiselle.jee.crud.pagination.PaginationContext;
import org.demoiselle.jee.crud.sort.SortContext;


/**
 * Implementation from {@link DemoiselleRequestContext}
 *
 * @author SERPRO
 */
@RequestScoped
public class DemoiselleRequestContextImpl implements DemoiselleRequestContext {

    private Class<?> entityClass = null;
    private Class<?> resultClass = null;

    private FieldsContext fieldsContext = FieldsContext.disabledFields();
    private PaginationContext paginationContext = PaginationContext.disabledPagination();
    private SortContext sortContext = SortContext.disabledSort();
    private FilterContext filterContext = FilterContext.disabledFilter();
    private DemoiselleResult demoiselleResultAnnotation;
    private boolean abstractRestRequest;
    private Function resultTransformer;

    @Override
    public boolean isAbstractRestRequest() {return abstractRestRequest;
    }

    @Override
    public void setAbstractRestRequest(boolean abstractRestRequest) {
        this.abstractRestRequest = abstractRestRequest;
    }

    @Override
    public FieldsContext getFieldsContext() {
        return fieldsContext;
    }

    @Override
    public void setFieldsContext(FieldsContext fieldsContext) {
        this.fieldsContext = fieldsContext;
    }



    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public PaginationContext getPaginationContext() {
        return paginationContext;
    }

    @Override
    public void setPaginationContext(PaginationContext paginationContext) {
        this.paginationContext = paginationContext;
    }

    @Override
    public DemoiselleResult getDemoiselleResultAnnotation() {
        return demoiselleResultAnnotation;
    }

    @Override
    public void setDemoiselleResultAnnotation(DemoiselleResult demoiselleResultAnnotation) {
        this.demoiselleResultAnnotation = demoiselleResultAnnotation;
    }

    @Override
    public SortContext getSortContext() {
        return sortContext;
    }

    @Override
    public void setSortContext(SortContext sortContext) {
        this.sortContext = sortContext;
    }

    @Override
    public FilterContext getFilterContext() {
        return filterContext;
    }

    @Override
    public void setFilterContext(FilterContext filterContext) {
        this.filterContext = filterContext;
    }

    @Override
    public Class<?> getResultClass() {
        return resultClass;
    }

    @Override
    public void setResultClass(Class<?> resultClass) {
        this.resultClass = resultClass;
    }

    @Override
    public Function getResultTransformer() {
        return resultTransformer;
    }

    @Override
    public void setResultTransformer(Function resultTransformer) {
        this.resultTransformer = resultTransformer;
    }
}
