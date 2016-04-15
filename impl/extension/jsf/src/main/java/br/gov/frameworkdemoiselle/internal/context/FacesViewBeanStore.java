package br.gov.frameworkdemoiselle.internal.context;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import br.gov.frameworkdemoiselle.context.ViewContext;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Store that keeps view scoped beans. It associates all view scoped beans with a view ID. When the ID changes (meaning
 * the view changed) all old view scoped beans are destroyed before new beans for the new view are created and stored.
 * 
 * @author SERPRO
 */
@Deprecated
public class FacesViewBeanStore implements Serializable {

	private static final long serialVersionUID = -8265458933971929432L;

	private final ConcurrentHashMap<Long, FacesViewData> viewStore = new ConcurrentHashMap<Long, FacesViewBeanStore.FacesViewData>();

	private long maxInactiveTimeInSeconds;

	public FacesViewBeanStore(long maxInactiveTimeInSeconds) {
		this.maxInactiveTimeInSeconds = maxInactiveTimeInSeconds;
	}

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
		FacesViewData data = null;
		synchronized (viewStore) {
			data = viewStore.get(viewId);
			if (data == null) {
				BeanStore store = AbstractCustomContext.createStore();
				data = new FacesViewData();
				data.store = store;
				viewStore.put(viewId, data);
			} else if (data.isExpired(maxInactiveTimeInSeconds)) {
				throw new IllegalStateException(ResourceBundle.getBundle("demoiselle-jsf-bundle").getString(
						"view-expired"));
			}
		}

		data.lastTimeAccessed = System.currentTimeMillis();
		return data.store;
	}

	/**
	 * @see #destroyStoresInSession(AbstractCustomContext, boolean)
	 */
	public void destroyStoresInSession(AbstractCustomContext context) {
		destroyStoresInSession(context, false);
	}

	/**
	 * Destroys all View scoped beans and the associated {@link BeanStore} for this user's session. The destroyed beans
	 * will respect CDI bean lifecycle, thus they'll trigger any events associated with destroying beans.
	 * 
	 * @param context
	 *            ViewContext managing the view scoped beans
	 * @param onlyExpired
	 *            Only destroy beans if the underlying view has expired
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void destroyStoresInSession(final AbstractCustomContext context, final boolean onlyExpired) {
		for (Iterator<Entry<Long, FacesViewData>> it = viewStore.entrySet().iterator(); it.hasNext();) {
			Entry<Long, FacesViewData> currentEntry = it.next();
			FacesViewData data = currentEntry.getValue();

			if (data != null && data.store != null) {
				if (!onlyExpired || data.isExpired(maxInactiveTimeInSeconds)) {
					for (String id : data.store) {
						Contextual contextual = context.getContextualStore().getContextual(id);
						Object instance = data.store.getInstance(id);
						CreationalContext creationalContext = data.store.getCreationalContext(id);

						if (contextual != null && instance != null) {
							contextual.destroy(instance, creationalContext);
						}
					}

					data.store.clear();
					it.remove();
				}
			}
		}
	}

	/**
	 * Contains a {@link BeanStore} with some metadata, like the last time this store was accessed (used to determine
	 * when a store expires).
	 * 
	 * @author serpro
	 */
	private static class FacesViewData {

		long lastTimeAccessed;

		BeanStore store;

		public synchronized boolean isExpired(long viewTimeoutInSeconds) {
			return ((System.currentTimeMillis() - lastTimeAccessed) / 1000) > viewTimeoutInSeconds;
		}
	}
}
