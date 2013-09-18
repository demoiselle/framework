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
package br.gov.frameworkdemoiselle.internal.context;

import java.util.Map;

import javax.enterprise.inject.Alternative;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.annotation.ViewScoped;
import br.gov.frameworkdemoiselle.context.ViewContext;
import br.gov.frameworkdemoiselle.util.Faces;

/**
 * 
 * This {@link ViewContext} implementation uses a map provided
 * by {@link UIViewRoot#getViewMap()} as a store. Any beans stored on
 * this store are kept as long as the view is still active.
 * 
 * @author serpro
 *
 */
@Priority(Priority.L2_PRIORITY)
@Alternative
public class FacesViewContextImpl extends AbstractCustomContext implements ViewContext {

	public FacesViewContextImpl() {
		super(ViewScoped.class);
	}
	
	@Override
	protected boolean isStoreInitialized() {
		return FacesContext.getCurrentInstance()!=null;
	}

	@Override
	protected Store getStore() {
		Map<String, Object> viewMap = Faces.getViewMap();
		String key = Store.class.getName();

		if (!viewMap.containsKey(key)) {
			viewMap.put(key, createStore());
		}

		return (Store) viewMap.get(key);
	}
}
