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
package br.gov.frameworkdemoiselle;

import java.util.HashSet;
import java.util.Set;

public class HttpViolationException extends Exception {

	private static final long serialVersionUID = 1L;

	private Set<Violation> violations = new HashSet<Violation>();

	private int statusCode;

	private String mediaType = "application/json";

	public HttpViolationException(int statusCode) {
		this.statusCode = statusCode;
	}

	public HttpViolationException(int statusCode, String mediaType) {
		this.mediaType = mediaType;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getMediaType() {
		return mediaType;
	}

	public HttpViolationException addViolation(String message) {
		return addViolation(null, message);
	}

	public HttpViolationException addViolation(String property, String message) {
		this.violations.add(new Violation(property, message));
		return this;
	}

	public Set<Violation> getViolations() {
		return violations;
	}

	public static class Violation {

		public String property;

		public String message;

		public Violation() {
		}

		public Violation(String property, String message) {
			this.property = property;
			this.message = message;
		}

		public String getProperty() {
			return property;
		}

		public void setProperty(String property) {
			this.property = property;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((message == null) ? 0 : message.hashCode());
			result = prime * result + ((property == null) ? 0 : property.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Violation other = (Violation) obj;
			if (message == null) {
				if (other.message != null)
					return false;
			} else if (!message.equals(other.message))
				return false;
			if (property == null) {
				if (other.property != null)
					return false;
			} else if (!property.equals(other.property))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return this.property + " " + this.message;
		}
	}
}
