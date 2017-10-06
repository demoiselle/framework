package br.gov.frameworkdemoiselle.internal.context;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.inject.Inject;

import br.gov.frameworkdemoiselle.internal.bootstrap.CustomContextBootstrap;
import br.gov.frameworkdemoiselle.util.Faces;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Store that keeps view scoped beans. It associates all view scoped beans with a view ID. When the ID changes (meaning
 * the view changed) all old view scoped beans are destroyed before new beans for the new view are created and stored.
 * 
 * @author SERPRO
 */
@SessionScoped
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

	private Map<Long, BeanStore> viewStore;

	@Inject
	private CustomContextBootstrap bootstrap;

	@PostConstruct
	protected void postConstruct() {
		viewStore = Collections.synchronizedMap(new LRUViewStoreMap());
	}

	/**
	 * Gets the store that contains the view scoped beans for that view ID. If no store exists (new view) one is
	 * created.
	 * 
	 * @param viewId
	 *            ID of the current view
	 * @return The {@link BeanStore} that stores view scoped beans for this view ID
	 */
	public BeanStore getStoreForView(Long viewId) {
		BeanStore store = viewStore.get(viewId);

		if (store == null) {
			store = AbstractCustomContext.createStore();
			viewStore.put(viewId, store);
		}

		return store;
	}

	/**
	 * Destroys all View scoped beans and the associated {@link BeanStore} for this user's session. The destroyed beans
	 * will respect CDI bean lifecycle, thus they'll trigger any events associated with destroying beans.
	 */
	@PreDestroy
	public void destroyStores() {
		for (Iterator<Entry<Long, BeanStore>> it = viewStore.entrySet().iterator(); it.hasNext();) {
			Entry<Long, BeanStore> currentEntry = it.next();
			BeanStore store = currentEntry.getValue();

			destroyStore(store);
			it.remove();
		}
	}

	/**
	 * Destroys all view scoped beans and the associated {@link BeanStore} for the specified viewId.
	 * 
	 * @param viewId
	 *            ID of the current view
	 */
	public void destroyStore(Long viewId) {
		BeanStore store = viewStore.remove(viewId);
		if (store != null) {
			destroyStore(store);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void destroyStore(BeanStore store) {
		ContextualStore contextualStore = bootstrap.getContextualStore();

		for (String id : store) {
			Contextual contextual = contextualStore.getContextual(id);
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

				destroyStore(eldest.getValue());
				return true;
			}
			return false;
		}
	}
}
