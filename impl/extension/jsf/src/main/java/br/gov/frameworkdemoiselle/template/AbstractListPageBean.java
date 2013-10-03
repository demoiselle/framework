/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;

import br.gov.frameworkdemoiselle.pagination.Pagination;
import br.gov.frameworkdemoiselle.pagination.PaginationContext;
import br.gov.frameworkdemoiselle.util.Reflections;
/**
 * Template Managed AuthenticationBean class that implements the methods defined by the interface ListPageBean.
 * 
 * @param <T>
 *            bean object type
 * @param <I>
 *            bean id type
 *
 * @author SERPRO
 * @see ListPageBean
 */
public abstract class AbstractListPageBean<T, I> extends AbstractPageBean implements ListPageBean<T, I> {

	private static final long serialVersionUID = 1L;

	private List<T> resultList;

	private transient DataModel<T> dataModel;

	private Map<I, Boolean> selection = new HashMap<I, Boolean>();

	@Inject
	private PaginationContext paginationContext;

	public void clear() {
		this.dataModel = null;
		this.resultList = null;
	}

	private Class<T> beanClass;

	protected Class<T> getBeanClass() {
		if (this.beanClass == null) {
			this.beanClass = Reflections.getGenericTypeArgument(this.getClass(), 0);
		}

		return this.beanClass;
	}

	@Override
	public DataModel<T> getDataModel() {
		if (this.dataModel == null) {
			this.dataModel = new ListDataModel<T>(this.getResultList());
		}

		return this.dataModel;
	}

	@Override
	public List<T> getResultList() {
		if (this.resultList == null) {
			this.resultList = handleResultList();
		}

		return this.resultList;
	}

	protected abstract List<T> handleResultList();

	@Override
	public String list() {
		clear();
		return getCurrentView();
	}

	public Map<I, Boolean> getSelection() {
		return selection;
	}

	public void setSelection(Map<I, Boolean> selection) {
		this.selection = selection;
	}

	public void clearSelection() {
		this.selection = new HashMap<I, Boolean>();
	}

	public List<I> getSelectedList() {
		List<I> selectedList = new ArrayList<I>();
		Iterator<I> iter = getSelection().keySet().iterator();
		while (iter.hasNext()) {
			I id = iter.next();
			if (getSelection().get(id)) {
				selectedList.add(id);
			}
		}
		return selectedList;
	}

	public Pagination getPagination() {
		return paginationContext.getPagination(getBeanClass(), true);
	}
}
