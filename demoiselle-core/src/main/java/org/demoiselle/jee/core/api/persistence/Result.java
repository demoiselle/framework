package org.demoiselle.jee.core.api.persistence;

import java.util.List;

public interface Result {

	public Integer getOffset();
	public void setOffset(Integer offset);
	
	public Integer getLimit();
	public void setLimit(Integer limit);
	
	public Long getCount();
	public void setCount(Long count);
	
	public List<?> getContent();
	public void setContent(List<?> content);

}
