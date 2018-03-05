/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.LinkedList;
import java.util.List;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.AbstractDAO;
import org.demoiselle.jee.crud.fields.FieldsContext;

/**
 * This classes implements {@link org.demoiselle.jee.core.api.crud.Result} to hold the results came from {@link AbstractDAO}
 * 
 * @author SERPRO
 */
public class ResultSet implements Result{

    private PaginationContext paginationContext;
    private FieldsContext fieldsContext;
    private Long count;
    private Class<?> entityClass;
	private List<?> content = new LinkedList<>();

	@Override
	public List<?> getContent() {
        return content;
    }

    @Override
	public void setContent(List<?> content) {
		this.content = (List<?>) content;
	}

    public static <T> Result forList(List<T> resultList, Class<T> entityClass, PaginationContext paginationContext, FieldsContext fieldsContext) {
        ResultSet resultSet = new ResultSet();
        resultSet.setCount((long) resultList.size());
        resultSet.setContent(resultList);
        resultSet.setEntityClass(entityClass);
        resultSet.setPaginationContext(paginationContext);
        resultSet.setFieldsContext(fieldsContext);
        return resultSet;
    }

    public PaginationContext getPaginationContext() {
        return paginationContext;
    }

    public void setPaginationContext(PaginationContext paginationContext) {
        this.paginationContext = paginationContext;
    }

    public FieldsContext getFieldsContext() {
        return fieldsContext;
    }

    public void setFieldsContext(FieldsContext fieldsContext) {
        this.fieldsContext = fieldsContext;
    }

    @Override
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

}