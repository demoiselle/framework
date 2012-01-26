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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

import javax.inject.Singleton;

import br.gov.frameworkdemoiselle.message.DefaultMessage;
import br.gov.frameworkdemoiselle.message.SeverityType;

@Singleton
public class ResourceBundle extends java.util.ResourceBundle implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient java.util.ResourceBundle delegate;

	public ResourceBundle(java.util.ResourceBundle resourceBundle) {
		this.delegate = resourceBundle;
	}

	@Override
	public boolean containsKey(String key) {
		return delegate.containsKey(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		return delegate.getKeys();
	}

	@Override
	public Locale getLocale() {
		return delegate.getLocale();
	}

	@Override
	public Set<String> keySet() {
		return delegate.keySet();
	}

	public String getString(String key, Object... params) {
		return Strings.getString(getString(key), params);
	}

	@Override
	protected Object handleGetObject(String key) {
		Object result;

		try {
			Method method = delegate.getClass().getMethod("handleGetObject", String.class);

			method.setAccessible(true);
			result = method.invoke(delegate, key);
			method.setAccessible(false);

		} catch (Exception cause) {
			throw new RuntimeException(cause);
		}
		return result;
	}

	public DefaultMessage getI18nMessage(String bundleKey) {
		return new DefaultMessage(getString(bundleKey));
	}

	public DefaultMessage getI18nMessage(String bundleKey, Object... params) {
		return new DefaultMessage(getString(bundleKey, params));
	}

	public DefaultMessage getI18nMessage(String bundleKey, SeverityType type) {
		return new DefaultMessage(getString(bundleKey), type);
	}

	public DefaultMessage getI18nMessage(String bundleKey, SeverityType type, Object... params) {
		return new DefaultMessage(getString(bundleKey, params), type);
	}

}
