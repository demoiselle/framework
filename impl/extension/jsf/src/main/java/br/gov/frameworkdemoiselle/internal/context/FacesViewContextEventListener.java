package br.gov.frameworkdemoiselle.internal.context;

import javax.enterprise.context.spi.Context;
import javax.faces.component.UIViewRoot;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.ViewMapListener;

import br.gov.frameworkdemoiselle.lifecycle.ViewScoped;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * Listener for JSF view scope destroy events so that view scope context can be notified.
 */
public class FacesViewContextEventListener implements ViewMapListener {

	@Override
	public void processEvent(SystemEvent event) throws AbortProcessingException {
		if (event instanceof PreDestroyViewMapEvent) {
			final Context context = Beans.getBeanManager().getContext(ViewScoped.class);

			if (context instanceof FacesViewContextImpl) {
				((FacesViewContextImpl) context).clearView();
			}
		}
	}

	@Override
	public boolean isListenerForSource(Object source) {
		return (source instanceof UIViewRoot);
	}

}
