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

import java.util.concurrent.atomic.AtomicLong;

import javax.enterprise.inject.Alternative;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.context.ViewContext;
import br.gov.frameworkdemoiselle.lifecycle.ViewScoped;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Faces;

/**
 * This {@link ViewContext} implementation uses a map provided by {@link UIViewRoot#getViewMap()} as a store. Any beans
 * stored on this store are kept as long as the view is still active.
 * 
 * @author serpro
 */
@Priority(Priority.L2_PRIORITY)
@Alternative
public class FacesViewContextImpl extends AbstractCustomContext implements ViewContext {

	private static final AtomicLong atomicLong = new AtomicLong();

	private static final String FACES_KEY = FacesViewContextImpl.class.getCanonicalName();

	/**
	 * Armazena todas as views relacionadas à sessão atual.<br>
	 * Deve ser acessado através do método {@link #getViewStore()}.
	 */
	private FacesViewBeanStore viewStoreInSession;

	public FacesViewContextImpl() {
		super(ViewScoped.class);
	}

	@Override
	protected boolean isStoreInitialized() {
		FacesContext context = FacesContext.getCurrentInstance();
		return context != null && context.getViewRoot().getViewMap(false) != null;
	}

	private FacesViewBeanStore getViewStore() {
		if (viewStoreInSession == null) {
			viewStoreInSession = Beans.getReference(FacesViewBeanStore.class);
		}
		return viewStoreInSession;
	}

	@Override
	protected BeanStore getStore() {

		// Não há necessidade de sincronismo aqui porque o FacesContext nunca é compartilhado entre threads.
		Long viewId = (Long) Faces.getViewMap().get(FACES_KEY);
		if (viewId == null) {
			viewId = atomicLong.incrementAndGet();
			Faces.getViewMap().put(FACES_KEY, viewId);
		}

		return getViewStore().getStoreForView(viewId);
	}

	/**
	 * Called by the {@link FacesViewContextEventListener} on the PreDestroyViewMapEvent event so that we can destroy
	 * all beans associated with the view that's been cleared.
	 */
	public void clearView() {
		Long viewId = (Long) Faces.getViewMap().get(FACES_KEY);
		if (viewId == null) {
			return;
		}
		getViewStore().destroyStore(viewId);
	}
}
