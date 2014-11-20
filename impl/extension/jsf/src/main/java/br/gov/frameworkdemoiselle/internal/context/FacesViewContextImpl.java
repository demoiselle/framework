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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.annotation.ViewScoped;
import br.gov.frameworkdemoiselle.context.ViewContext;
import br.gov.frameworkdemoiselle.lifecycle.BeforeSessionDestroyed;
import br.gov.frameworkdemoiselle.util.Beans;
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
	
	private final AtomicLong atomicLong = new AtomicLong();
	
	//private ConcurrentHashMap<String, FacesViewBeanStore> sessionBeanStore = new ConcurrentHashMap<String, FacesViewBeanStore>();

	private static final String FACES_KEY = FacesViewContextImpl.class.getCanonicalName();
	private static final String VIEW_STORE_KEY = FacesViewBeanStore.class.getCanonicalName();

	public FacesViewContextImpl() {
		super(ViewScoped.class);
	}
	
	@Override
	protected boolean isStoreInitialized() {
		return FacesContext.getCurrentInstance()!=null && getSession()!=null;
	}

	@Override
	protected BeanStore getStore() {
		HttpSession session = getSession();
		if (session == null){
			return null;
		}
		
		/*
		 * Tenta obter o viewID de forma não thread-safe por questões de performance.
		 * Se o viewID não existe entra em um trecho thread-safe para incrementa-lo, evitando
		 * conflito entre duas requests tentando incrementar esse número. 
		 */
		Long viewId = (Long)Faces.getViewMap().get(FACES_KEY);
		if (viewId==null){
			synchronized (this) {
				
				//Tenta obte-lo novamente, caso entre a primeira tentativa e o bloqueio
				//da thread outra thread já tenha criado o número. 
				viewId = (Long)Faces.getViewMap().get(FACES_KEY);
				if (viewId==null){
					viewId = atomicLong.incrementAndGet();
					Faces.getViewMap().put(FACES_KEY, viewId);
				}
			}
		}
		
		//A mesma técnica de bloqueio de thread acima é usada aqui para
		//criar um SessionBeanStore caso o mesmo ainda não exista.
		FacesViewBeanStore currentStore = (FacesViewBeanStore) session.getAttribute(VIEW_STORE_KEY);
		if (currentStore==null){
			synchronized (this) {
				currentStore = (FacesViewBeanStore) session.getAttribute(VIEW_STORE_KEY);
				if (currentStore==null){
					currentStore = new FacesViewBeanStore();
					session.setAttribute(VIEW_STORE_KEY, currentStore);
				}
			}
		}

		return currentStore.getStore(viewId, this);
	}
	
	/*
	 * Called before the session is invalidated for that user.
	 * Destroys all view scoped beans stored on that session.
	 */
	private void clearInvalidatedSession(HttpSession session){
		if (session != null){
			FacesViewBeanStore store = (FacesViewBeanStore) session.getAttribute(VIEW_STORE_KEY);
			if (store!=null){
				session.removeAttribute(VIEW_STORE_KEY);
				store.clear(this);
			}
		}
	}
	
	private HttpSession getSession(){
		return (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
	}
	
	/**
	 * Observes HTTP session lifecycle and notifies the ViewContext of session events (creation or destruction)
	 * so view scoped beans can be created or destroyed based on their underlying session scopes.
	 * 
	 * @author SERPRO
	 *
	 */
	@ApplicationScoped
	protected static class FacesViewSessionListener {
		
		/**
		 * Called before the session is invalidated for that user.
		 * Destroys all view scoped beans stored on that session.
		 */
		protected void clearInvalidatedSession(@Observes BeforeSessionDestroyed event){
			HttpSession session = event.getSession();
			try{
				Context context = Beans.getBeanManager().getContext(ViewScoped.class);
				if ( FacesViewContextImpl.class.isInstance(context) ){
					((FacesViewContextImpl)context).clearInvalidatedSession(session);
				}
			}
			catch(ContextNotActiveException ce){
				//Nada a fazer, contexto não está ativo. 
			}
		}
	}
}

