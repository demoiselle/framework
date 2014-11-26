package br.gov.frameworkdemoiselle.internal.context;

import java.io.Serializable;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Alternative;

/**
 * Store that keeps view scoped beans. It associates all view scoped beans with a view ID.
 * When the ID changes (meaning the view changed) all old view scoped beans are destroyed
 * before new beans for the new view are created and stored.
 * 
 * @author SERPRO
 *
 */
@Alternative
public class FacesViewBeanStore implements Serializable {

	private static final long serialVersionUID = -8265458933971929432L;

	private Long lastViewId = null;

	private BeanStore store;

	synchronized BeanStore getStore(Long viewId, AbstractCustomContext context) {
		if (lastViewId == null || !lastViewId.equals(viewId)) {
			clear(context);
			lastViewId = viewId;
			store = AbstractCustomContext.createStore();
		}

		return store;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void clear(AbstractCustomContext context) {
		if (store != null) {
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
	}
}
