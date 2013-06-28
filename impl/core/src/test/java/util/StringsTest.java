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
package util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.gov.frameworkdemoiselle.annotation.Ignore;
import br.gov.frameworkdemoiselle.util.Strings;

public class StringsTest {

	@Test
	public void getString() {
		testEqualsGetString("teste", "teste");
		testEqualsGetString("", "");
		testEqualsGetString(null, null);
		testEqualsGetString(null, null, "teste");
		testEqualsGetString("{0}", "teste", "teste");
		testEqualsGetString("teste", "teste", (Object[]) null);
		testEqualsGetString("teste {0}", "teste 1", "1");
		testEqualsGetString("{0} teste", "Um teste", "Um");
		testEqualsGetString("{1} testando {0}", "Apenas testando novamente", "novamente", "Apenas");
		testEqualsGetString("{0} testando {1}", "Apenas testando {1}", "Apenas", null);
		testEqualsGetString("testando {1} novamente", "testando isto novamente", "aquilo", "isto");
		testEqualsGetString("teste", "teste", "1", "2");
		testEqualsGetString("teste {0}.", "teste \\.", "\\");
	}

	@Test
	public void isEmpty() {
		assertTrue(Strings.isEmpty(null));
		assertTrue(Strings.isEmpty(""));
		assertTrue(Strings.isEmpty(" "));
		assertTrue(Strings.isEmpty("                 "));

		assertFalse(Strings.isEmpty(" _ "));
		assertFalse(Strings.isEmpty("."));
		assertFalse(Strings.isEmpty("null"));
	}

	@Test
	public void isResourceBundleKeyFormat() {
		assertTrue(Strings.isResourceBundleKeyFormat("{x}"));
		assertTrue(Strings.isResourceBundleKeyFormat("{.}"));
		assertTrue(Strings.isResourceBundleKeyFormat("{*}"));
		assertTrue(Strings.isResourceBundleKeyFormat("{$}"));
		assertFalse(Strings.isResourceBundleKeyFormat("{}"));
		assertFalse(Strings.isResourceBundleKeyFormat(""));
		assertFalse(Strings.isResourceBundleKeyFormat(" "));
		assertFalse(Strings.isResourceBundleKeyFormat(null));
		assertFalse(Strings.isResourceBundleKeyFormat("a{a}a"));
		assertFalse(Strings.isResourceBundleKeyFormat("a{a}"));
		assertFalse(Strings.isResourceBundleKeyFormat("{a}a"));
		assertFalse(Strings.isResourceBundleKeyFormat(" {a} "));
		assertFalse(Strings.isResourceBundleKeyFormat("{a"));
		assertFalse(Strings.isResourceBundleKeyFormat("a}"));
	}

	@Test
	public void camelCaseToSymbolSeparated() {
		assertEquals(null, Strings.camelCaseToSymbolSeparated(null, null));
		assertEquals(null, Strings.camelCaseToSymbolSeparated(null, "."));
		assertEquals("myvar", Strings.camelCaseToSymbolSeparated("myVar", null));
		assertEquals("myvar", Strings.camelCaseToSymbolSeparated("myVar", ""));
		assertEquals("my.var", Strings.camelCaseToSymbolSeparated("myVar", "."));
		assertEquals("my-var", Strings.camelCaseToSymbolSeparated("MyVar", "-"));
		assertEquals("my?var?name", Strings.camelCaseToSymbolSeparated("myVarName", "?"));
		assertEquals("my___var___name", Strings.camelCaseToSymbolSeparated("myVarName", "___"));
	}

	@Test
	public void firstToUpper() {
		assertNull(Strings.firstToUpper(null));
		assertEquals("", Strings.firstToUpper(""));
		assertEquals("A", Strings.firstToUpper("a"));
		assertEquals("A", Strings.firstToUpper("A"));
		assertEquals("Ab", Strings.firstToUpper("Ab"));
		assertEquals("AB", Strings.firstToUpper("AB"));
		assertEquals("Ab", Strings.firstToUpper("ab"));
		assertEquals("AB", Strings.firstToUpper("aB"));
	}

	@Test
	public void toStringWhenObjectIsNull() {
		assertEquals("", Strings.toString(null));
	}

	@Test
	public void classToString() throws SecurityException, NoSuchFieldException {
		@SuppressWarnings("unused")
		class Test {

			private String name = "myName";

			private String lastname = "myLastname";

			private String nullField = null;

			@Ignore
			private String ignore = "ignoreMe";

		}

		String result = Strings.toString(new Test());
		assertTrue(result.contains("Test [name=myName, lastname=myLastname, nullField=null, this"));
	}

	private void testEqualsGetString(final String in, final String expected, final Object... params) {
		String out = Strings.getString(in, params);
		assertEquals(expected, out);
	}

	@Test
	public void removeBraces() {
		assertNull(Strings.removeBraces(null));
		assertEquals("", Strings.removeBraces(""));
		assertEquals(" ", Strings.removeBraces(" "));
		assertEquals(" {x} ", Strings.removeBraces(" {x} "));
		assertEquals("{x} ", Strings.removeBraces("{x} "));
		assertEquals(" {x}", Strings.removeBraces(" {x}"));
		assertEquals("x", Strings.removeBraces("{x}"));
		assertEquals("a b c", Strings.removeBraces("{a b c}"));
		assertEquals("{}", Strings.removeBraces("{}"));
		assertEquals("{}", Strings.removeBraces("{{}}"));
		assertEquals("?", Strings.removeBraces("{?}"));
		assertEquals("*", Strings.removeBraces("{*}"));
	}

	@Test
	public void insertBraces() {
		assertNull(Strings.insertBraces(null));
		assertEquals("", Strings.insertBraces(""));
		assertEquals(" ", Strings.insertBraces(" "));
		assertEquals("{ x }", Strings.insertBraces(" x "));
		assertEquals("{ {x} }", Strings.insertBraces(" {x} "));
		assertEquals("{{x}", Strings.insertBraces("{x"));
		assertEquals("{*}", Strings.insertBraces("*"));
		assertEquals("{?}", Strings.insertBraces("?"));
	}

	@Test
	public void removeCharsWhenStringIsNull() {
		assertEquals(null, Strings.removeChars(null, 'a'));
	}

	@Test
	public void removeCharsWhenStringIsNotNull() {
		String string = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus lobortis.";
		string = Strings.removeChars(string, 'L', 'l');
		assertEquals(-1, string.indexOf('L'));
		assertEquals(-1, string.indexOf('l'));
	}

	@Test
	public void insertZeros() {
		String string = "Lorem ipsum";
		assertEquals("00000", Strings.insertZeros(null, 5));
		assertEquals(string, Strings.insertZeros(string, string.length() - 1));
		assertEquals(string, Strings.insertZeros(string, string.length()));
		assertEquals("0" + string, Strings.insertZeros(string, string.length() + 1));
		assertEquals("00" + string, Strings.insertZeros(string, string.length() + 2));
	}
}
