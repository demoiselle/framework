package br.gov.frameworkdemoiselle.util;

import br.gov.frameworkdemoiselle.DemoiselleException;

public class PageNotFoundException extends DemoiselleException {

	private static final long serialVersionUID = 1L;

	private final String viewId;

	public PageNotFoundException(String viewId) {
		// TODO Colocar a mensage no bundle
		super(viewId + " not found");
		this.viewId = viewId;
	}

	public String getViewId() {
		return viewId;
	}
}
