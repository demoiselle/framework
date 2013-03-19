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

import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Faces;
import br.gov.frameworkdemoiselle.util.Parameter;
import br.gov.frameworkdemoiselle.util.Reflections;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public abstract class AbstractEditPageBean<T, I> extends AbstractPageBean implements EditPageBean<T> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Parameter<String> id;

	private T bean;

	private Class<T> beanClass;

	private Class<I> idClass;

	@Inject
	@Name("demoiselle-jsf-bundle")
	private ResourceBundle bundle;

	@Inject
	private FacesContext facesContext;

	protected void clear() {
		this.id = null;
		this.bean = null;
	}

	protected T createBean() {
		return Beans.getReference(getBeanClass());
	}

	@Override
	public T getBean() {
		if (this.bean == null) {
			initBean();
		}

		return this.bean;
	}

	protected Class<T> getBeanClass() {
		if (this.beanClass == null) {
			this.beanClass = Reflections.getGenericTypeArgument(this.getClass(), 0);
		}

		return this.beanClass;
	}

	protected Class<I> getIdClass() {
		if (this.idClass == null) {
			this.idClass = Reflections.getGenericTypeArgument(this.getClass(), 1);
		}

		return this.idClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	public I getId() {
		Converter converter = getIdConverter();

		if (converter == null && String.class.equals(getIdClass())) {
			return (I) id.getValue();

		} else if (converter == null) {
			throw new DemoiselleException(bundle.getString("id-converter-not-found", getIdClass().getCanonicalName()));

		} else {
			return (I) converter.getAsObject(facesContext, facesContext.getViewRoot(), id.getValue());
		}
	}

	private Converter getIdConverter() {
		return Faces.getConverter(getIdClass());
	}

	protected abstract void handleLoad();

	private void initBean() {
		if (isUpdateMode()) {
			this.bean = this.loadBean();
		} else {
			setBean(createBean());
		}
	}

	@Override
	public boolean isUpdateMode() {
		return getId() != null;
	}

	private T loadBean() {
		this.handleLoad();
		return this.bean;
	}

	protected void setBean(final T bean) {
		this.bean = bean;
	}
}
