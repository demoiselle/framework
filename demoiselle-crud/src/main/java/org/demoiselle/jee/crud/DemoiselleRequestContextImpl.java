/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.demoiselle.jee.crud.fields.FieldsContext;
import org.demoiselle.jee.crud.filter.FilterContext;
import org.demoiselle.jee.crud.pagination.PaginationContext;
import org.demoiselle.jee.crud.sort.SortContext;
import org.demoiselle.jee.crud.sort.SortModel;


/**
 * Implementation from {@link DemoiselleRequestContext}
 *
 * @author SERPRO
 */
@RequestScoped
public class DemoiselleRequestContextImpl implements DemoiselleRequestContext {

    private Class<?> entityClass = null;

    private FieldsContext fieldsContext = FieldsContext.disabledFields();
    private PaginationContext paginationContext = PaginationContext.disabledPagination();
    private SortContext sortContext = SortContext.disabledSort();
    private FilterContext filterContext = FilterContext.disabledFilter();
    private DemoiselleCrud demoiselleCrudAnnotation;
    private boolean abstractRestRequest;

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
    public DemoiselleCrud getDemoiselleCrudAnnotation() {
        return demoiselleCrudAnnotation;
    }

    @Override
    public void setDemoiselleCrudAnnotation(DemoiselleCrud demoiselleCrudAnnotation) {
        this.demoiselleCrudAnnotation = demoiselleCrudAnnotation;
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
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DemoiselleRequestContextImpl that = (DemoiselleRequestContextImpl) o;

        return new EqualsBuilder()
                .append(entityClass, that.entityClass)
                .append(fieldsContext, that.fieldsContext)
                .append(filterContext, that.filterContext)
                .append(paginationContext, that.paginationContext)
                .append(sortContext, that.sortContext)
                .append(demoiselleCrudAnnotation, that.demoiselleCrudAnnotation)
                .append(abstractRestRequest, that.abstractRestRequest)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(entityClass)
                .append(fieldsContext)
                .append(sortContext)
                .append(filterContext)
                .append(paginationContext)
                .append(demoiselleCrudAnnotation)
                .append(abstractRestRequest)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "DemoiselleRequestContextImpl{" +
                "entityClass=" + entityClass +
                ", filterContext=" + fieldsContext +
                ", sortContext=" + sortContext +
                ", filterContext=" + filterContext +
                ", paginationContext=" + paginationContext +
                ", abstractRestRequest=" + abstractRestRequest +
                ", demoiselleCrudAnnotation=" + demoiselleCrudAnnotation +

                '}';
    }
}
