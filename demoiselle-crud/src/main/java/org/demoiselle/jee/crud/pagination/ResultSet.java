/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.AbstractDAO;
import org.demoiselle.jee.crud.fields.FieldsContext;

/**
 * This classes implements {@link org.demoiselle.jee.core.api.crud.Result} to hold the results came from {@link AbstractDAO}
 * 
 * @author SERPRO
 */
public class ResultSet<T>  implements Result<T> {

    private PaginationContext paginationContext;
    private FieldsContext fieldsContext;
    private Long count;
    private Class<T> entityClass;
	private List<T> content = new LinkedList<>();

	@Override
	public List<T> getContent() {
        return content;
    }

    @Override
	public void setContent(List<T> content) {
		this.content = content;
	}

    public static <V> ResultSet<V> forList(List<V> resultList, Class<V> entityClass, PaginationContext paginationContext, FieldsContext fieldsContext) {
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
    public Class<T> getResultType() {
        return entityClass;
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public static <T, V> ResultSet<V> transform(Result<T> result, Class<V> resultClass, Function<T, V> transformer) {
	    List<V> resultList = result.getContent().stream().map(transformer).collect(Collectors.toList());
	    return ResultSet.forList(resultList, resultClass, result.getPaginationContext(), result.getFieldsContext());
    }
}