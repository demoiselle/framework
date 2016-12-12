/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.crud.pagination;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;

import org.demoiselle.jee.core.api.crud.Result;

/**
 * 
 * @author SERPRO
 *
 */
@RequestScoped
public class ResultSet implements Result{
	
	private Integer offset = new Integer(0);
	private Integer limit = new Integer(0);
	private Long count = new Long(0);
	private List<?> content = new ArrayList<>();
	private Class<?> entityClass = null;

	@Override
	public Integer getOffset() {
		return this.offset;
	}

	@Override
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	@Override
	public Integer getLimit() {
		return this.limit;
	}

	@Override
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	@Override
	public Long getCount() {
		return this.count;
	}

	@Override
	public void setCount(Long count) {
		this.count = count;
	}

	@Override
	public List<?> getContent() {
		return this.content;
	}

	@Override
	public void setContent(List<?> content) {
		this.content = (List<?>) content;
	}

	@Override
	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	@Override
	public Class<?> getEntityClass() {
		return this.entityClass;
	}
	
	@Override
	public String toString() {
		return "ResultSet [offset=" + offset + ", limit=" + limit + ", count=" + count + "]";
	}

}