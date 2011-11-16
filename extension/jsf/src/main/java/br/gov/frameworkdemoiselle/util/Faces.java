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
package br.gov.frameworkdemoiselle.util;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_FATAL;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;

import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import br.gov.frameworkdemoiselle.exception.ApplicationException;
import br.gov.frameworkdemoiselle.message.Message;
import br.gov.frameworkdemoiselle.message.SeverityType;

import com.sun.faces.util.Util;

public class Faces {

	public static void addMessages(final List<Message> messages) {
		if (messages != null) {
			for (Message m : messages) {
				addMessage(m);
			}
		}
	}

	public static void addMessage(final Message message) {
		getFacesContext().addMessage(null, parse(message));
	}

	public static void addMessage(final String clientId, final Message message) {
		getFacesContext().addMessage(clientId, parse(message));
	}

	public static void addMessage(final String clientId, final Throwable throwable) {
		getFacesContext().addMessage(clientId, parse(throwable));
	}

	public static void addMessage(final Throwable throwable) {
		addMessage(null, throwable);
	}

	private static FacesContext getFacesContext() {
		return Beans.getReference(FacesContext.class);
	}

	public static Severity parse(final SeverityType severityType) {
		Severity result = null;

		switch (severityType) {
			case INFO:
				result = SEVERITY_INFO;
				break;
			case WARN:
				result = SEVERITY_WARN;
				break;
			case ERROR:
				result = SEVERITY_ERROR;
				break;
			case FATAL:
				result = SEVERITY_FATAL;
		}

		return result;
	}

	public static FacesMessage parse(final Throwable throwable) {
		FacesMessage facesMessage = new FacesMessage();
		ApplicationException annotation = throwable.getClass().getAnnotation(ApplicationException.class);

		if (annotation != null) {
			facesMessage.setSeverity(parse(annotation.severity()));
		} else {
			facesMessage.setSeverity(SEVERITY_ERROR);
		}

		if (throwable.getMessage() != null) {
			facesMessage.setSummary(throwable.getMessage());
		} else {
			facesMessage.setSummary(throwable.toString());
		}

		return facesMessage;
	}

	public static FacesMessage parse(final Message message) {
		FacesMessage facesMessage = new FacesMessage();
		facesMessage.setSeverity(parse(message.getSeverity()));
		facesMessage.setSummary(message.getText());
		return facesMessage;
	}

	public static Object convert(final String value, final Converter converter) {
		Object result = null;

		if (!Strings.isEmpty(value)) {
			if (converter != null) {
				result = converter.getAsObject(getFacesContext(), getFacesContext().getViewRoot(), value);
			} else {
				result = new String(value);
			}
		}

		return result;
	}

	public static Converter getConverter(Class<?> clazz) {
		return Util.getConverterForClass(clazz, getFacesContext());
	}

	public static Map<String, Object> getViewMap() {
		UIViewRoot viewRoot = getFacesContext().getViewRoot();
		return viewRoot.getViewMap(true);
	}

}
