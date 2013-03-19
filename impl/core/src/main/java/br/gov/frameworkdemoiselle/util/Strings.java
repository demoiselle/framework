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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.gov.frameworkdemoiselle.annotation.Ignore;

public final class Strings {

	private Strings() {
	}

	public static boolean isResourceBundleKeyFormat(final String key) {
		return Pattern.matches("^\\{(.+)\\}$", key == null ? "" : key);
	}

	public static String removeChars(String string, char... chars) {
		String result = string;
		
		if (result != null) {
			for (char ch : chars) {
				result = string.replace(String.valueOf(ch), "");
			}
		}
		return result;
	}

	public static String insertZeros(String string, int howMuchZeros) {
		StringBuffer result = new StringBuffer((string == null ? "" : string).trim());
		int difference = howMuchZeros - result.toString().length();

		for (int j = 0; j < difference; j++) {
			result.insert(0, '0');
		}

		return result.toString();
	}

	public static String getString(final String string, final Object... params) {
		String result = null;

		if (string != null) {
			result = new String(string);
		}

		if (params != null && string != null) {
			for (int i = 0; i < params.length; i++) {
				if (params[i] != null) {
					result = result.replaceAll("\\{" + i + "\\}", Matcher.quoteReplacement(params[i].toString()));
				}
			}
		}

		return result;
	}

	public static boolean isEmpty(String string) {
		return string == null || string.trim().isEmpty();
	}

	public static String toString(Object object) {
		StringBuffer result = new StringBuffer();
		Object fieldValue;

		if (object != null) {
			result.append(object.getClass().getSimpleName());
			result.append(" [");

			boolean first = true;
			for (Field field : Reflections.getNonStaticDeclaredFields(object.getClass())) {
				if (!field.isAnnotationPresent(Ignore.class)) {
					if (first) {
						first = false;
					} else {
						result.append(", ");
					}

					result.append(field.getName());
					result.append("=");
					fieldValue = Reflections.getFieldValue(field, object);
					result.append(fieldValue != null && fieldValue.getClass().isArray() ? Arrays
							.toString((Object[]) fieldValue) : fieldValue);
				}
			}

			result.append("]");
		}

		return result.toString();
	}

	public static String camelCaseToSymbolSeparated(String string, String symbol) {
		if (symbol == null) {
			symbol = "";
		}

		return string == null ? null : string.replaceAll("\\B([A-Z])", symbol + "$1").toLowerCase();
	}

	public static String firstToUpper(String string) {
		String result = string;

		if (!Strings.isEmpty(string)) {
			result = string.toUpperCase().charAt(0) + (string.length() > 1 ? string.substring(1) : "");
		}

		return result;
	}

	public static String removeBraces(String string) {
		String result = string;

		if (isResourceBundleKeyFormat(string)) {
			result = string.substring(1, string.length() - 1);
		}

		return result;
	}

	public static String insertBraces(String string) {
		String result = string;

		if (!isEmpty(string)) {
			result = "{" + string + "}";
		}

		return result;
	}
}
