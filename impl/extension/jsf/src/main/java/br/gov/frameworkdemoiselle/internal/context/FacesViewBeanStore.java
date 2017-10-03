package br.gov.frameworkdemoiselle.internal.context;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import br.gov.frameworkdemoiselle.context.ViewContext;
import br.gov.frameworkdemoiselle.lifecycle.ViewScoped;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Faces;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Store that keeps view scoped beans. It associates all view scoped beans with a view ID. When the ID changes (meaning
 * the view changed) all old view scoped beans are destroyed before new beans for the new view are created and stored.
 * 
 * @author SERPRO
 */
public class FacesViewBeanStore implements Serializable {

	private static final long serialVersionUID = -8265458933971929432L;

	/**
	 * Demoiselle specific context parameter name of maximum active view scopes in
	 * session.
	 */
	public static final String PARAM_NAME_MAX_ACTIVE_VIEW_SCOPES = "br.gov.frameworkdemoiselle.MAX_ACTIVE_VIEW_SCOPES";

	/**
	 * Mojarra specific context parameter name of maximum number of logical views in
	 * session.
	 */
	public static final String PARAM_NAME_MOJARRA_NUMBER_OF_VIEWS = "com.sun.faces.numberOfLogicalViews";

	/**
	 * MyFaces specific context parameter name of maximum number of views in
	 * session.
	 */
	public static final String PARAM_NAME_MYFACES_NUMBER_OF_VIEWS = "org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION";

	/** Default value of maximum active view scopes in session. */
	public static final int DEFAULT_MAX_ACTIVE_VIEW_SCOPES = 20; // Mojarra's default is 15 and MyFaces' default is 20.

	private static final String[] PARAM_NAMES_MAX_ACTIVE_VIEW_SCOPES = { PARAM_NAME_MAX_ACTIVE_VIEW_SCOPES,
			PARAM_NAME_MOJARRA_NUMBER_OF_VIEWS, PARAM_NAME_MYFACES_NUMBER_OF_VIEWS };

	private static volatile Integer maxActiveViewScopes;

	private final Map<Long, BeanStore> viewStore = Collections.synchronizedMap(new LRUViewStoreMap());

	/**
	 * Gets the store that contains the view scoped beans for that view ID. If no store exists (new view) one is
	 * created.
	 * 
	 * @param viewId
	 *            ID of the current view
	 * @param context
	 *            Reference to the {@link ViewContext} class managing the view scope
	 * @return The {@link BeanStore} that stores view scoped beans for this view ID
	 * @throws IllegalStateException
	 *             if the view associated with the requested view ID has expired
	 */
	public BeanStore getStoreForView(Long viewId, AbstractCustomContext context) {
		BeanStore store;
		synchronized (viewStore) {
			store = viewStore.get(viewId);
			if (store == null) {
				store = AbstractCustomContext.createStore();
				viewStore.put(viewId, store);
			}
		}

		return store;
	}

	/**
	 * Destroys all View scoped beans and the associated {@link BeanStore} for this user's session. The destroyed beans
	 * will respect CDI bean lifecycle, thus they'll trigger any events associated with destroying beans.
	 * 
	 * @param context
	 *            ViewContext managing the view scoped beans
	 */
	public synchronized void destroyStores(final AbstractCustomContext context) {
		for (Iterator<Entry<Long, BeanStore>> it = viewStore.entrySet().iterator(); it.hasNext();) {
			Entry<Long, BeanStore> currentEntry = it.next();
			BeanStore store = currentEntry.getValue();

			destroyStore(context, store);
			it.remove();
		}
	}

	/**
	 * Destroys all view scoped beans and the associated {@link BeanStore} for the specified viewId.
	 * 
	 * @param context
	 *            ViewContext managing the view scoped beans
	 * @param viewId
	 *            ID of the current view
	 */
	public void destroyStore(final AbstractCustomContext context, Long viewId) {
		BeanStore store = viewStore.remove(viewId);
		if (store != null) {
			destroyStore(context, store);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void destroyStore(final AbstractCustomContext context, BeanStore store) {
		for (String id : store) {
			Contextual contextual = context.getContextualStore().getContextual(id);
			Object instance = store.getInstance(id);
			CreationalContext creationalContext = store.getCreationalContext(id);

			if (contextual != null && instance != null) {
				contextual.destroy(instance, creationalContext);
			}
		}

		store.clear();
	}

	/**
	 * Returns the max active view scopes depending on available context params.
	 * This will be calculated lazily once and re-returned everytime; the faces
	 * context is namely not available during class' initialization/construction,
	 * but only during a post construct.<br>
	 * (from OmniFaces implementation)
	 */
	private static int getMaxActiveViewScopes() {
		if (maxActiveViewScopes != null) {
			return maxActiveViewScopes;
		}

		for (String name : PARAM_NAMES_MAX_ACTIVE_VIEW_SCOPES) {
			String value = Faces.getInitParameter(name);

			if (value != null) {
				try {
					maxActiveViewScopes = Integer.valueOf(value);
					return maxActiveViewScopes;
				} catch (NumberFormatException e) {
					String message = getMessageBundle().getString("max-active-view-scopes-param-invalid");
					throw new IllegalArgumentException(MessageFormat.format(message, name, value), e);
				}
			}
		}

		maxActiveViewScopes = DEFAULT_MAX_ACTIVE_VIEW_SCOPES;
		return maxActiveViewScopes;
	}

	private static java.util.ResourceBundle getMessageBundle() {
		return ResourceBundle.getBundle("demoiselle-jsf-bundle");
	}

	private final class LRUViewStoreMap extends LinkedHashMap<Long, BeanStore> {

		private static final long serialVersionUID = -2520661683192850878L;

		protected boolean removeEldestEntry(Map.Entry<Long, BeanStore> eldest) {
			if (size() > getMaxActiveViewScopes()) {

				final Context context = Beans.getBeanManager().getContext(ViewScoped.class);

				if (context instanceof AbstractCustomContext) {
					destroyStore((AbstractCustomContext) context, eldest.getValue());
				}
				return true;
			}
			return false;
		}
	}
}
