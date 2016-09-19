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
package org.demoiselle.util;

import org.demoiselle.annotation.Ignore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contain a set of methods that implements a set of functionalities that envolves manipulation of strings.
 *
 * @author SERPRO
 */
public final class Strings {

	private Strings() {
	}

	/**
	 * Returns if some string matches with the format of a ResourceBundle key or not.
	 *
	 * @param key string to check if matches with key format of ResourceBundle.
	 * @return boolean true if matches and false otherwise.
	 */
	public static boolean isResourceBundleKeyFormat(final String key) {
		return Pattern.matches("^\\{(.+)\\}$", key == null ? "" : key);
	}

	/**
	 * Removes specific characteres from a given string.
	 *
	 * @param string string to be changed, by the removing of some characters.
	 * @param chars  characters to be removed from string.
	 * @return String returns the given string without the given characters.
	 */
	public static String removeChars(String string, char... chars) {
		String result = string;

		if (result != null) {
			for (char ch : chars) {
				result = result.replace(String.valueOf(ch), "");
			}
		}
		return result;
	}

	public static String join(String separator, String... strings) {
		StringBuffer result = new StringBuffer();

		if (strings != null) {
			for (int i = 0; i < strings.length; i++) {
				if (i != 0 && separator != null) {
					result.append(separator);
				}

				if (strings[i] != null) {
					result.append(strings[i]);
				}
			}
		}

		return result.length() > 0 ? result.toString() : null;
	}

	/**
	 * Inserts the character "0" in the begin of a given string. The quantity of zeros that will be placed depends on
	 * the difference between the length of the given string and the value of howMuchZeros.
	 *
	 * @param string       string to insert zeros characthers.
	 * @param howMuchZeros its controls how much zeros will be insert.
	 * @return String Retuns the string, added with appropriate number of zeros. For exemplo, if string = "yes" and
	 * howMuchZeros = 5, the returned string will be "00yes".
	 */
	public static String insertZeros(String string, int howMuchZeros) {
		StringBuffer result = new StringBuffer((string == null ? "" : string).trim());
		int difference = howMuchZeros - result.toString().length();

		for (int j = 0; j < difference; j++) {
			result.insert(0, '0');
		}

		return result.toString();
	}

	/**
	 *      * Replaces the numbers between braces in the given string with the given parameters. The process will replace a
	 * number between braces for the parameter for which its order in the set of parameters matches with the number of
	 * the given string.
	 * For exemple, if is received the following string "Treats an {0} exception" and the set of parameters
	 * {"DemoiselleException"}, the return will be the following string: "Treats an DemoiselleException exception".
	 *
	 * @param string with the numbers with braces to be replaced with the parameters.
	 * @param params parameters that will replace the number with braces in the given string.
	 * @return String string with numbers replaced with the matching parameter.
	 */
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

	/**
	 * Verifies if a given string is empty or null.
	 *
	 * @param string string to be verified.
	 * @return boolean returns true if the given string is empty or null and returns false otherwise.
	 */
	public static boolean isEmpty(String string) {
		return string == null || string.trim().isEmpty();
	}

	/**
	 * Converts any object to string.
	 *
	 * @param object object to be converted.
	 * @return String the given object converted to string.
	 */
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
					result.append('=');
					fieldValue = Reflections.getFieldValue(field, object);
					result.append(fieldValue != null && fieldValue.getClass().isArray() ?
							Arrays.toString((Object[]) fieldValue) :
							fieldValue);
				}
			}

			result.append(']');
		}

		return result.toString();
	}

	/**
	 * Replace the camel case string for a lowercase string separated for a given symbol.
	 *
	 * @param string string that separeted with camel case.
	 * @param symbol simbol to be the new separator for the given string.
	 * @return String the given string separated with the given symbol.
	 */
	public static String camelCaseToSymbolSeparated(String string, String symbol) {
		if (symbol == null) {
			symbol = "";
		}

		return string == null ? null : string.replaceAll("\\B([A-Z])", symbol + "$1").toLowerCase();
	}

	/**
	 * Sets the first character of a given string to upper case.
	 *
	 * @param string    Full string to convert
	 * @return String the given string with the first character setted to upper case.
	 */
	public static String firstToUpper(String string) {
		String result = string;

		if (!Strings.isEmpty(string)) {
			result = string.toUpperCase().charAt(0) + (string.length() > 1 ? string.substring(1) : "");
		}

		return result;
	}

	/**
	 * Removes braces from a given string.
	 *
	 * @param string Message to remove braces from
	 * @return String the given string without braces.
	 */
	public static String removeBraces(String string) {
		String result = string;

		if (isResourceBundleKeyFormat(string)) {
			result = string.substring(1, string.length() - 1);
		}

		return result;
	}

	/**
	 * Inserts braces in a given string.
	 *
	 * @param string Original string to insert braces on.
	 * @return String the given string with braces.
	 */
	public static String insertBraces(String string) {
		String result = string;

		if (!isEmpty(string)) {
			result = "{" + string + "}";
		}

		return result;
	}

	public static String parse(InputStream inputStream) throws IOException {
		StringBuilder result = new StringBuilder();

		if (inputStream != null) {
			BufferedReader reader = null;

			try {
				reader = new BufferedReader(new InputStreamReader(inputStream));
				String line;

				while ((line = reader.readLine()) != null) {
					result.append(line);
				}

			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		}

		return result.length() > 0 ? result.toString() : null;
	}
}
