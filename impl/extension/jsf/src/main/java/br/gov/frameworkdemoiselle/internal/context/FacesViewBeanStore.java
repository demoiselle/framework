package br.gov.frameworkdemoiselle.internal.context;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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
	
	private static final int MAX_VIEWS_IN_SESSION = 15;
	
	private static final AtomicLong AGE_COUNTER = new AtomicLong();
	
	private ConcurrentHashMap<Long, BeanStoreAge> beanStoreCache = new ConcurrentHashMap<Long, FacesViewBeanStore.BeanStoreAge>();

	synchronized BeanStore getStore(Long viewId, AbstractCustomContext context) {
		BeanStoreAge beanStoreWithAge = beanStoreCache.get(viewId);
		if (beanStoreWithAge == null) {
			beanStoreWithAge = new BeanStoreAge();
			beanStoreWithAge.beanStore = AbstractCustomContext.createStore();
			beanStoreCache.put(viewId, beanStoreWithAge);
		}
		beanStoreWithAge.age = AGE_COUNTER.getAndIncrement();
		
		if (beanStoreCache.size() > MAX_VIEWS_IN_SESSION) {
			clearExpiredStores(context);
		}
		
		return beanStoreWithAge.beanStore;
	}

	private synchronized void clearExpiredStores(AbstractCustomContext context) {
		BeanStoreAge oldestCache = null;
		for (BeanStoreAge cache : beanStoreCache.values()) {
			if (oldestCache == null || cache.age < oldestCache.age) {
				oldestCache = cache;
			}
		}
		
		if (oldestCache != null) {
			clear(context, oldestCache.beanStore);
		}
	}
	
	public void clear(AbstractCustomContext context) {
		for (BeanStoreAge cache : beanStoreCache.values()) {
			clear(context, cache.beanStore);
		}
		beanStoreCache.clear();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void clear(AbstractCustomContext context, BeanStore store) {
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
	
	private class BeanStoreAge implements Serializable {
		private static final long serialVersionUID = 1L;
		BeanStore beanStore;
		long age;
	}
}
