/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

import java.util.List;

import org.demoiselle.jee.crud.fields.FieldsContext;
import org.demoiselle.jee.crud.pagination.PaginationContext;

/**
 *
 *
 * @author SERPRO
 *
 */
public interface Result {
    PaginationContext getPaginationContext();
    void setPaginationContext(PaginationContext paginationContext);
    FieldsContext getFieldsContext();
    void setFieldsContext(FieldsContext fieldsContext);
    Long getCount();
    void setCount(Long count);
	List<?> getContent();
	Class<?> getEntityClass();
	void setContent(List<?> content);
}
