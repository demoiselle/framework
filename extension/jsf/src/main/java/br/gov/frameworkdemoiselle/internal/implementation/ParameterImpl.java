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
package br.gov.frameworkdemoiselle.internal.implementation;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.annotation.ViewScoped;
import br.gov.frameworkdemoiselle.util.Faces;
import br.gov.frameworkdemoiselle.util.Parameter;
import br.gov.frameworkdemoiselle.util.Reflections;

public class ParameterImpl<T> implements Parameter<T>, Serializable {

	private static final long serialVersionUID = 1L;

	private Class<Object> type;

	private Converter converter;

	private final InjectionPoint ip;

	private final HttpServletRequest request;

	private T value;

	private final String key;

	@Inject
	public ParameterImpl(InjectionPoint ip, HttpServletRequest request) {
		this.ip = ip;
		this.request = request;

		if (ip.getAnnotated().isAnnotationPresent(Name.class)) {
			this.key = ip.getAnnotated().getAnnotation(Name.class).value();
		} else {
			this.key = ip.getMember().getName();
		}

		this.type = Reflections.getGenericTypeArgument(ip.getMember(), 0);
		this.converter = Faces.getConverter(type);
	}

	public String getKey() {
		return key;
	}

	private boolean isSessionScoped() {
		return ip.getAnnotated().isAnnotationPresent(SessionScoped.class);
	}

	private boolean isViewScoped() {
		return ip.getAnnotated().isAnnotationPresent(ViewScoped.class);
	}

	private boolean isRequestScoped() {
		return ip.getAnnotated().isAnnotationPresent(RequestScoped.class);
	}

	@SuppressWarnings("unchecked")
	public T getValue() {
		T result = null;
		String parameterValue = request.getParameter(key);

		if (isSessionScoped()) {
			if (parameterValue != null) {
				request.getSession().setAttribute(key, Faces.convert(parameterValue, converter));
			}

			result = (T) request.getSession().getAttribute(key);

		} else if (isRequestScoped()) {
			result = (T) Faces.convert(parameterValue, converter);

		} else if (isViewScoped()) {
			Map<String, Object> viewMap = Faces.getViewMap();
			if (parameterValue != null) {
				viewMap.put(key, Faces.convert(parameterValue, converter));
			}

			result = (T) viewMap.get(key);

		} else {
			if (value == null) {
				value = (T) Faces.convert(parameterValue, converter);
			}

			result = value;
		}

		return result;
	}

	public Converter getConverter() {
		return converter;
	}

	@Override
	public void setValue(T value) {
		if (isSessionScoped()) {
			this.request.getSession().setAttribute(key, value);

		} else if (isRequestScoped()) {
			// FIXME Lançar exceção informando que não é possível setar parâmetros no request.

		} else if (isViewScoped()) {
			Map<String, Object> viewMap = Faces.getViewMap();
			viewMap.put(key, value);

		} else {
			this.value = value;
		}
	}
}
