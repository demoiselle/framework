package br.gov.frameworkdemoiselle.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.context.FacesContext;

public class Redirector implements Serializable {

	private static final long serialVersionUID = 1L;

	public static void redirect(String viewId) {
		redirect(viewId, null);
	}

	public static void redirect(String viewId, Map<String, Object> params) {
		try {
			if (viewId != null && !viewId.isEmpty()) {
				FacesContext facesContext = Beans.getReference(FacesContext.class);
				ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
				String url = viewHandler.getBookmarkableURL(facesContext, viewId, parse(params), true);

				facesContext.getExternalContext().redirect(url);
			}

		} catch (NullPointerException cause) {
			throw new PageNotFoundException(viewId);

		} catch (IOException cause) {
			throw new FacesException(cause);
		}
	}

	private static Map<String, List<String>> parse(Map<String, Object> map) {
		Map<String, List<String>> result = null;

		if (map != null) {
			ArrayList<String> list;
			result = new HashMap<String, List<String>>();

			for (String key : map.keySet()) {
				list = new ArrayList<String>();
				list.add(map.get(key).toString());
				result.put(key, list);
			}
		}

		return result;
	}
}
