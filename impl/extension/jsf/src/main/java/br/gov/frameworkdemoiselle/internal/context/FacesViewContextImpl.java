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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import br.gov.frameworkdemoiselle.annotation.Priority;
import br.gov.frameworkdemoiselle.context.ViewContext;
import br.gov.frameworkdemoiselle.lifecycle.BeforeRequestDestroyed;
import br.gov.frameworkdemoiselle.lifecycle.BeforeSessionDestroyed;
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

	private final AtomicLong atomicLong = new AtomicLong();

	// Armazena todas as views relacionadas à sessão atual. Quando uma sessão
	// termina, o store correspondente é destruído.
	private ConcurrentHashMap<String, FacesViewBeanStore> viewStoreInSession = new ConcurrentHashMap<String, FacesViewBeanStore>();

	private static final String FACES_KEY = FacesViewContextImpl.class.getCanonicalName();

	public FacesViewContextImpl() {
		super(ViewScoped.class);
	}

	@Override
	protected boolean isStoreInitialized() {
		return FacesContext.getCurrentInstance() != null && getSessionId() != null;
	}

	@Override
	protected BeanStore getStore() {
		String sessionId = getSessionId();
		if (sessionId == null) {
			return null;
		}

		/*
		 * Tenta obter o viewID de forma não thread-safe por questões de performance. Se o viewID não existe entra em um
		 * trecho thread-safe para incrementa-lo, evitando conflito entre duas requests tentando incrementar esse
		 * número.
		 */
		Long viewId = (Long) Faces.getViewMap().get(FACES_KEY);
		if (viewId == null) {
			synchronized (this) {

				// Tenta obte-lo novamente, caso entre a primeira tentativa e o
				// bloqueio
				// da thread outra thread já tenha criado o número.
				viewId = (Long) Faces.getViewMap().get(FACES_KEY);
				if (viewId == null) {
					viewId = atomicLong.incrementAndGet();
					Faces.getViewMap().put(FACES_KEY, viewId);
				}
			}
		}

		// A mesma técnica de bloqueio de thread acima é usada aqui para
		// criar um FacesViewBeanStore caso o mesmo ainda não exista, e
		// associa-lo à sessão atual.
		FacesViewBeanStore currentViewStore = viewStoreInSession.get(sessionId);
		if (currentViewStore == null) {
			synchronized (this) {
				currentViewStore = (FacesViewBeanStore) viewStoreInSession.get(sessionId);
				if (currentViewStore == null) {
					currentViewStore = new FacesViewBeanStore(getSessionTimeout());
					viewStoreInSession.put(sessionId, currentViewStore);
				}
			}
		}

		return currentViewStore.getStoreForView(viewId, this);
	}

	/*
	 * Called before the session is invalidated for that user.
	 * Destroys all view scoped beans stored on that session.
	 */
	private void clearInvalidatedSession(String sessionId) {
		if (sessionId != null) {
			final FacesViewBeanStore store = viewStoreInSession.get(sessionId);
			if (store != null) {
				store.destroyStoresInSession(this);
				viewStoreInSession.remove(sessionId);
			}
		}
	}

	/*
	 * Called at each new request at a given session.
	 * Destroys any expired views.
	 */
	private synchronized void clearExpiredViews(String sessionId) {
		if (sessionId != null) {
			final FacesViewBeanStore store = viewStoreInSession.get(sessionId);
			if (store != null) {
				store.destroyStoresInSession(this, true);
			}
		}
	}

	/*
	 * Returns the current session ID. Creates a session if one doesn't exist.
	 * Returns NULL if the session can't be created.
	 */
	private String getSessionId() {
		final HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
				.getSession(true);
		return session != null ? session.getId() : null;
	}

	/*
	 * Returns the configured session timeout in seconds. This is the maximum
	 * inactive interval, not the remaining timeout for this session.
	 */
	private int getSessionTimeout() {
		final HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
				.getSession(true);
		return session != null ? session.getMaxInactiveInterval() : 0;
	}

	/**
	 * Observes HTTP servlet lifecycle and notifies the ViewContext of session events (creation or destruction)
	 * and request events (before going into scope and before going out of scope) so view
	 * scoped beans can be created or destroyed based on their underlying session and request scopes.
	 * 
	 * @author SERPRO
	 */
	@ApplicationScoped
	protected static class ServletEventListener {

		/**
		 * Called before the session is invalidated for that user. Destroys all view scoped beans stored on that
		 * session.
		 */
		protected void clearInvalidatedSession(@Observes BeforeSessionDestroyed event) {
			String sessionId = event.getSessionId();
			try {
				Context context = Beans.getBeanManager().getContext(ViewScoped.class);
				if (FacesViewContextImpl.class.isInstance(context)) {
					((FacesViewContextImpl) context).clearInvalidatedSession(sessionId);
				}
			} catch (ContextNotActiveException ce) {
				// Nada a fazer, contexto não está ativo.
			}
		}

		/**
		 * Called before the current request is about to go out of scope. Checks if any currently
		 * active views have expired and requests the destruction of those beans according to CDI
		 * lifecycle.
		 * 
		 */
		protected void clearExpiredViews(@Observes BeforeRequestDestroyed event) {
			ServletRequest request = event.getRequest();
			
			if (HttpServletRequest.class.isInstance(request)) {
				HttpSession session = ((HttpServletRequest) request).getSession(false);
				
				if (session != null) {
					try {
						final Context context = Beans.getBeanManager().getContext(ViewScoped.class);
						final String currentSessionId = session.getId();

						if (FacesViewContextImpl.class.isInstance(context)) {
							new Thread() {

								@Override
								public void run() {
									((FacesViewContextImpl) context).clearExpiredViews(currentSessionId);
								}
							}.start();
						}
					} catch (ContextNotActiveException ce) {
						// Nada a fazer, contexto não está ativo.
					}
				}
			}
		}
	}
}
